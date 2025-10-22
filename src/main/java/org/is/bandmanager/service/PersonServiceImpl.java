package org.is.bandmanager.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.is.bandmanager.dto.PersonDto;
import org.is.bandmanager.dto.PersonMapper;
import org.is.bandmanager.dto.request.PersonRequest;
import org.is.bandmanager.event.EntityEvent;
import org.is.bandmanager.exception.ServiceException;
import org.is.bandmanager.model.Person;
import org.is.bandmanager.repository.PersonRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;

import static org.is.bandmanager.event.EventType.*;
import static org.is.bandmanager.exception.message.ServiceErrorMessage.MUST_BE_NOT_NULL;
import static org.is.bandmanager.exception.message.ServiceErrorMessage.SOURCE_NOT_FOUND;

@Service
@Validated
@RequiredArgsConstructor
public class PersonServiceImpl implements PersonService {

    private final PersonRepository personRepository;

    private final LocationService locationService;

    private final ApplicationEventPublisher eventPublisher;
    private final PersonMapper mapper;

    private Person findById(Long id) {
        if (id == null) {
            throw new ServiceException(MUST_BE_NOT_NULL, "Person.id");
        }
        return personRepository.findById(id)
                .orElseThrow(() -> new ServiceException(SOURCE_NOT_FOUND, "Person", id));
    }

    @Transactional
    protected void handleDependencies(PersonRequest request, Person entity) {
        entity.setLocation(locationService.getEntity(request.getLocationId()));
    }

    @Override
    @Transactional
    public PersonDto create(PersonRequest request) {
        Person person = mapper.toEntity(request);
        handleDependencies(request, person);
        PersonDto createdPerson = mapper.toDto(personRepository.save(person));
        eventPublisher.publishEvent(new EntityEvent<>(CREATED, createdPerson));
        return mapper.toDto(person);
    }

    @Override
    public List<PersonDto> getAll() {
        return personRepository.findAll().stream().map(mapper::toDto).toList();
    }

    @Override
    public PersonDto get(Long id) {
        return mapper.toDto(findById(id));
    }

    @Override
    public Person getEntity(Long id) {
        return findById(id);
    }

    @Override
    @Transactional
    public PersonDto update(Long id, PersonRequest request) {
        Person updatingPerson = findById(id);
        mapper.updateEntityFromRequest(request, updatingPerson);
        handleDependencies(request, updatingPerson);
        PersonDto updatedPerson = mapper.toDto(updatingPerson);
        eventPublisher.publishEvent(new EntityEvent<>(UPDATED, updatedPerson));
        return updatedPerson;
    }

    @Override
    @Transactional
    public PersonDto delete(Long id) {
        Person person = findById(id);
        personRepository.delete(person);
        PersonDto deletedPerson = mapper.toDto(person);
        eventPublisher.publishEvent(new EntityEvent<>(DELETED, deletedPerson));
        return deletedPerson;
    }

    @Async("cleanupTaskExecutor")
    @Scheduled(fixedDelay = 300000)
    @Transactional
    public void cleanupUnusedPersons() {
        List<Person> unusedFrontMen = personRepository.findUnusedPersons();
        if (!unusedFrontMen.isEmpty()) {
            personRepository.deleteAll(unusedFrontMen);
            eventPublisher.publishEvent(new EntityEvent<>(BULK_DELETED, unusedFrontMen.stream().map(mapper::toDto).toList()));
        }
    }

}
