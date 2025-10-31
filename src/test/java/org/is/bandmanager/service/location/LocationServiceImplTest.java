package org.is.bandmanager.service.location;

import org.is.bandmanager.dto.LocationDto;
import org.is.bandmanager.dto.LocationMapper;
import org.is.bandmanager.dto.request.LocationRequest;
import org.is.bandmanager.event.EntityEvent;
import org.is.exception.ServiceException;
import org.is.exception.message.BandManagerErrorMessage;
import org.is.bandmanager.model.Location;
import org.is.bandmanager.repository.LocationRepository;
import org.is.bandmanager.repository.PersonRepository;
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
import static org.is.exception.message.BandManagerErrorMessage.ENTITY_IN_USE;
import static org.is.exception.message.BandManagerErrorMessage.ID_MUST_BE_POSITIVE;
import static org.is.exception.message.BandManagerErrorMessage.MUST_BE_NOT_NULL;
import static org.is.exception.message.BandManagerErrorMessage.SOURCE_NOT_FOUND;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LocationServiceImplTest {

    @Mock
    private LocationRepository locationRepository;

    @Mock
    private PersonRepository personRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private LocationMapper mapper;

    @InjectMocks
    private LocationServiceImpl locationService;

    @Test
    @SuppressWarnings("unchecked")
    void shouldCreateLocationSuccessfully() {
        // Given
        LocationRequest request = createLocationRequest(10, 15L, 20L);
        Location location = createLocation(1L, 10, 15L, 20L);
        LocationDto locationDto = createLocationDto(1L, 10, 15L, 20L);

        when(mapper.toEntity(request)).thenReturn(location);
        when(locationRepository.save(location)).thenReturn(location);
        when(mapper.toDto(location)).thenReturn(locationDto);

        // When
        LocationDto result = locationService.create(request);

        // Then
        assertThat(result).isEqualTo(locationDto);

        ArgumentCaptor<EntityEvent<LocationDto>> eventCaptor = ArgumentCaptor.forClass(EntityEvent.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());

        EntityEvent<LocationDto> capturedEvent = eventCaptor.getValue();
        assertThat(capturedEvent.getEventType()).isEqualTo(org.is.bandmanager.event.EventType.CREATED);
        assertThat(capturedEvent.getEntities()).containsExactly(locationDto);
    }

    @Test
    void shouldCreateLocationWithNullX() {
        // Given
        LocationRequest request = createLocationRequest(null, 15L, 20L);
        Location location = createLocation(1L, null, 15L, 20L);
        LocationDto locationDto = createLocationDto(1L, null, 15L, 20L);

        when(mapper.toEntity(request)).thenReturn(location);
        when(locationRepository.save(location)).thenReturn(location);
        when(mapper.toDto(location)).thenReturn(locationDto);

        // When
        LocationDto result = locationService.create(request);

        // Then
        assertThat(result).isEqualTo(locationDto);
        assertThat(result.getX()).isNull();
    }


    @Test
    void shouldGetLocationById() {
        // Given
        Long locationId = 1L;
        Location location = createLocation(locationId, 10, 15L, 20L);
        LocationDto locationDto = createLocationDto(locationId, 10, 15L, 20L);

        when(locationRepository.findById(locationId)).thenReturn(Optional.of(location));
        when(mapper.toDto(location)).thenReturn(locationDto);

        // When
        LocationDto result = locationService.get(locationId);

        // Then
        assertThat(result).isEqualTo(locationDto);
    }

    @Test
    void shouldGetLocationEntity() {
        // Given
        Long locationId = 1L;
        Location location = createLocation(locationId, 10, 15L, 20L);

        when(locationRepository.findById(locationId)).thenReturn(Optional.of(location));

        // When
        Location result = locationService.getEntity(locationId);

        // Then
        assertThat(result).isEqualTo(location);
    }

    @Test
    void shouldThrowExceptionWhenLocationNotFound() {
        // Given
        Long locationId = 999L;
        when(locationRepository.findById(locationId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> locationService.get(locationId))
                .isInstanceOf(ServiceException.class)
                .hasMessage(SOURCE_NOT_FOUND.getFormattedMessage("Location", locationId))
                .extracting(ex -> ((ServiceException) ex).getHttpStatus())
                .isEqualTo(SOURCE_NOT_FOUND.getHttpStatus());
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(longs = {0L, -1L})
    void shouldThrowExceptionWhenIdIsInvalid(Long id) {
        // When & Then
        BandManagerErrorMessage expectedError = id == null ? MUST_BE_NOT_NULL : ID_MUST_BE_POSITIVE;
        String expectedField = "Location.id";

        assertThatThrownBy(() -> locationService.get(id))
                .isInstanceOf(ServiceException.class)
                .hasMessage(expectedError.getFormattedMessage(expectedField))
                .extracting(ex -> ((ServiceException) ex).getHttpStatus())
                .isEqualTo(expectedError.getHttpStatus());
    }


    @Test
    void shouldGetAllLocations() {
        // Given
        Location location1 = createLocation(1L, 10, 15L, 20L);
        Location location2 = createLocation(2L, 20, 25L, 30L);
        LocationDto dto1 = createLocationDto(1L, 10, 15L, 20L);
        LocationDto dto2 = createLocationDto(2L, 20, 25L, 30L);

        when(locationRepository.findAll()).thenReturn(List.of(location1, location2));
        when(mapper.toDto(location1)).thenReturn(dto1);
        when(mapper.toDto(location2)).thenReturn(dto2);

        // When
        List<LocationDto> result = locationService.getAll();

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).containsExactly(dto1, dto2);
    }

    @Test
    void shouldReturnEmptyListWhenNoLocations() {
        // Given
        when(locationRepository.findAll()).thenReturn(List.of());

        // When
        List<LocationDto> result = locationService.getAll();

        // Then
        assertThat(result).isEmpty();
    }


    @Test
    @SuppressWarnings("unchecked")
    void shouldUpdateLocationSuccessfully() {
        // Given
        Long locationId = 1L;
        LocationRequest request = createLocationRequest(50, 55L, 60L);
        Location existingLocation = createLocation(locationId, 10, 15L, 20L);
        LocationDto updatedDto = createLocationDto(locationId, 50, 55L, 60L);

        when(locationRepository.findById(locationId)).thenReturn(Optional.of(existingLocation));
        when(locationRepository.save(existingLocation)).thenReturn(existingLocation);
        when(mapper.toDto(existingLocation)).thenReturn(updatedDto);

        // When
        LocationDto result = locationService.update(locationId, request);

        // Then
        assertThat(result).isEqualTo(updatedDto);
        verify(mapper).updateEntityFromRequest(request, existingLocation);

        ArgumentCaptor<EntityEvent<LocationDto>> eventCaptor = ArgumentCaptor.forClass(EntityEvent.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());

        EntityEvent<LocationDto> capturedEvent = eventCaptor.getValue();
        assertThat(capturedEvent.getEventType()).isEqualTo(org.is.bandmanager.event.EventType.UPDATED);
        assertThat(capturedEvent.getEntities()).containsExactly(updatedDto);
    }


    @Test
    @SuppressWarnings("unchecked")
    void shouldDeleteLocationSuccessfully() {
        // Given
        Long locationId = 1L;
        Location location = createLocation(locationId, 10, 15L, 20L);
        LocationDto deletedDto = createLocationDto(locationId, 10, 15L, 20L);

        when(locationRepository.findById(locationId)).thenReturn(Optional.of(location));
        when(personRepository.existsByLocationId(locationId)).thenReturn(false);
        when(mapper.toDto(location)).thenReturn(deletedDto);

        // When
        LocationDto result = locationService.delete(locationId);

        // Then
        assertThat(result).isEqualTo(deletedDto);
        verify(locationRepository).delete(location);

        ArgumentCaptor<EntityEvent<LocationDto>> eventCaptor = ArgumentCaptor.forClass(EntityEvent.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());

        EntityEvent<LocationDto> capturedEvent = eventCaptor.getValue();
        assertThat(capturedEvent.getEventType()).isEqualTo(org.is.bandmanager.event.EventType.DELETED);
        assertThat(capturedEvent.getEntities()).containsExactly(deletedDto);
    }

    @Test
    void shouldThrowExceptionWhenDeletingLocationInUse() {
        // Given
        Long locationId = 1L;
        Location location = createLocation(locationId, 10, 15L, 20L);

        when(locationRepository.findById(locationId)).thenReturn(Optional.of(location));
        when(personRepository.existsByLocationId(locationId)).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> locationService.delete(locationId))
                .isInstanceOf(ServiceException.class)
                .hasMessage(ENTITY_IN_USE.getFormattedMessage("Location", locationId, "Person"))
                .extracting(ex -> ((ServiceException) ex).getHttpStatus())
                .isEqualTo(ENTITY_IN_USE.getHttpStatus());
    }


    @Test
    void shouldFindUnusedLocations() {
        // Given
        Location unusedLocation = createLocation(1L, 10, 15L, 20L);
        when(locationRepository.findUnusedLocations()).thenReturn(List.of(unusedLocation));

        // When
        List<Location> result = locationService.findUnusedEntities();

        // Then
        assertThat(result).containsExactly(unusedLocation);
    }

    @Test
    void shouldDeleteEntities() {
        // Given
        List<Location> locations = List.of(
                createLocation(1L, 10, 15L, 20L),
                createLocation(2L, 20, 25L, 30L)
        );

        // When
        locationService.deleteEntities(locations);

        // Then
        verify(locationRepository).deleteAll(locations);
    }

    @Test
    void shouldConvertToDto() {
        // Given
        Location location = createLocation(1L, 10, 15L, 20L);
        LocationDto locationDto = createLocationDto(1L, 10, 15L, 20L);
        when(mapper.toDto(location)).thenReturn(locationDto);

        // When
        List<LocationDto> result = locationService.convertToDto(List.of(location));

        // Then
        assertThat(result).containsExactly(locationDto);
    }

    @Test
    void shouldReturnEntityName() {
        assertThat(locationService.getEntityName()).isEqualTo("Location");
    }


    private LocationRequest createLocationRequest(Integer x, Long y, Long z) {
        return LocationRequest.builder()
                .x(x)
                .y(y)
                .z(z)
                .build();
    }

    private Location createLocation(Long id, Integer x, Long y, Long z) {
        return Location.builder()
                .id(id)
                .x(x)
                .y(y)
                .z(z)
                .build();
    }

    private LocationDto createLocationDto(Long id, Integer x, Long y, Long z) {
        return LocationDto.builder()
                .id(id)
                .x(x)
                .y(y)
                .z(z)
                .build();
    }
}