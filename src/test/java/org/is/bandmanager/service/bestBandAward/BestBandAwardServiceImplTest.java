package org.is.bandmanager.service.bestBandAward;

import org.is.bandmanager.dto.BestBandAwardDto;
import org.is.bandmanager.dto.BestBandAwardMapper;
import org.is.bandmanager.dto.request.BestBandAwardRequest;
import org.is.event.EntityEvent;
import org.is.event.EventType;
import org.is.exception.ServiceException;
import org.is.exception.message.BandManagerErrorMessage;
import org.is.bandmanager.model.BestBandAward;
import org.is.bandmanager.model.MusicBand;
import org.is.bandmanager.model.MusicGenre;
import org.is.bandmanager.repository.BestBandAwardRepository;
import org.is.bandmanager.repository.filter.BestBandAwardFilter;
import org.is.bandmanager.service.musicBand.MusicBandService;
import org.is.bandmanager.util.pageable.PageableConfig;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.is.exception.message.BandManagerErrorMessage.ID_MUST_BE_POSITIVE;
import static org.is.exception.message.BandManagerErrorMessage.MUST_BE_NOT_NULL;
import static org.is.exception.message.BandManagerErrorMessage.SOURCE_WITH_ID_NOT_FOUND;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BestBandAwardServiceImplTest {

    private final MusicBand testBand = MusicBand.builder()
            .id(1)
            .name("Test Band")
            .build();

    @Mock
    private BestBandAwardRepository bestBandAwardRepository;

    @Mock
    private MusicBandService musicBandService;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private BestBandAwardMapper mapper;

    @InjectMocks
    private BestBandAwardServiceImpl bestBandAwardService;

    @Test
    @SuppressWarnings("unchecked")
    void shouldCreateBestBandAwardSuccessfully() {
        // Given
        BestBandAwardRequest request = createBestBandAwardRequest(MusicGenre.ROCK);
        BestBandAward award = createBestBandAward(1L, testBand, MusicGenre.ROCK, LocalDateTime.now());
        BestBandAwardDto awardDto = createBestBandAwardDto(1L, MusicGenre.ROCK, LocalDateTime.now());

        when(mapper.toEntity(request)).thenReturn(award);
        when(musicBandService.getEntity(1)).thenReturn(testBand);
        when(bestBandAwardRepository.save(award)).thenReturn(award);
        when(mapper.toDto(award)).thenReturn(awardDto);

        // When
        BestBandAwardDto result = bestBandAwardService.create(request);

        // Then
        assertThat(result).isEqualTo(awardDto);
        assertThat(award.getBand()).isEqualTo(testBand);

        ArgumentCaptor<EntityEvent<BestBandAwardDto>> eventCaptor = ArgumentCaptor.forClass(EntityEvent.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());

        EntityEvent<BestBandAwardDto> capturedEvent = eventCaptor.getValue();
        assertThat(capturedEvent.getEventType()).isEqualTo(EventType.CREATED);
        assertThat(capturedEvent.getEntities()).containsExactly(awardDto);
    }

    @Test
    void shouldGetAllBestBandAwardsWithFilterAndPagination() {
        // Given
        BestBandAwardFilter filter = BestBandAwardFilter.builder().build();
        filter.setGenre(MusicGenre.ROCK);

        PageableConfig config = new PageableConfig();
        config.setPage(0);
        config.setSize(10);
        config.setSort(List.of("createdAt"));
        config.setDirection("DESC");

        BestBandAward award = createBestBandAward(1L, testBand, MusicGenre.ROCK, LocalDateTime.now());
        BestBandAwardDto awardDto = createBestBandAwardDto(1L, MusicGenre.ROCK, LocalDateTime.now());
        Page<BestBandAward> awardPage = new PageImpl<>(List.of(award));

        when(bestBandAwardRepository.findWithFilter(any(BestBandAwardFilter.class), any(Pageable.class)))
                .thenReturn(awardPage);
        when(mapper.toDto(award)).thenReturn(awardDto);

        // When
        Page<BestBandAwardDto> result = bestBandAwardService.getAll(filter, config);

        // Then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0)).isEqualTo(awardDto);
    }

    @Test
    void shouldGetAllBestBandAwardsWithNullFilter() {
        // Given
        PageableConfig config = new PageableConfig();

        BestBandAward award = createBestBandAward(1L, testBand, MusicGenre.ROCK, LocalDateTime.now());
        BestBandAwardDto awardDto = createBestBandAwardDto(1L, MusicGenre.ROCK, LocalDateTime.now());
        Page<BestBandAward> awardPage = new PageImpl<>(List.of(award));

        // ИСПРАВЛЕННАЯ СТРОКА - используем isNull() вместо any()
        when(bestBandAwardRepository.findWithFilter(isNull(), any(Pageable.class)))
                .thenReturn(awardPage);
        when(mapper.toDto(award)).thenReturn(awardDto);

        // When
        Page<BestBandAwardDto> result = bestBandAwardService.getAll(null, config);

        // Then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0)).isEqualTo(awardDto);
    }

    @Test
    void shouldReturnEmptyPageWhenNoBestBandAwards() {
        // Given
        BestBandAwardFilter filter = BestBandAwardFilter.builder().build();
        PageableConfig config = new PageableConfig();
        Page<BestBandAward> emptyPage = Page.empty();

        when(bestBandAwardRepository.findWithFilter(any(BestBandAwardFilter.class), any(Pageable.class)))
                .thenReturn(emptyPage);

        // When
        Page<BestBandAwardDto> result = bestBandAwardService.getAll(filter, config);

        // Then
        assertThat(result.getContent()).isEmpty();
    }

    @Test
    void shouldReturnEmptyListWhenEmptyIds() {
        // When
        List<BestBandAwardDto> result = bestBandAwardService.delete(List.of());

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void shouldReturnEmptyListWhenNullIds() {
        // When
        List<BestBandAwardDto> result = bestBandAwardService.delete((List<Long>) null);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void shouldGetBestBandAwardById() {
        // Given
        Long awardId = 1L;
        BestBandAward award = createBestBandAward(awardId, testBand, MusicGenre.ROCK, LocalDateTime.now());
        BestBandAwardDto awardDto = createBestBandAwardDto(awardId, MusicGenre.ROCK, LocalDateTime.now());

        when(bestBandAwardRepository.findById(awardId)).thenReturn(Optional.of(award));
        when(mapper.toDto(award)).thenReturn(awardDto);

        // When
        BestBandAwardDto result = bestBandAwardService.get(awardId);

        // Then
        assertThat(result).isEqualTo(awardDto);
    }

    @Test
    void shouldThrowExceptionWhenBestBandAwardNotFound() {
        // Given
        Long awardId = 999L;
        when(bestBandAwardRepository.findById(awardId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> bestBandAwardService.get(awardId))
                .isInstanceOf(ServiceException.class)
                .hasMessage(SOURCE_WITH_ID_NOT_FOUND.getFormattedMessage("BestBandAward", awardId))
                .extracting(ex -> ((ServiceException) ex).getHttpStatus())
                .isEqualTo(SOURCE_WITH_ID_NOT_FOUND.getHttpStatus());
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(longs = {0L, -1L})
    void shouldThrowExceptionWhenIdIsInvalid(Long id) {
        // When & Then
        BandManagerErrorMessage expectedError = id == null ? MUST_BE_NOT_NULL : ID_MUST_BE_POSITIVE;
        String expectedField = "BestBandAward.id";

        assertThatThrownBy(() -> bestBandAwardService.get(id))
                .isInstanceOf(ServiceException.class)
                .hasMessage(expectedError.getFormattedMessage(expectedField))
                .extracting(ex -> ((ServiceException) ex).getHttpStatus())
                .isEqualTo(expectedError.getHttpStatus());
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldUpdateBestBandAwardSuccessfully() {
        // Given
        Long awardId = 1L;
        BestBandAwardRequest request = createBestBandAwardRequest(MusicGenre.POST_PUNK);
        BestBandAward existingAward = createBestBandAward(awardId, testBand, MusicGenre.ROCK, LocalDateTime.now());
        BestBandAwardDto updatedDto = createBestBandAwardDto(awardId, MusicGenre.POST_PUNK, LocalDateTime.now());

        when(bestBandAwardRepository.findById(awardId)).thenReturn(Optional.of(existingAward));
        when(musicBandService.getEntity(1)).thenReturn(testBand);
        when(bestBandAwardRepository.save(existingAward)).thenReturn(existingAward);
        when(mapper.toDto(existingAward)).thenReturn(updatedDto);

        // When
        BestBandAwardDto result = bestBandAwardService.update(awardId, request);

        // Then
        assertThat(result).isEqualTo(updatedDto);
        verify(mapper).updateEntityFromRequest(request, existingAward);
        assertThat(existingAward.getBand()).isEqualTo(testBand);

        ArgumentCaptor<EntityEvent<BestBandAwardDto>> eventCaptor = ArgumentCaptor.forClass(EntityEvent.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());

        EntityEvent<BestBandAwardDto> capturedEvent = eventCaptor.getValue();
        assertThat(capturedEvent.getEventType()).isEqualTo(EventType.UPDATED);
        assertThat(capturedEvent.getEntities()).containsExactly(updatedDto);
    }

    @Test
    void shouldDeleteBestBandAwardSuccessfully() {
        // Given
        Long awardId = 1L;
        BestBandAward award = createBestBandAward(awardId, testBand, MusicGenre.ROCK, LocalDateTime.now());
        BestBandAwardDto deletedDto = createBestBandAwardDto(awardId, MusicGenre.ROCK, LocalDateTime.now());

        when(bestBandAwardRepository.findById(awardId)).thenReturn(Optional.of(award));
        when(mapper.toDto(award)).thenReturn(deletedDto);

        // When
        BestBandAwardDto result = bestBandAwardService.delete(awardId);

        // Then
        assertThat(result).isEqualTo(deletedDto);
        verify(bestBandAwardRepository).delete(award);
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldDeleteMultipleBestBandAwardsSuccessfully() {
        // Given
        List<Long> awardIds = List.of(1L, 2L);
        BestBandAward award1 = createBestBandAward(1L, testBand, MusicGenre.ROCK, LocalDateTime.now());
        BestBandAward award2 = createBestBandAward(2L, testBand, MusicGenre.POST_ROCK, LocalDateTime.now());
        List<BestBandAward> awards = List.of(award1, award2);

        BestBandAwardDto dto1 = createBestBandAwardDto(1L, MusicGenre.ROCK, LocalDateTime.now());
        BestBandAwardDto dto2 = createBestBandAwardDto(2L, MusicGenre.POST_ROCK, LocalDateTime.now());

        when(bestBandAwardRepository.findAllById(awardIds)).thenReturn(awards);
        when(mapper.toDto(award1)).thenReturn(dto1);
        when(mapper.toDto(award2)).thenReturn(dto2);

        // When
        List<BestBandAwardDto> result = bestBandAwardService.delete(awardIds);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).containsExactly(dto1, dto2);
        verify(bestBandAwardRepository).deleteAll(awards);

        ArgumentCaptor<EntityEvent<BestBandAwardDto>> eventCaptor = ArgumentCaptor.forClass(EntityEvent.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());

        EntityEvent<BestBandAwardDto> capturedEvent = eventCaptor.getValue();
        assertThat(capturedEvent.getEventType()).isEqualTo(EventType.BULK_DELETED);
        assertThat(capturedEvent.getEntities()).containsExactly(dto1, dto2);
    }

    @Test
    void shouldReturnEmptyListWhenDeletingEmptyIds() {
        // When
        List<BestBandAwardDto> result = bestBandAwardService.delete(List.of());

        // Then
        assertThat(result).isEmpty();
    }

    private BestBandAwardRequest createBestBandAwardRequest(MusicGenre genre) {
        return BestBandAwardRequest.builder()
                .musicBandId(1)
                .genre(genre)
                .build();
    }

    private BestBandAward createBestBandAward(Long id, MusicBand band, MusicGenre genre, LocalDateTime createdAt) {
        return BestBandAward.builder()
                .id(id)
                .band(band)
                .genre(genre)
                .createdAt(createdAt)
                .build();
    }

    private BestBandAwardDto createBestBandAwardDto(Long id, MusicGenre genre, LocalDateTime createdAt) {
        return BestBandAwardDto.builder()
                .id(id)
                .bandId(1L)
                .bandName("Test Band")
                .genre(genre)
                .createdAt(createdAt)
                .build();
    }

}