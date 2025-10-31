package org.is.bandmanager.repository.specifications;

import org.is.bandmanager.model.BestBandAward;
import org.is.bandmanager.repository.filter.BestBandAwardFilter;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalTime;

public final class BestBandAwardSpecifications {

    public static Specification<BestBandAward> withFilter(BestBandAwardFilter filter) {
        if (filter == null) return null;

        return Specification.<BestBandAward>where((root, query, cb) ->
                        filter.getGenre() != null ? cb.equal(root.get("genre"), filter.getGenre()) : null)
                .and((root, query, cb) ->
                        filter.getBandName() != null ? cb.equal(root.get("band").get("name"), filter.getBandName()) : null)
                .and((root, query, cb) ->
                        filter.getBandId() != null ? cb.equal(root.get("band").get("id"), filter.getBandId()) : null)
                .and((root, query, cb) ->
                        filter.getCreatedAtAfter() != null ?
                                cb.greaterThanOrEqualTo(root.get("createdAt"), filter.getCreatedAtAfter().atStartOfDay()) : null)
                .and((root, query, cb) ->
                        filter.getCreatedAtBefore() != null ?
                                cb.lessThanOrEqualTo(root.get("createdAt"), filter.getCreatedAtBefore().atTime(LocalTime.MAX)) : null);
    }

}
