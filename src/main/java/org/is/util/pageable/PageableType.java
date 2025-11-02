package org.is.util.pageable;

import lombok.Getter;

import java.util.Set;

@Getter
public enum PageableType {
    // todo: Разобраться с bandName -> band.name
    BEST_BAND_AWARDS(Set.of(
            "id", "bandName", "genre", "createdAt", "bandId"
    ), "createdDate"),

    MUSIC_BANDS(Set.of(
            "id", "name", "description", "genre", "numberOfParticipants",
            "singlesCount", "albumsCount", "establishmentDate",
            "frontMan.name", "bestAlbum.name", "coordinates.x", "coordinates.y"
    ), "id"),

    USERS(Set.of(
            "id", "username", "createdAt", "updatedAt"
    ), "id");

    private final Set<String> allowedFields;
    private final String defaultField;

    PageableType(Set<String> allowedFields, String defaultField) {
        this.allowedFields = allowedFields;
        this.defaultField = defaultField;
    }

}
