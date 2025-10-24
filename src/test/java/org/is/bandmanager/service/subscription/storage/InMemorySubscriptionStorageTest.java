package org.is.bandmanager.service.subscription.storage;

import org.is.bandmanager.repository.specifications.EntityFilter;
import org.is.bandmanager.service.subscription.model.Subscription;
import org.is.bandmanager.service.subscription.model.request.SubscriptionRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class InMemorySubscriptionStorageTest {

    private InMemorySubscriptionStorage storage;

    static class DummyFilterA implements EntityFilter {
    }

    static class DummyFilterB implements EntityFilter {
    }

    @BeforeEach
    void setup() {
        storage = new InMemorySubscriptionStorage();
    }

    @Test
    void shouldCreateAndGetSubscription() {
        DummyFilterA filter = new DummyFilterA();
        SubscriptionRequest<DummyFilterA> request = SubscriptionRequest.<DummyFilterA>builder()
                .filter(filter)
                .build();

        Subscription<DummyFilterA> sub = storage.createSubscription("session1", request);

        assertNotNull(sub.getSubscriptionId());
        assertEquals("session1", sub.getSessionId());
        assertEquals(filter, sub.getFilter());

        Optional<Subscription<DummyFilterA>> fetched = storage.getSubscription(sub.getSubscriptionId());
        assertTrue(fetched.isPresent());
        assertEquals(sub.getSubscriptionId(), fetched.get().getSubscriptionId());
    }

    @Test
    void shouldUpdateSubscriptionAndMaintainTypeIndex() {
        DummyFilterA filter = new DummyFilterA();
        Subscription<DummyFilterA> sub = storage.createSubscription("s1", SubscriptionRequest.<DummyFilterA>builder()
                .filter(filter)
                .build());

        DummyFilterB newFilter = new DummyFilterB();
        SubscriptionRequest<DummyFilterB> updateRequest = SubscriptionRequest.<DummyFilterB>builder()
                .subscriptionId(sub.getSubscriptionId())
                .filter(newFilter)
                .build();

        storage.updateSubscription(updateRequest);

        List<Subscription<DummyFilterA>> typeA = storage.getSubscriptionsByType(DummyFilterA.class);
        List<Subscription<DummyFilterB>> typeB = storage.getSubscriptionsByType(DummyFilterB.class);

        assertTrue(typeA.isEmpty());
        assertEquals(1, typeB.size());
        assertEquals(newFilter, typeB.get(0).getFilter());
    }

    @Test
    void shouldDeleteSubscription() {
        DummyFilterA filter = new DummyFilterA();
        Subscription<DummyFilterA> sub = storage.createSubscription("s1", SubscriptionRequest.<DummyFilterA>builder()
                .filter(filter)
                .build());

        storage.deleteSubscription(sub.getSubscriptionId());
        assertFalse(storage.getSubscription(sub.getSubscriptionId()).isPresent());
        assertTrue(storage.getSubscriptionsByType(DummyFilterA.class).isEmpty());
    }

    @Test
    void shouldGetSubscriptionsByType() {
        storage.createSubscription("s1", SubscriptionRequest.<DummyFilterA>builder().filter(new DummyFilterA()).build());
        storage.createSubscription("s2", SubscriptionRequest.<DummyFilterB>builder().filter(new DummyFilterB()).build());
        storage.createSubscription("s3", SubscriptionRequest.<DummyFilterA>builder().filter(new DummyFilterA()).build());

        List<Subscription<DummyFilterA>> typeA = storage.getSubscriptionsByType(DummyFilterA.class);
        List<Subscription<DummyFilterB>> typeB = storage.getSubscriptionsByType(DummyFilterB.class);

        assertEquals(2, typeA.size());
        assertEquals(1, typeB.size());
    }

    @Test
    void shouldDeleteSubscriptionsBySession() {
        storage.createSubscription("session1", SubscriptionRequest.<DummyFilterA>builder().filter(new DummyFilterA()).build());
        storage.createSubscription("session2", SubscriptionRequest.<DummyFilterB>builder().filter(new DummyFilterB()).build());
        storage.createSubscription("session1", SubscriptionRequest.<DummyFilterA>builder().filter(new DummyFilterA()).build());

        List<UUID> deleted = storage.deleteSessionSubscriptions("session1");
        assertEquals(2, deleted.size());

        List<Subscription<DummyFilterA>> remaining = storage.getSubscriptionsByType(DummyFilterA.class);
        assertTrue(remaining.isEmpty());
    }

    @Test
    void shouldDeleteDeadSubscriptions() {
        DummyFilterA filter = new DummyFilterA();
        Subscription<DummyFilterA> sub1 = storage.createSubscription("s1", SubscriptionRequest.<DummyFilterA>builder().filter(filter).build());
        Subscription<DummyFilterA> sub2 = storage.createSubscription("s2", SubscriptionRequest.<DummyFilterA>builder().filter(filter).build());

        setTouchedAt(sub1, Instant.now().minusSeconds(7200));
        setTouchedAt(sub2, Instant.now().minusSeconds(1800));

        List<UUID> removed = storage.deleteDeadSubscriptions();

        assertEquals(1, removed.size());
        assertTrue(removed.contains(sub1.getSubscriptionId()));
        assertFalse(storage.getSubscription(sub1.getSubscriptionId()).isPresent());
        assertTrue(storage.getSubscription(sub2.getSubscriptionId()).isPresent());
    }

    private void setTouchedAt(Subscription<?> sub, Instant time) {
        try {
            java.lang.reflect.Field field = Subscription.class.getDeclaredField("touchedAt");
            field.setAccessible(true);
            field.set(sub, time);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
