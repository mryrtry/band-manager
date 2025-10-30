package org.is.bandmanager.repository.specifications;

import org.is.bandmanager.model.BestBandAward;
import org.is.bandmanager.model.MusicGenre;
import org.is.bandmanager.repository.filter.BestBandAwardFilter;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;


public class BestBandAwardSpecifications {

    public static Specification<BestBandAward> hasGenre(MusicGenre genre) {
        return (root, query, cb) -> genre == null ? null : cb.equal(root.get("genre"), genre);
    }

    public static Specification<BestBandAward> hasBandName(String bandName) {
        return (root, query, cb) -> bandName == null ? null : cb.equal(root.get("bandName"), bandName);
    }

    public static Specification<BestBandAward> bandNameContains(String bandName) {
        return (root, query, cb) -> bandName == null ? null : cb.like(cb.lower(root.get("bandName")), "%" + bandName.toLowerCase() + "%");
    }

    public static Specification<BestBandAward> hasBandId(Long bandId) {
        return (root, query, cb) -> bandId == null ? null : cb.equal(root.get("bandId"), bandId);
    }

    public static Specification<BestBandAward> createdAtAfter(LocalDateTime date) {
        return (root, query, cb) -> date == null ? null : cb.greaterThanOrEqualTo(root.get("createdAt"), date);
    }

    public static Specification<BestBandAward> createdAtBefore(LocalDateTime date) {
        return (root, query, cb) -> date == null ? null : cb.lessThanOrEqualTo(root.get("createdAt"), date);
    }

    public static Specification<BestBandAward> withFilter(BestBandAwardFilter filter) {
        return Specification.where(hasGenre(filter.getGenre())).and(hasBandName(filter.getBandName())).and(bandNameContains(filter.getBandNameContains())).and(hasBandId(filter.getBandId())).and(createdAtAfter(filter.getCreatedAtAfter())).and(createdAtBefore(filter.getCreatedAtBefore()));
    }

}