package org.is.bandmanager.repository.specifications;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import org.is.bandmanager.repository.filter.MusicBandFilter;
import org.is.bandmanager.model.*;
import org.springframework.data.jpa.domain.Specification;

import java.util.Date;

public class MusicBandSpecifications {

    public static Specification<MusicBand> hasName(String name) {
        return (root, query, cb) ->
                name == null ? null : cb.equal(root.get("name"), name);
    }

    public static Specification<MusicBand> hasDescription(String description) {
        return (root, query, cb) ->
                description == null ? null : cb.equal(root.get("description"), description);
    }

    public static Specification<MusicBand> hasGenre(MusicGenre genre) {
        return (root, query, cb) ->
                genre == null ? null : cb.equal(root.get("genre"), genre);
    }

    public static Specification<MusicBand> hasFrontManName(String frontManName) {
        return (root, query, cb) -> {
            if (frontManName == null) return null;
            Join<MusicBand, Person> frontManJoin = root.join("frontMan", JoinType.LEFT);
            return cb.equal(frontManJoin.get("name"), frontManName);
        };
    }

    public static Specification<MusicBand> hasBestAlbumName(String bestAlbumName) {
        return (root, query, cb) -> {
            if (bestAlbumName == null) return null;
            Join<MusicBand, Album> albumJoin = root.join("bestAlbum", JoinType.LEFT);
            return cb.equal(albumJoin.get("name"), bestAlbumName);
        };
    }

    public static Specification<MusicBand> participantsBetween(Long min, Long max) {
        return (root, query, cb) -> {
            if (min == null && max == null) return null;
            if (min == null) return cb.lessThanOrEqualTo(root.get("numberOfParticipants"), max);
            if (max == null) return cb.greaterThanOrEqualTo(root.get("numberOfParticipants"), min);
            return cb.between(root.get("numberOfParticipants"), min, max);
        };
    }

    public static Specification<MusicBand> singlesBetween(Long min, Long max) {
        return (root, query, cb) -> {
            if (min == null && max == null) return null;
            if (min == null) return cb.lessThanOrEqualTo(root.get("singlesCount"), max);
            if (max == null) return cb.greaterThanOrEqualTo(root.get("singlesCount"), min);
            return cb.between(root.get("singlesCount"), min, max);
        };
    }

    public static Specification<MusicBand> albumsCountBetween(Long min, Long max) {
        return (root, query, cb) -> {
            if (min == null && max == null) return null;
            if (min == null) return cb.lessThanOrEqualTo(root.get("albumsCount"), max);
            if (max == null) return cb.greaterThanOrEqualTo(root.get("albumsCount"), min);
            return cb.between(root.get("albumsCount"), min, max);
        };
    }

    public static Specification<MusicBand> coordinateXBetween(Integer min, Integer max) {
        return (root, query, cb) -> {
            if (min == null && max == null) return null;
            Join<MusicBand, Coordinates> coordJoin = root.join("coordinates", JoinType.LEFT);
            if (min == null) return cb.lessThanOrEqualTo(coordJoin.get("x"), max);
            if (max == null) return cb.greaterThanOrEqualTo(coordJoin.get("x"), min);
            return cb.between(coordJoin.get("x"), min, max);
        };
    }

    public static Specification<MusicBand> coordinateYBetween(Float min, Float max) {
        return (root, query, cb) -> {
            if (min == null && max == null) return null;
            Join<MusicBand, Coordinates> coordJoin = root.join("coordinates", JoinType.LEFT);
            if (min == null) return cb.lessThanOrEqualTo(coordJoin.get("y"), max);
            if (max == null) return cb.greaterThanOrEqualTo(coordJoin.get("y"), min);
            return cb.between(coordJoin.get("y"), min, max);
        };
    }

    public static Specification<MusicBand> establishmentDateBefore(Date date) {
        return (root, query, cb) ->
                date == null ? null : cb.lessThan(root.get("establishmentDate"), date);
    }

    public static Specification<MusicBand> establishmentDateAfter(Date date) {
        return (root, query, cb) ->
                date == null ? null : cb.greaterThan(root.get("establishmentDate"), date);
    }

    public static Specification<MusicBand> withFilter(MusicBandFilter filter) {
        return Specification.where(hasName(filter.getName()))
                .and(hasDescription(filter.getDescription()))
                .and(hasGenre(filter.getGenre()))
                .and(hasFrontManName(filter.getFrontManName()))
                .and(hasBestAlbumName(filter.getBestAlbumName()))
                .and(participantsBetween(filter.getMinParticipants(), filter.getMaxParticipants()))
                .and(singlesBetween(filter.getMinSingles(), filter.getMaxSingles()))
                .and(albumsCountBetween(filter.getMinAlbumsCount(), filter.getMaxAlbumsCount()))
                .and(coordinateXBetween(filter.getMinCoordinateX(), filter.getMaxCoordinateX()))
                .and(coordinateYBetween(filter.getMinCoordinateY(), filter.getMaxCoordinateY()))
                .and(establishmentDateAfter(filter.getEstablishmentDateAfter()))
                .and(establishmentDateBefore(filter.getEstablishmentDateBefore()));
    }

}