package org.is.bandmanager.service.person;

import org.is.bandmanager.dto.PersonDto;
import org.is.bandmanager.dto.PersonMapper;
import org.is.bandmanager.dto.request.PersonRequest;
import org.is.bandmanager.event.EntityEvent;
import org.is.bandmanager.exception.ServiceException;
import org.is.bandmanager.exception.message.ServiceErrorMessage;
import org.is.bandmanager.model.Color;
import org.is.bandmanager.model.Country;
import org.is.bandmanager.model.Location;
import org.is.bandmanager.model.Person;
import org.is.bandmanager.repository.PersonRepository;
import org.is.bandmanager.service.location.LocationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.is.bandmanager.exception.message.ServiceErrorMessage.ID_MUST_BE_POSITIVE;
import static org.is.bandmanager.exception.message.ServiceErrorMessage.MUST_BE_NOT_NULL;
import static org.is.bandmanager.exception.message.ServiceErrorMessage.SOURCE_NOT_FOUND;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PersonServiceImplTest {

    private final Location testLocation = Location.builder().id(1L).build();

    @Mock
    private PersonRepository personRepository;

    @Mock
    private LocationService locationService;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private PersonMapper mapper;

    @InjectMocks
    private PersonServiceImpl personService;


    @Test
    @SuppressWarnings("unchecked")
    void shouldCreatePersonSuccessfully() {
        // Given
        PersonRequest request = createPersonRequest("John Doe", Color.BLUE, Color.BROWN, 75.5f, Country.USA, 1L);
        Person person = createPerson(1L, "John Doe", Color.BLUE, Color.BROWN, 75.5f, Country.USA);
        PersonDto personDto = createPersonDto(1L, "John Doe", Color.BLUE, Color.BROWN, 75.5f, Country.USA);

        when(mapper.toEntity(request)).thenReturn(person);
        when(locationService.getEntity(1L)).thenReturn(testLocation);
        when(personRepository.save(person)).thenReturn(person);
        when(mapper.toDto(person)).thenReturn(personDto);

        // When
        PersonDto result = personService.create(request);

        // Then
        assertThat(result).isEqualTo(personDto);
        assertThat(person.getLocation()).isEqualTo(testLocation);

        ArgumentCaptor<EntityEvent<PersonDto>> eventCaptor = ArgumentCaptor.forClass(EntityEvent.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());

        EntityEvent<PersonDto> capturedEvent = eventCaptor.getValue();
        assertThat(capturedEvent.getEventType()).isEqualTo(org.is.bandmanager.event.EventType.CREATED);
        assertThat(capturedEvent.getEntities()).containsExactly(personDto);
    }


    @Test
    void shouldGetPersonById() {
        // Given
        Long personId = 1L;
        Person person = createPerson(personId, "John Doe", Color.BLUE, Color.BROWN, 75.5f, Country.USA);
        PersonDto personDto = createPersonDto(personId, "John Doe", Color.BLUE, Color.BROWN, 75.5f, Country.USA);

        when(personRepository.findById(personId)).thenReturn(Optional.of(person));
        when(mapper.toDto(person)).thenReturn(personDto);

        // When
        PersonDto result = personService.get(personId);

        // Then
        assertThat(result).isEqualTo(personDto);
    }

    @Test
    void shouldGetPersonEntity() {
        // Given
        Long personId = 1L;
        Person person = createPerson(personId, "John Doe", Color.BLUE, Color.BROWN, 75.5f, Country.USA);

        when(personRepository.findById(personId)).thenReturn(Optional.of(person));

        // When
        Person result = personService.getEntity(personId);

        // Then
        assertThat(result).isEqualTo(person);
    }

    @Test
    void shouldThrowExceptionWhenPersonNotFound() {
        // Given
        Long personId = 999L;
        when(personRepository.findById(personId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> personService.get(personId))
                .isInstanceOf(ServiceException.class)
                .hasMessage(SOURCE_NOT_FOUND.getFormattedMessage("Person", personId))
                .extracting(ex -> ((ServiceException) ex).getHttpStatus())
                .isEqualTo(SOURCE_NOT_FOUND.getHttpStatus());
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(longs = {0L, -1L})
    void shouldThrowExceptionWhenIdIsInvalid(Long id) {
        // When & Then
        ServiceErrorMessage expectedError = id == null ? MUST_BE_NOT_NULL : ID_MUST_BE_POSITIVE;
        String expectedField = "Person.id";

        assertThatThrownBy(() -> personService.get(id))
                .isInstanceOf(ServiceException.class)
                .hasMessage(expectedError.getFormattedMessage(expectedField))
                .extracting(ex -> ((ServiceException) ex).getHttpStatus())
                .isEqualTo(expectedError.getHttpStatus());
    }


    @Test
    void shouldGetAllPersons() {
        // Given
        Person person1 = createPerson(1L, "Person 1", Color.BLUE, Color.BROWN, 70.0f, Country.USA);
        Person person2 = createPerson(2L, "Person 2", Color.GREEN, Color.BLACK, 65.5f, Country.UK);
        PersonDto dto1 = createPersonDto(1L, "Person 1", Color.BLUE, Color.BROWN, 70.0f, Country.USA);
        PersonDto dto2 = createPersonDto(2L, "Person 2", Color.GREEN, Color.BLACK, 65.5f, Country.UK);

        when(personRepository.findAll()).thenReturn(List.of(person1, person2));
        when(mapper.toDto(person1)).thenReturn(dto1);
        when(mapper.toDto(person2)).thenReturn(dto2);

        // When
        List<PersonDto> result = personService.getAll();

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).containsExactly(dto1, dto2);
    }

    @Test
    void shouldReturnEmptyListWhenNoPersons() {
        // Given
        when(personRepository.findAll()).thenReturn(List.of());

        // When
        List<PersonDto> result = personService.getAll();

        // Then
        assertThat(result).isEmpty();
    }


    @Test
    @SuppressWarnings("unchecked")
    void shouldUpdatePersonSuccessfully() {
        // Given
        Long personId = 1L;
        PersonRequest request = createPersonRequest("Updated Name", Color.GREEN, Color.BLACK, 85.0f, Country.UK, 2L);
        Person existingPerson = createPerson(personId, "Old Name", Color.BLUE, Color.BROWN, 70.0f, Country.USA);
        Location newLocation = Location.builder().id(2L).build();
        PersonDto updatedDto = createPersonDto(personId, "Updated Name", Color.GREEN, Color.BLACK, 85.0f, Country.UK);

        when(personRepository.findById(personId)).thenReturn(Optional.of(existingPerson));
        when(locationService.getEntity(2L)).thenReturn(newLocation);
        when(personRepository.save(existingPerson)).thenReturn(existingPerson);
        when(mapper.toDto(existingPerson)).thenReturn(updatedDto);

        // When
        PersonDto result = personService.update(personId, request);

        // Then
        assertThat(result).isEqualTo(updatedDto);
        verify(mapper).updateEntityFromRequest(request, existingPerson);
        assertThat(existingPerson.getLocation()).isEqualTo(newLocation);

        // Verify that save was called
        verify(personRepository).save(existingPerson);

        ArgumentCaptor<EntityEvent<PersonDto>> eventCaptor = ArgumentCaptor.forClass(EntityEvent.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());

        EntityEvent<PersonDto> capturedEvent = eventCaptor.getValue();
        assertThat(capturedEvent.getEventType()).isEqualTo(org.is.bandmanager.event.EventType.UPDATED);
        assertThat(capturedEvent.getEntities()).containsExactly(updatedDto);
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldDeletePersonSuccessfully() {
        // Given
        Long personId = 1L;
        Person person = createPerson(personId, "To Delete", Color.BLUE, Color.BROWN, 70.0f, Country.USA);
        PersonDto deletedDto = createPersonDto(personId, "To Delete", Color.BLUE, Color.BROWN, 70.0f, Country.USA);

        when(personRepository.findById(personId)).thenReturn(Optional.of(person));
        when(mapper.toDto(person)).thenReturn(deletedDto);

        // When
        PersonDto result = personService.delete(personId);

        // Then
        assertThat(result).isEqualTo(deletedDto);
        verify(personRepository).delete(person);

        ArgumentCaptor<EntityEvent<PersonDto>> eventCaptor = ArgumentCaptor.forClass(EntityEvent.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());

        EntityEvent<PersonDto> capturedEvent = eventCaptor.getValue();
        assertThat(capturedEvent.getEventType()).isEqualTo(org.is.bandmanager.event.EventType.DELETED);
        assertThat(capturedEvent.getEntities()).containsExactly(deletedDto);
    }


    @Test
    void shouldFindUnusedPersons() {
        // Given
        Person unusedPerson = createPerson(1L, "Unused", Color.BLUE, Color.BROWN, 70.0f, Country.USA);
        when(personRepository.findUnusedPersons()).thenReturn(List.of(unusedPerson));

        // When
        List<Person> result = personService.findUnusedEntities();

        // Then
        assertThat(result).containsExactly(unusedPerson);
    }

    @Test
    void shouldDeleteEntities() {
        // Given
        List<Person> persons = List.of(
                createPerson(1L, "Person 1", Color.BLUE, Color.BROWN, 70.0f, Country.USA),
                createPerson(2L, "Person 2", Color.GREEN, Color.BLACK, 65.5f, Country.UK)
        );

        // When
        personService.deleteEntities(persons);

        // Then
        verify(personRepository).deleteAll(persons);
    }

    @Test
    void shouldConvertToDto() {
        // Given
        Person person = createPerson(1L, "Test", Color.BLUE, Color.BROWN, 70.0f, Country.USA);
        PersonDto personDto = createPersonDto(1L, "Test", Color.BLUE, Color.BROWN, 70.0f, Country.USA);
        when(mapper.toDto(person)).thenReturn(personDto);

        // When
        List<PersonDto> result = personService.convertToDto(List.of(person));

        // Then
        assertThat(result).containsExactly(personDto);
    }

    @Test
    void shouldReturnEntityName() {
        assertThat(personService.getEntityName()).isEqualTo("Person");
    }


    private PersonRequest createPersonRequest(String name, Color eyeColor, Color hairColor, Float weight, Country nationality, Long locationId) {
        return PersonRequest.builder()
                .name(name)
                .eyeColor(eyeColor)
                .hairColor(hairColor)
                .weight(weight)
                .nationality(nationality)
                .locationId(locationId)
                .build();
    }

    private Person createPerson(Long id, String name, Color eyeColor, Color hairColor, Float weight, Country nationality) {
        return Person.builder()
                .id(id)
                .name(name)
                .eyeColor(eyeColor)
                .hairColor(hairColor)
                .location(testLocation)
                .weight(weight)
                .nationality(nationality)
                .build();
    }

    private PersonDto createPersonDto(Long id, String name, Color eyeColor, Color hairColor, Float weight, Country nationality) {
        return PersonDto.builder()
                .id(id)
                .name(name)
                .eyeColor(eyeColor)
                .hairColor(hairColor)
                .weight(weight)
                .nationality(nationality)
                .build();
    }

}