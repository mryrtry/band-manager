package org.is.bandmanager.service.coordinates;

import org.is.bandmanager.dto.CoordinatesDto;
import org.is.bandmanager.dto.CoordinatesMapper;
import org.is.bandmanager.dto.request.CoordinatesRequest;
import org.is.bandmanager.event.EntityEvent;
import org.is.bandmanager.exception.ServiceException;
import org.is.bandmanager.exception.message.ServiceErrorMessage;
import org.is.bandmanager.model.Coordinates;
import org.is.bandmanager.repository.CoordinatesRepository;
import org.is.bandmanager.repository.MusicBandRepository;
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
import static org.is.bandmanager.exception.message.ServiceErrorMessage.ENTITY_IN_USE;
import static org.is.bandmanager.exception.message.ServiceErrorMessage.ID_MUST_BE_POSITIVE;
import static org.is.bandmanager.exception.message.ServiceErrorMessage.MUST_BE_NOT_NULL;
import static org.is.bandmanager.exception.message.ServiceErrorMessage.SOURCE_NOT_FOUND;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CoordinatesServiceImplTest {

    @Mock
    private CoordinatesRepository coordinatesRepository;

    @Mock
    private MusicBandRepository musicBandRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private CoordinatesMapper mapper;

    @InjectMocks
    private CoordinatesServiceImpl coordinatesService;

    // ========== CREATE TESTS ==========

    @Test
    @SuppressWarnings("unchecked")
    void shouldCreateCoordinatesSuccessfully() {
        // Given
        CoordinatesRequest request = createCoordinatesRequest(10, 15.5f);
        Coordinates coordinates = createCoordinates(1L, 10, 15.5f);
        CoordinatesDto coordinatesDto = createCoordinatesDto(1L, 10, 15.5f);

        when(mapper.toEntity(request)).thenReturn(coordinates);
        when(coordinatesRepository.save(coordinates)).thenReturn(coordinates);
        when(mapper.toDto(coordinates)).thenReturn(coordinatesDto);

        // When
        CoordinatesDto result = coordinatesService.create(request);

        // Then
        assertThat(result).isEqualTo(coordinatesDto);

        ArgumentCaptor<EntityEvent<CoordinatesDto>> eventCaptor = ArgumentCaptor.forClass(EntityEvent.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());

        EntityEvent<CoordinatesDto> capturedEvent = eventCaptor.getValue();
        assertThat(capturedEvent.getEventType()).isEqualTo(org.is.bandmanager.event.EventType.CREATED);
        assertThat(capturedEvent.getEntities()).containsExactly(coordinatesDto);
    }

    @Test
    void shouldCreateCoordinatesWithNullY() {
        // Given
        CoordinatesRequest request = createCoordinatesRequest(10, null);
        Coordinates coordinates = createCoordinates(1L, 10, null);
        CoordinatesDto coordinatesDto = createCoordinatesDto(1L, 10, null);

        when(mapper.toEntity(request)).thenReturn(coordinates);
        when(coordinatesRepository.save(coordinates)).thenReturn(coordinates);
        when(mapper.toDto(coordinates)).thenReturn(coordinatesDto);

        // When
        CoordinatesDto result = coordinatesService.create(request);

        // Then
        assertThat(result).isEqualTo(coordinatesDto);
        assertThat(result.getY()).isNull();
    }

    // ========== GET TESTS ==========

    @Test
    void shouldGetCoordinatesById() {
        // Given
        Long coordinatesId = 1L;
        Coordinates coordinates = createCoordinates(coordinatesId, 10, 15.5f);
        CoordinatesDto coordinatesDto = createCoordinatesDto(coordinatesId, 10, 15.5f);

        when(coordinatesRepository.findById(coordinatesId)).thenReturn(Optional.of(coordinates));
        when(mapper.toDto(coordinates)).thenReturn(coordinatesDto);

        // When
        CoordinatesDto result = coordinatesService.get(coordinatesId);

        // Then
        assertThat(result).isEqualTo(coordinatesDto);
    }

    @Test
    void shouldGetCoordinatesEntity() {
        // Given
        Long coordinatesId = 1L;
        Coordinates coordinates = createCoordinates(coordinatesId, 10, 15.5f);

        when(coordinatesRepository.findById(coordinatesId)).thenReturn(Optional.of(coordinates));

        // When
        Coordinates result = coordinatesService.getEntity(coordinatesId);

        // Then
        assertThat(result).isEqualTo(coordinates);
    }

    @Test
    void shouldThrowExceptionWhenCoordinatesNotFound() {
        // Given
        Long coordinatesId = 999L;
        when(coordinatesRepository.findById(coordinatesId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> coordinatesService.get(coordinatesId))
                .isInstanceOf(ServiceException.class)
                .hasMessage(SOURCE_NOT_FOUND.getFormattedMessage("Coordinates", coordinatesId))
                .extracting(ex -> ((ServiceException) ex).getHttpStatus())
                .isEqualTo(SOURCE_NOT_FOUND.getHttpStatus());
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(longs = {0L, -1L})
    void shouldThrowExceptionWhenIdIsInvalid(Long id) {
        // When & Then
        ServiceErrorMessage expectedError = id == null ? MUST_BE_NOT_NULL : ID_MUST_BE_POSITIVE;
        String expectedField = "Coordinates.id";

        assertThatThrownBy(() -> coordinatesService.get(id))
                .isInstanceOf(ServiceException.class)
                .hasMessage(expectedError.getFormattedMessage(expectedField))
                .extracting(ex -> ((ServiceException) ex).getHttpStatus())
                .isEqualTo(expectedError.getHttpStatus());
    }

    // ========== GET ALL TESTS ==========

    @Test
    void shouldGetAllCoordinates() {
        // Given
        Coordinates coordinates1 = createCoordinates(1L, 10, 15.5f);
        Coordinates coordinates2 = createCoordinates(2L, 20, 25.5f);
        CoordinatesDto dto1 = createCoordinatesDto(1L, 10, 15.5f);
        CoordinatesDto dto2 = createCoordinatesDto(2L, 20, 25.5f);

        when(coordinatesRepository.findAll()).thenReturn(List.of(coordinates1, coordinates2));
        when(mapper.toDto(coordinates1)).thenReturn(dto1);
        when(mapper.toDto(coordinates2)).thenReturn(dto2);

        // When
        List<CoordinatesDto> result = coordinatesService.getAll();

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).containsExactly(dto1, dto2);
    }

    @Test
    void shouldReturnEmptyListWhenNoCoordinates() {
        // Given
        when(coordinatesRepository.findAll()).thenReturn(List.of());

        // When
        List<CoordinatesDto> result = coordinatesService.getAll();

        // Then
        assertThat(result).isEmpty();
    }

    // ========== UPDATE TESTS ==========

    @Test
    @SuppressWarnings("unchecked")
    void shouldUpdateCoordinatesSuccessfully() {
        // Given
        Long coordinatesId = 1L;
        CoordinatesRequest request = createCoordinatesRequest(50, 55.5f);
        Coordinates existingCoordinates = createCoordinates(coordinatesId, 10, 15.5f);
        CoordinatesDto updatedDto = createCoordinatesDto(coordinatesId, 50, 55.5f);

        when(coordinatesRepository.findById(coordinatesId)).thenReturn(Optional.of(existingCoordinates));
        when(coordinatesRepository.save(existingCoordinates)).thenReturn(existingCoordinates);
        when(mapper.toDto(existingCoordinates)).thenReturn(updatedDto);

        // When
        CoordinatesDto result = coordinatesService.update(coordinatesId, request);

        // Then
        assertThat(result).isEqualTo(updatedDto);
        verify(mapper).updateEntityFromRequest(request, existingCoordinates);

        ArgumentCaptor<EntityEvent<CoordinatesDto>> eventCaptor = ArgumentCaptor.forClass(EntityEvent.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());

        EntityEvent<CoordinatesDto> capturedEvent = eventCaptor.getValue();
        assertThat(capturedEvent.getEventType()).isEqualTo(org.is.bandmanager.event.EventType.UPDATED);
        assertThat(capturedEvent.getEntities()).containsExactly(updatedDto);
    }

    // ========== DELETE TESTS ==========

    @Test
    @SuppressWarnings("unchecked")
    void shouldDeleteCoordinatesSuccessfully() {
        // Given
        Long coordinatesId = 1L;
        Coordinates coordinates = createCoordinates(coordinatesId, 10, 15.5f);
        CoordinatesDto deletedDto = createCoordinatesDto(coordinatesId, 10, 15.5f);

        when(coordinatesRepository.findById(coordinatesId)).thenReturn(Optional.of(coordinates));
        when(musicBandRepository.existsByCoordinatesId(coordinatesId)).thenReturn(false);
        when(mapper.toDto(coordinates)).thenReturn(deletedDto);

        // When
        CoordinatesDto result = coordinatesService.delete(coordinatesId);

        // Then
        assertThat(result).isEqualTo(deletedDto);
        verify(coordinatesRepository).delete(coordinates);

        ArgumentCaptor<EntityEvent<CoordinatesDto>> eventCaptor = ArgumentCaptor.forClass(EntityEvent.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());

        EntityEvent<CoordinatesDto> capturedEvent = eventCaptor.getValue();
        assertThat(capturedEvent.getEventType()).isEqualTo(org.is.bandmanager.event.EventType.DELETED);
        assertThat(capturedEvent.getEntities()).containsExactly(deletedDto);
    }

    @Test
    void shouldThrowExceptionWhenDeletingCoordinatesInUse() {
        // Given
        Long coordinatesId = 1L;
        Coordinates coordinates = createCoordinates(coordinatesId, 10, 15.5f);

        when(coordinatesRepository.findById(coordinatesId)).thenReturn(Optional.of(coordinates));
        when(musicBandRepository.existsByCoordinatesId(coordinatesId)).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> coordinatesService.delete(coordinatesId))
                .isInstanceOf(ServiceException.class)
                .hasMessage(ENTITY_IN_USE.getFormattedMessage("Coordinates", coordinatesId, "MusicBand"))
                .extracting(ex -> ((ServiceException) ex).getHttpStatus())
                .isEqualTo(ENTITY_IN_USE.getHttpStatus());
    }

    // ========== CLEANUP STRATEGY TESTS ==========

    @Test
    void shouldFindUnusedCoordinates() {
        // Given
        Coordinates unusedCoordinates = createCoordinates(1L, 10, 15.5f);
        when(coordinatesRepository.findUnusedCoordinates()).thenReturn(List.of(unusedCoordinates));

        // When
        List<Coordinates> result = coordinatesService.findUnusedEntities();

        // Then
        assertThat(result).containsExactly(unusedCoordinates);
    }

    @Test
    void shouldDeleteEntities() {
        // Given
        List<Coordinates> coordinates = List.of(
                createCoordinates(1L, 10, 15.5f),
                createCoordinates(2L, 20, 25.5f)
        );

        // When
        coordinatesService.deleteEntities(coordinates);

        // Then
        verify(coordinatesRepository).deleteAll(coordinates);
    }

    @Test
    void shouldConvertToDto() {
        // Given
        Coordinates coordinates = createCoordinates(1L, 10, 15.5f);
        CoordinatesDto coordinatesDto = createCoordinatesDto(1L, 10, 15.5f);
        when(mapper.toDto(coordinates)).thenReturn(coordinatesDto);

        // When
        List<CoordinatesDto> result = coordinatesService.convertToDto(List.of(coordinates));

        // Then
        assertThat(result).containsExactly(coordinatesDto);
    }

    @Test
    void shouldReturnEntityName() {
        assertThat(coordinatesService.getEntityName()).isEqualTo("Coordinates");
    }

    // ========== HELPER METHODS ==========

    private CoordinatesRequest createCoordinatesRequest(Integer x, Float y) {
        return CoordinatesRequest.builder()
                .x(x)
                .y(y)
                .build();
    }

    private Coordinates createCoordinates(Long id, Integer x, Float y) {
        return Coordinates.builder()
                .id(id)
                .x(x)
                .y(y)
                .build();
    }

    private CoordinatesDto createCoordinatesDto(Long id, Integer x, Float y) {
        return CoordinatesDto.builder()
                .id(id)
                .x(x)
                .y(y)
                .build();
    }

}