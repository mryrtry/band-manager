package org.is.bandmanager.config;

import org.is.bandmanager.model.MusicBand;
import org.is.util.pageable.sort.SortConfig;
import org.springframework.context.annotation.Configuration;

import java.util.Set;


@Configuration
public class MusicBandSortConfig implements SortConfig {

    @Override
    public Class<?> getEntityClass() {
        return MusicBand.class;
    }

    @Override
    public Set<String> getAllowedSortFields() {
        return Set.of("id", "name", "description", "genre", "numberOfParticipants",
                "singlesCount", "albumsCount", "establishmentDate",
                "frontMan.name", "bestAlbum.name", "coordinates.x", "coordinates.y");
    }

    @Override
    public String getDefaultSortField() {
        return "id";
    }

}
