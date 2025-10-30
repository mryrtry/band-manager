package org.is.bandmanager.util.pageable;

import lombok.Getter;

import java.util.Set;

@Getter
public enum PageableType {
    BEST_BAND_AWARDS(Set.of(
            "id", "bandName", "genre", "createdAt", "bandId"
    ), "createdAt"),

    MUSIC_BANDS(Set.of(
            "id", "name", "description", "genre", "numberOfParticipants",
            "singlesCount", "albumsCount", "establishmentDate",
            "frontMan.name", "bestAlbum.name", "coordinates.x", "coordinates.y"
    ), "id");

    private final Set<String> allowedFields;
    private final String defaultField;

    PageableType(Set<String> allowedFields, String defaultField) {
        this.allowedFields = allowedFields;
        this.defaultField = defaultField;
    }

}
