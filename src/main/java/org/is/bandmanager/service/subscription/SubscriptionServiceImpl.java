package org.is.bandmanager.service.subscription;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.is.bandmanager.dto.BestBandAwardDto;
import org.is.bandmanager.dto.MusicBandDto;
import org.is.bandmanager.dto.request.BestBandAwardFilter;
import org.is.bandmanager.dto.request.MusicBandFilter;
import org.is.bandmanager.event.EntityEvent;
import org.is.bandmanager.exception.ServiceException;
import org.is.bandmanager.repository.specifications.EntityFilter;
import org.is.bandmanager.service.BestBandAwardService;
import org.is.bandmanager.service.MusicBandService;
import org.is.bandmanager.service.subscription.model.Subscription;
import org.is.bandmanager.service.subscription.model.request.SubscriptionRequest;
import org.is.bandmanager.service.subscription.notifier.SubscriptionNotifier;
import org.is.bandmanager.service.subscription.storage.SubscriptionStorage;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.Page;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static org.is.bandmanager.exception.message.ServiceErrorMessage.CANNOT_ACCESS_SOURCE;
import static org.is.bandmanager.exception.message.ServiceErrorMessage.SOURCE_NOT_FOUND;

@Slf4j
@Service
@RequiredArgsConstructor
public class SubscriptionServiceImpl implements SubscriptionService {

    private final SubscriptionStorage storage;
    private final SubscriptionNotifier notifier;

    private final MusicBandService musicBandService;
    private final BestBandAwardService bestBandAwardService;

    private Subscription<?> getSubscription(UUID id) {
        return storage.getSubscription(id)
                .orElseThrow(() -> new ServiceException(SOURCE_NOT_FOUND, "Subscription", id));
    }

    private void assertSessionEquals(String principalId, Subscription<?> subscription) {
        if (!Objects.equals(subscription.getPrincipalId(), principalId))
            throw new ServiceException(CANNOT_ACCESS_SOURCE, "Subscription", subscription.getSubscriptionId());
    }

    @Override
    public void createSubscription(String principalId, SubscriptionRequest<?> subscriptionRequest) {
        Subscription<?> subscription = storage.createSubscription(principalId, subscriptionRequest);
        notifier.notifySubscriptionMeta(subscription);
    }

    @Override
    public void updateSubscription(String principalId, SubscriptionRequest<?> subscriptionRequest) {
        Subscription<?> subscription = getSubscription(subscriptionRequest.getSubscriptionId());
        assertSessionEquals(principalId, subscription);
        Subscription<?> updatedSubscription = storage.updateSubscription(subscriptionRequest);
        notifier.notifySubscriptionMeta(updatedSubscription);
    }

    @Override
    public void cancelSubscription(String principalId, UUID subscriptionId) {
        Subscription<?> subscription = getSubscription(subscriptionId);
        assertSessionEquals(principalId, subscription);
        storage.deleteSubscription(subscriptionId);
    }

    @Override
    public void cancelAllPrincipalSubscriptions(String principalId) {
        storage.deleteAllPrincipalSubscriptions(principalId);
    }

    @Override
    @Async("subscriptionTaskExecutor")
    @Scheduled(fixedRate = 1800000)
    public void cancelDeadSubscriptions() {
        storage.deleteDeadSubscriptions();
    }

    @Override
    @Async("subscriptionTaskExecutor")
    @EventListener(EntityEvent.class)
    public void handleEntityEvent(EntityEvent<?> entityEvent) {
        if (entityEvent.getEntities().isEmpty()) return;
        if (entityEvent.getEntities().get(0).getClass().equals(MusicBandDto.class)) {
            List<Subscription<MusicBandFilter>> subscriptions = storage.getSubscriptionsByType(MusicBandFilter.class);
            subscriptions.forEach(this::sendSubscriptionData);
        } else if (entityEvent.getEntities().get(0).getClass().equals(BestBandAwardDto.class)) {
            List<Subscription<BestBandAwardFilter>> subscriptions = storage.getSubscriptionsByType(BestBandAwardFilter.class);
            subscriptions.forEach(this::sendSubscriptionData);
        }
    }

    @Override
    @Async("subscriptionTaskExecutor")
    public void notifySubscription(UUID subscriptionId) {
        sendSubscriptionData(getSubscription(subscriptionId));
    }

    private void sendSubscriptionData(Subscription<?> subscription) {
        Page<?> subscriptionData = getSubscriptionData(subscription);
        notifier.notifySubscription(subscription, subscriptionData);
    }

    private <T extends EntityFilter> Page<?> getSubscriptionData(Subscription<T> subscription) {
        return switch (subscription.getFilter().getClass().getSimpleName()) {
            case "MusicBandFilter" ->
                    musicBandService.getAll((MusicBandFilter) subscription.getFilter(), subscription.getPageableConfig());
            case "BestBandAwardFilter" ->
                    bestBandAwardService.getAll((BestBandAwardFilter) subscription.getFilter(), subscription.getPageableConfig());
            default -> Page.empty();
        };
    }

}
