package org.is.bandmanager.repository.specifications;

import org.is.bandmanager.repository.filter.BestBandAwardFilter;
import org.is.bandmanager.model.BestBandAward;
import org.springframework.data.jpa.domain.Specification;

public final class BestBandAwardSpecifications {

    public static Specification<BestBandAward> withFilter(BestBandAwardFilter filter) {
        if (filter == null) return null;

        return Specification.<BestBandAward>where((root, query, cb) ->
                        filter.getGenre() != null ? cb.equal(root.get("genre"), filter.getGenre()) : null)
                .and((root, query, cb) ->
                        filter.getBandName() != null ? cb.equal(root.get("bandName"), filter.getBandName()) : null)
                .and((root, query, cb) ->
                        filter.getBandId() != null ? cb.equal(root.get("bandId"), filter.getBandId()) : null)
                .and((root, query, cb) ->
                        filter.getCreatedAtAfter() != null ? cb.greaterThanOrEqualTo(root.get("createdAt"), filter.getCreatedAtAfter()) : null)
                .and((root, query, cb) ->
                        filter.getCreatedAtBefore() != null ? cb.lessThanOrEqualTo(root.get("createdAt"), filter.getCreatedAtBefore()) : null);
    }

}
