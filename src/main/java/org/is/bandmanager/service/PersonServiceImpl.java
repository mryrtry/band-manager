package org.is.bandmanager.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.is.bandmanager.dto.PersonDto;
import org.is.bandmanager.dto.PersonMapper;
import org.is.bandmanager.dto.request.PersonRequest;
import org.is.bandmanager.exception.ServiceException;
import org.is.bandmanager.model.Person;
import org.is.bandmanager.repository.PersonRepository;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;

import static org.is.bandmanager.exception.message.ServiceErrorMessage.MUST_BE_NOT_NULL;
import static org.is.bandmanager.exception.message.ServiceErrorMessage.SOURCE_NOT_FOUND;

@Service
@Validated
@RequiredArgsConstructor
public class PersonServiceImpl implements PersonService {

    private final PersonRepository personRepository;
    private final PersonMapper mapper;

    private Person findById(Long id) {
        if (id == null) {
            throw new ServiceException(MUST_BE_NOT_NULL, "Person.id");
        }
        return personRepository.findById(id)
                .orElseThrow(() -> new ServiceException(SOURCE_NOT_FOUND, "Person", id));
    }

    @Override
    @Transactional
    public PersonDto create(PersonRequest request) {
        Person person = personRepository.save(mapper.toEntity(request));
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
        findById(id);
        Person updatedPerson = mapper.toEntity(request);
        updatedPerson.setId(id);
        return mapper.toDto(personRepository.save(updatedPerson));
    }

    @Override
    @Transactional
    public PersonDto delete(Long id) {
        Person person = findById(id);
        personRepository.delete(person);
        return mapper.toDto(person);
    }

}
