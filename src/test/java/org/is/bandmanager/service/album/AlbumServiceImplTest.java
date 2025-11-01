package org.is.bandmanager.service.album;

import org.is.bandmanager.dto.AlbumDto;
import org.is.bandmanager.dto.AlbumMapper;
import org.is.bandmanager.dto.request.AlbumRequest;
import org.is.event.EntityEvent;
import org.is.event.EventType;
import org.is.exception.ServiceException;
import org.is.exception.message.BandManagerErrorMessage;
import org.is.bandmanager.model.Album;
import org.is.bandmanager.repository.AlbumRepository;
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
import static org.is.exception.message.BandManagerErrorMessage.ENTITY_IN_USE;
import static org.is.exception.message.BandManagerErrorMessage.ID_MUST_BE_POSITIVE;
import static org.is.exception.message.BandManagerErrorMessage.MUST_BE_NOT_NULL;
import static org.is.exception.message.BandManagerErrorMessage.SOURCE_WITH_ID_NOT_FOUND;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AlbumServiceImplTest {

    @Mock
    private AlbumRepository albumRepository;

    @Mock
    private MusicBandRepository musicBandRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private AlbumMapper mapper;

    @InjectMocks
    private AlbumServiceImpl albumService;

    @Test
    @SuppressWarnings("unchecked")
    void shouldCreateAlbumSuccessfully() {
        // Given
        AlbumRequest request = createAlbumRequest("New Album", 10L, 100);
        Album album = createAlbum(1L, "New Album", 10L, 100);
        AlbumDto albumDto = createAlbumDto(1L, "New Album", 10L, 100);

        when(mapper.toEntity(request)).thenReturn(album);
        when(albumRepository.save(album)).thenReturn(album);
        when(mapper.toDto(album)).thenReturn(albumDto);

        // When
        AlbumDto result = albumService.create(request);

        // Then
        assertThat(result).isEqualTo(albumDto);

        // Проверяем событие через ArgumentCaptor
        ArgumentCaptor<EntityEvent<AlbumDto>> eventCaptor = ArgumentCaptor.forClass(EntityEvent.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());

        EntityEvent<AlbumDto> capturedEvent = eventCaptor.getValue();
        assertThat(capturedEvent.getEventType()).isEqualTo(EventType.CREATED);
        assertThat(capturedEvent.getEntities()).containsExactly(albumDto);
    }

    @Test
    void shouldGetAlbumById() {
        // Given
        Long albumId = 1L;
        Album album = createAlbum(albumId, "Test Album", 5L, 50);
        AlbumDto albumDto = createAlbumDto(albumId, "Test Album", 5L, 50);

        when(albumRepository.findById(albumId)).thenReturn(Optional.of(album));
        when(mapper.toDto(album)).thenReturn(albumDto);

        // When
        AlbumDto result = albumService.get(albumId);

        // Then
        assertThat(result).isEqualTo(albumDto);
    }

    @Test
    void shouldGetAlbumEntity() {
        // Given
        Long albumId = 1L;
        Album album = createAlbum(albumId, "Test Album", 5L, 50);

        when(albumRepository.findById(albumId)).thenReturn(Optional.of(album));

        // When
        Album result = albumService.getEntity(albumId);

        // Then
        assertThat(result).isEqualTo(album);
    }

    @Test
    void shouldThrowExceptionWhenAlbumNotFound() {
        // Given
        Long albumId = 999L;
        when(albumRepository.findById(albumId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> albumService.get(albumId))
                .isInstanceOf(ServiceException.class)
                .hasMessage(SOURCE_WITH_ID_NOT_FOUND.getFormattedMessage("Album", albumId))
                .extracting(ex -> ((ServiceException) ex).getHttpStatus())
                .isEqualTo(SOURCE_WITH_ID_NOT_FOUND.getHttpStatus());
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(longs = {0L, -1L})
    void shouldThrowExceptionWhenIdIsInvalid(Long id) {
        // When & Then
        BandManagerErrorMessage expectedError = id == null ? MUST_BE_NOT_NULL : ID_MUST_BE_POSITIVE;
        String expectedField = "Album.id";

        assertThatThrownBy(() -> albumService.get(id))
                .isInstanceOf(ServiceException.class)
                .hasMessage(expectedError.getFormattedMessage(expectedField))
                .extracting(ex -> ((ServiceException) ex).getHttpStatus())
                .isEqualTo(expectedError.getHttpStatus());
    }

    @Test
    void shouldGetAllAlbums() {
        // Given
        Album album1 = createAlbum(1L, "Album 1", 5L, 50);
        Album album2 = createAlbum(2L, "Album 2", 8L, 80);
        AlbumDto dto1 = createAlbumDto(1L, "Album 1", 5L, 50);
        AlbumDto dto2 = createAlbumDto(2L, "Album 2", 8L, 80);

        when(albumRepository.findAll()).thenReturn(List.of(album1, album2));
        when(mapper.toDto(album1)).thenReturn(dto1);
        when(mapper.toDto(album2)).thenReturn(dto2);

        // When
        List<AlbumDto> result = albumService.getAll();

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).containsExactly(dto1, dto2);
    }

    @Test
    void shouldReturnEmptyListWhenNoAlbums() {
        // Given
        when(albumRepository.findAll()).thenReturn(List.of());

        // When
        List<AlbumDto> result = albumService.getAll();

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldUpdateAlbumSuccessfully() {
        // Given
        Long albumId = 1L;
        AlbumRequest request = createAlbumRequest("Updated Name", 9L, 99);
        Album existingAlbum = createAlbum(albumId, "Old Name", 5L, 50);
        AlbumDto updatedDto = createAlbumDto(albumId, "Updated Name", 9L, 99);

        when(albumRepository.findById(albumId)).thenReturn(Optional.of(existingAlbum));
        when(albumRepository.save(existingAlbum)).thenReturn(existingAlbum);
        when(mapper.toDto(existingAlbum)).thenReturn(updatedDto);

        // When
        AlbumDto result = albumService.update(albumId, request);

        // Then
        assertThat(result).isEqualTo(updatedDto);
        verify(mapper).updateEntityFromRequest(request, existingAlbum);

        // Проверяем событие через ArgumentCaptor
        ArgumentCaptor<EntityEvent<AlbumDto>> eventCaptor = ArgumentCaptor.forClass(EntityEvent.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());

        EntityEvent<AlbumDto> capturedEvent = eventCaptor.getValue();
        assertThat(capturedEvent.getEventType()).isEqualTo(EventType.UPDATED);
        assertThat(capturedEvent.getEntities()).containsExactly(updatedDto);
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldDeleteAlbumSuccessfully() {
        // Given
        Long albumId = 1L;
        Album album = createAlbum(albumId, "To Delete", 5L, 50);
        AlbumDto deletedDto = createAlbumDto(albumId, "To Delete", 5L, 50);

        when(albumRepository.findById(albumId)).thenReturn(Optional.of(album));
        when(mapper.toDto(album)).thenReturn(deletedDto);
        when(musicBandRepository.existsByBestAlbumId(albumId)).thenReturn(false);

        // When
        AlbumDto result = albumService.delete(albumId);

        // Then
        assertThat(result).isEqualTo(deletedDto);
        verify(albumRepository).delete(album);

        // Проверяем событие через ArgumentCaptor
        ArgumentCaptor<EntityEvent<AlbumDto>> eventCaptor = ArgumentCaptor.forClass(EntityEvent.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());

        EntityEvent<AlbumDto> capturedEvent = eventCaptor.getValue();
        assertThat(capturedEvent.getEventType()).isEqualTo(EventType.DELETED);
        assertThat(capturedEvent.getEntities()).containsExactly(deletedDto);
    }

    @Test
    void shouldThrowExceptionWhenDeletingAlbumInUse() {
        // Given
        Long albumId = 1L;
        Album album = createAlbum(albumId, "In Use", 5L, 50);

        when(albumRepository.findById(albumId)).thenReturn(Optional.of(album));
        when(musicBandRepository.existsByBestAlbumId(albumId)).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> albumService.delete(albumId))
                .isInstanceOf(ServiceException.class)
                .hasMessage(ENTITY_IN_USE.getFormattedMessage("Album", albumId, "MusicBand"))
                .extracting(ex -> ((ServiceException) ex).getHttpStatus())
                .isEqualTo(ENTITY_IN_USE.getHttpStatus());

        verify(albumRepository, never()).delete(any());
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    void shouldFindUnusedAlbums() {
        // Given
        Album unusedAlbum = createAlbum(1L, "Unused", 5L, 50);
        when(albumRepository.findUnusedAlbum()).thenReturn(List.of(unusedAlbum));

        // When
        List<Album> result = albumService.findUnusedEntities();

        // Then
        assertThat(result).containsExactly(unusedAlbum);
    }

    @Test
    void shouldDeleteEntities() {
        // Given
        List<Album> albums = List.of(
                createAlbum(1L, "Album 1", 5L, 50),
                createAlbum(2L, "Album 2", 8L, 80)
        );

        // When
        albumService.deleteEntities(albums);

        // Then
        verify(albumRepository).deleteAll(albums);
    }

    @Test
    void shouldConvertToDto() {
        // Given
        Album album = createAlbum(1L, "Test", 5L, 50);
        AlbumDto albumDto = createAlbumDto(1L, "Test", 5L, 50);
        when(mapper.toDto(album)).thenReturn(albumDto);

        // When
        List<AlbumDto> result = albumService.convertToDto(List.of(album));

        // Then
        assertThat(result).containsExactly(albumDto);
    }

    @Test
    void shouldReturnEntityName() {
        assertThat(albumService.getEntityName()).isEqualTo("Album");
    }

    private AlbumRequest createAlbumRequest(String name, Long tracks, Integer sales) {
        return AlbumRequest.builder()
                .name(name)
                .tracks(tracks)
                .sales(sales)
                .build();
    }

    private Album createAlbum(Long id, String name, Long tracks, Integer sales) {
        return Album.builder()
                .id(id)
                .name(name)
                .tracks(tracks)
                .sales(sales)
                .build();
    }

    private AlbumDto createAlbumDto(Long id, String name, Long tracks, Integer sales) {
        return AlbumDto.builder()
                .id(id)
                .name(name)
                .tracks(tracks)
                .sales(sales)
                .build();
    }

}