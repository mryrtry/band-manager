package org.is.bandmanager.repository.specifications;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import org.is.bandmanager.dto.request.MusicBandFilter;
import org.is.bandmanager.model.Album;
import org.is.bandmanager.model.Coordinates;
import org.is.bandmanager.model.MusicBand;
import org.is.bandmanager.model.Person;
import org.springframework.data.jpa.domain.Specification;

public class MusicBandSpecifications {

    public static Specification<MusicBand> withFilter(MusicBandFilter filter) {
        if (filter == null) return null;

        return Specification.<MusicBand>where((root, query, cb) ->
                        filter.getName() != null ? cb.equal(root.get("name"), filter.getName()) : null)
                .and((root, query, cb) ->
                        filter.getDescription() != null ? cb.equal(root.get("description"), filter.getDescription()) : null)
                .and((root, query, cb) ->
                        filter.getGenre() != null ? cb.equal(root.get("genre"), filter.getGenre()) : null)
                .and((root, query, cb) -> {
                    if (filter.getFrontManName() == null) return null;
                    Join<MusicBand, Person> frontMan = root.join("frontMan", JoinType.LEFT);
                    return cb.equal(frontMan.get("name"), filter.getFrontManName());
                })
                .and((root, query, cb) -> {
                    if (filter.getBestAlbumName() == null) return null;
                    Join<MusicBand, Album> album = root.join("bestAlbum", JoinType.LEFT);
                    return cb.equal(album.get("name"), filter.getBestAlbumName());
                })
                .and((root, query, cb) -> {
                    Long min = filter.getMinParticipants();
                    Long max = filter.getMaxParticipants();
                    if (min == null && max == null) return null;
                    if (min == null) return cb.lessThanOrEqualTo(root.get("numberOfParticipants"), max);
                    if (max == null) return cb.greaterThanOrEqualTo(root.get("numberOfParticipants"), min);
                    return cb.between(root.get("numberOfParticipants"), min, max);
                })
                .and((root, query, cb) -> {
                    Long min = filter.getMinSingles();
                    Long max = filter.getMaxSingles();
                    if (min == null && max == null) return null;
                    if (min == null) return cb.lessThanOrEqualTo(root.get("singlesCount"), max);
                    if (max == null) return cb.greaterThanOrEqualTo(root.get("singlesCount"), min);
                    return cb.between(root.get("singlesCount"), min, max);
                })
                .and((root, query, cb) -> {
                    Long min = filter.getMinAlbumsCount();
                    Long max = filter.getMaxAlbumsCount();
                    if (min == null && max == null) return null;
                    if (min == null) return cb.lessThanOrEqualTo(root.get("albumsCount"), max);
                    if (max == null) return cb.greaterThanOrEqualTo(root.get("albumsCount"), min);
                    return cb.between(root.get("albumsCount"), min, max);
                })
                .and((root, query, cb) -> {
                    Integer min = filter.getMinCoordinateX();
                    Integer max = filter.getMaxCoordinateX();
                    if (min == null && max == null) return null;
                    Join<MusicBand, Coordinates> coordinates = root.join("coordinates", JoinType.LEFT);
                    if (min == null) return cb.lessThanOrEqualTo(coordinates.get("x"), max);
                    if (max == null) return cb.greaterThanOrEqualTo(coordinates.get("x"), min);
                    return cb.between(coordinates.get("x"), min, max);
                })
                .and((root, query, cb) -> {
                    Float min = filter.getMinCoordinateY();
                    Float max = filter.getMaxCoordinateY();
                    if (min == null && max == null) return null;
                    Join<MusicBand, Coordinates> coord = root.join("coordinates", JoinType.LEFT);
                    if (min == null) return cb.lessThanOrEqualTo(coord.get("y"), max);
                    if (max == null) return cb.greaterThanOrEqualTo(coord.get("y"), min);
                    return cb.between(coord.get("y"), min, max);
                })
                .and((root, query, cb) ->
                        filter.getEstablishmentDateAfter() != null ? cb.greaterThan(root.get("establishmentDate"), filter.getEstablishmentDateAfter()) : null)
                .and((root, query, cb) ->
                        filter.getEstablishmentDateBefore() != null ? cb.lessThan(root.get("establishmentDate"), filter.getEstablishmentDateBefore()) : null);
    }

}
