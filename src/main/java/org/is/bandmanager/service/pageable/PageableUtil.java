package org.is.bandmanager.service.pageable;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

@Component
public class PageableUtil {

    private static final Set<String> ALLOWED_BEST_BAND_AWARD_SORT_FIELDS = Set.of(
            "id", "bandName", "genre", "createdAt", "bandId"
    );

    private static final Set<String> ALLOWED_MUSIC_BAND_SORT_FIELDS = Set.of(
            "id", "name", "description", "genre", "numberOfParticipants",
            "singlesCount", "albumsCount", "establishmentDate",
            "frontMan.name", "bestAlbum.name", "coordinates.x", "coordinates.y"
    );

    public static Pageable createBestBandAwardPageable(PageableConfig config) {
        return createPageable(config, ALLOWED_BEST_BAND_AWARD_SORT_FIELDS, "createdAt");
    }

    public static Pageable createMusicBandPageable(PageableConfig config) {
        return createPageable(config, ALLOWED_MUSIC_BAND_SORT_FIELDS, "id");
    }

    private static Pageable createPageable(PageableConfig config, Set<String> allowedFields, String defaultField) {
        int page = Math.max(config.getPage(), 0);
        int size = config.getSize() > 0 ? config.getSize() : 10;

        List<String> sortFields = (config.getSort() == null || config.getSort().isEmpty())
                ? List.of(defaultField)
                : config.getSort().stream()
                .filter(allowedFields::contains)
                .toList();

        if (sortFields.isEmpty()) {
            sortFields = List.of(defaultField);
        }

        Sort.Direction direction;
        try {
            direction = Sort.Direction.fromString(config.getDirection());
        } catch (IllegalArgumentException e) {
            direction = Sort.Direction.ASC;
        }

        Sort sort = Sort.by(direction, sortFields.toArray(String[]::new));

        return PageRequest.of(page, size, sort);
    }

}
