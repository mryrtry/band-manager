package org.is.bandmanager.service.subscription.storage;

import org.is.bandmanager.model.MusicGenre;
import org.is.bandmanager.repository.filter.BestBandAwardFilter;
import org.is.bandmanager.repository.filter.MusicBandFilter;
import org.is.bandmanager.service.subscription.model.Subscription;
import org.is.bandmanager.service.subscription.model.request.SubscriptionRequest;
import org.is.bandmanager.util.pageable.PageableConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class InMemorySubscriptionStorageTest {

    private static final String PRINCIPAL_ID = "test-user-123";
    private static final String ANOTHER_PRINCIPAL_ID = "another-user-456";
    private InMemorySubscriptionStorage storage;

    @BeforeEach
    void setUp() {
        storage = new InMemorySubscriptionStorage();
    }

    @Test
    void shouldCreateSubscriptionSuccessfully() {
        // Given
        SubscriptionRequest<BestBandAwardFilter> request = createBestBandAwardSubscriptionRequest();

        // When
        Subscription<BestBandAwardFilter> result = storage.createSubscription(PRINCIPAL_ID, request);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getSubscriptionId()).isNotNull();
        assertThat(result.getPrincipalId()).isEqualTo(PRINCIPAL_ID);
        assertThat(result.getFilter().getGenre()).isEqualTo(MusicGenre.ROCK);
        assertThat(result.getFilter().getBandName()).isEqualTo("Test Band");
        assertThat(result.getPageableConfig()).isNotNull();
        assertThat(result.getCreatedAt()).isBeforeOrEqualTo(Instant.now());
        assertThat(result.getTouchedAt()).isBeforeOrEqualTo(Instant.now());
    }

    @Test
    void shouldCreateSubscriptionWithMusicBandFilter() {
        // Given
        SubscriptionRequest<MusicBandFilter> request = createMusicBandSubscriptionRequest();

        // When
        Subscription<MusicBandFilter> result = storage.createSubscription(PRINCIPAL_ID, request);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getFilter()).isInstanceOf(MusicBandFilter.class);
        assertThat(result.getFilter().getName()).isEqualTo("Rock Band");
        assertThat(result.getFilter().getGenre()).isEqualTo(MusicGenre.ROCK);
    }

    @Test
    void shouldCreateSubscriptionWithDefaultPageableConfig() {
        // Given
        SubscriptionRequest<BestBandAwardFilter> request = SubscriptionRequest.<BestBandAwardFilter>builder()
                .filter(createBestBandAwardFilter())
                .build();

        // When
        Subscription<BestBandAwardFilter> result = storage.createSubscription(PRINCIPAL_ID, request);

        // Then
        assertThat(result.getPageableConfig()).isNotNull();
        assertThat(result.getPageableConfig().getPage()).isEqualTo(0);
        assertThat(result.getPageableConfig().getSize()).isEqualTo(10);
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldUpdateSubscriptionSuccessfully() {
        // Given
        SubscriptionRequest<BestBandAwardFilter> createRequest = createBestBandAwardSubscriptionRequest();
        Subscription<BestBandAwardFilter> created = storage.createSubscription(PRINCIPAL_ID, createRequest);

        SubscriptionRequest<?> updateRequest = SubscriptionRequest
                .builder()
                .subscriptionId(created.getSubscriptionId())
                .filter(BestBandAwardFilter.builder()
                        .genre(MusicGenre.POST_PUNK)
                        .bandName("Updated Band")
                        .build())
                .pageableConfig(new PageableConfig())
                .build();

        // When
        Subscription<BestBandAwardFilter> result = (Subscription<BestBandAwardFilter>) storage.updateSubscription(updateRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getSubscriptionId()).isEqualTo(created.getSubscriptionId());
        assertThat(result.getPrincipalId()).isEqualTo(PRINCIPAL_ID);
        assertThat(result.getFilter().getGenre()).isEqualTo(MusicGenre.POST_PUNK);
        assertThat(result.getFilter().getBandName()).isEqualTo("Updated Band");
        assertThat(result.getPageableConfig().getPage()).isEqualTo(0);
        assertThat(result.getPageableConfig().getSize()).isEqualTo(10);
        assertThat(result.getCreatedAt()).isEqualTo(created.getCreatedAt());
    }

    @Test
    void shouldThrowExceptionWhenUpdatingNonExistentSubscription() {
        // Given
        UUID nonExistentId = UUID.randomUUID();
        SubscriptionRequest<BestBandAwardFilter> updateRequest = SubscriptionRequest.<BestBandAwardFilter>builder()
                .subscriptionId(nonExistentId)
                .filter(createBestBandAwardFilter())
                .build();

        // When & Then
        assertThatThrownBy(() -> storage.updateSubscription(updateRequest))
                .isInstanceOf(org.is.bandmanager.exception.ServiceException.class)
                .hasMessageContaining("Ресурс 'Subscription' с ID: '" + nonExistentId + "' не был найден");
    }

    @Test
    void shouldThrowExceptionWhenUpdatingWithNullSubscriptionId() {
        // Given
        SubscriptionRequest<BestBandAwardFilter> updateRequest = SubscriptionRequest.<BestBandAwardFilter>builder()
                .subscriptionId(null)
                .filter(createBestBandAwardFilter())
                .build();

        // When & Then
        assertThatThrownBy(() -> storage.updateSubscription(updateRequest))
                .isInstanceOf(org.is.bandmanager.exception.ServiceException.class)
                .hasMessageContaining("Ресурс 'Subscription.ID' не может быть пустым");
    }

    @Test
    void shouldGetSubscriptionById() {
        // Given
        SubscriptionRequest<BestBandAwardFilter> request = createBestBandAwardSubscriptionRequest();
        Subscription<BestBandAwardFilter> created = storage.createSubscription(PRINCIPAL_ID, request);

        // When
        Optional<Subscription<BestBandAwardFilter>> result = storage.getSubscription(created.getSubscriptionId());

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getSubscriptionId()).isEqualTo(created.getSubscriptionId());
        assertThat(result.get().getPrincipalId()).isEqualTo(PRINCIPAL_ID);
    }

    @Test
    void shouldReturnEmptyWhenGettingNonExistentSubscription() {
        // Given
        UUID nonExistentId = UUID.randomUUID();

        // When
        Optional<Subscription<BestBandAwardFilter>> result = storage.getSubscription(nonExistentId);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void shouldGetSubscriptionsByFilterType() {
        // Given
        SubscriptionRequest<BestBandAwardFilter> awardRequest = createBestBandAwardSubscriptionRequest();
        SubscriptionRequest<MusicBandFilter> bandRequest = createMusicBandSubscriptionRequest();

        storage.createSubscription(PRINCIPAL_ID, awardRequest);
        storage.createSubscription(PRINCIPAL_ID, bandRequest);
        storage.createSubscription(ANOTHER_PRINCIPAL_ID, awardRequest);

        // When
        List<Subscription<BestBandAwardFilter>> awardSubscriptions =
                storage.getSubscriptionsByType(BestBandAwardFilter.class);
        List<Subscription<MusicBandFilter>> bandSubscriptions =
                storage.getSubscriptionsByType(MusicBandFilter.class);

        // Then
        assertThat(awardSubscriptions).hasSize(2);
        assertThat(bandSubscriptions).hasSize(1);

        assertThat(awardSubscriptions)
                .allMatch(sub -> sub.getFilter() != null);
        assertThat(bandSubscriptions)
                .allMatch(sub -> sub.getFilter() != null);
    }

    @Test
    void shouldReturnEmptyListWhenNoSubscriptionsOfType() {
        // When
        List<Subscription<BestBandAwardFilter>> result =
                storage.getSubscriptionsByType(BestBandAwardFilter.class);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void shouldDeleteSubscriptionSuccessfully() {
        // Given
        SubscriptionRequest<BestBandAwardFilter> request = createBestBandAwardSubscriptionRequest();
        Subscription<BestBandAwardFilter> created = storage.createSubscription(PRINCIPAL_ID, request);

        // When
        storage.deleteSubscription(created.getSubscriptionId());

        // Then
        Optional<Subscription<BestBandAwardFilter>> result = storage.getSubscription(created.getSubscriptionId());
        assertThat(result).isEmpty();

        List<Subscription<BestBandAwardFilter>> byType =
                storage.getSubscriptionsByType(BestBandAwardFilter.class);
        assertThat(byType).isEmpty();
    }

    @Test
    void shouldDeleteAllPrincipalSubscriptions() {
        // Given
        SubscriptionRequest<BestBandAwardFilter> request1 = createBestBandAwardSubscriptionRequest();
        SubscriptionRequest<MusicBandFilter> request2 = createMusicBandSubscriptionRequest();

        Subscription<BestBandAwardFilter> sub1 = storage.createSubscription(PRINCIPAL_ID, request1);
        Subscription<MusicBandFilter> sub2 = storage.createSubscription(PRINCIPAL_ID, request2);
        storage.createSubscription(ANOTHER_PRINCIPAL_ID, request1); // Subscription from another user

        // When
        List<UUID> deletedIds = storage.deleteAllPrincipalSubscriptions(PRINCIPAL_ID);

        // Then
        assertThat(deletedIds).hasSize(2);
        assertThat(deletedIds).containsExactlyInAnyOrder(sub1.getSubscriptionId(), sub2.getSubscriptionId());

        assertThat(storage.getSubscription(sub1.getSubscriptionId())).isEmpty();
        assertThat(storage.getSubscription(sub2.getSubscriptionId())).isEmpty();

        // Subscription from another user should still exist
        List<Subscription<BestBandAwardFilter>> remaining =
                storage.getSubscriptionsByType(BestBandAwardFilter.class);
        assertThat(remaining).hasSize(1);
    }

    @Test
    void shouldReturnEmptyListWhenDeletingNonExistentPrincipalSubscriptions() {
        // When
        List<UUID> result = storage.deleteAllPrincipalSubscriptions("non-existent-principal");

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void shouldUpdateSubscriptionWithDifferentFilterType() {
        // Given
        SubscriptionRequest<BestBandAwardFilter> createRequest = createBestBandAwardSubscriptionRequest();
        Subscription<BestBandAwardFilter> created = storage.createSubscription(PRINCIPAL_ID, createRequest);

        SubscriptionRequest<MusicBandFilter> updateRequest = SubscriptionRequest.<MusicBandFilter>builder()
                .subscriptionId(created.getSubscriptionId())
                .filter(createMusicBandFilter())
                .build();

        // When
        Subscription<MusicBandFilter> result = storage.updateSubscription(updateRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getFilter()).isInstanceOf(MusicBandFilter.class);

        // Verify type index was updated
        List<Subscription<BestBandAwardFilter>> awardSubscriptions =
                storage.getSubscriptionsByType(BestBandAwardFilter.class);
        List<Subscription<MusicBandFilter>> bandSubscriptions =
                storage.getSubscriptionsByType(MusicBandFilter.class);

        assertThat(awardSubscriptions).isEmpty();
        assertThat(bandSubscriptions).hasSize(1);
    }

    private SubscriptionRequest<BestBandAwardFilter> createBestBandAwardSubscriptionRequest() {
        return SubscriptionRequest.<BestBandAwardFilter>builder()
                .filter(createBestBandAwardFilter())
                .pageableConfig(new PageableConfig())
                .build();
    }

    private SubscriptionRequest<MusicBandFilter> createMusicBandSubscriptionRequest() {
        return SubscriptionRequest.<MusicBandFilter>builder()
                .filter(createMusicBandFilter())
                .pageableConfig(new PageableConfig())
                .build();
    }

    private BestBandAwardFilter createBestBandAwardFilter() {
        return BestBandAwardFilter.builder()
                .genre(MusicGenre.ROCK)
                .bandName("Test Band")
                .bandId(1L)
                .build();
    }

    private MusicBandFilter createMusicBandFilter() {
        return MusicBandFilter.builder()
                .name("Rock Band")
                .genre(MusicGenre.ROCK)
                .minParticipants(1L)
                .maxParticipants(10L)
                .build();
    }

}