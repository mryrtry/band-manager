package org.is.bandmanager.repository;

import org.is.bandmanager.model.MusicBand;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface MusicBandRepository extends JpaRepository<MusicBand, Integer> {

    @Query(value = """
             SELECT mb.* FROM music_band mb\s
             JOIN coordinates c ON mb.coordinates_id = c.id\s
             ORDER BY c.x DESC, c.y DESC\s
             LIMIT 1
            \s""", nativeQuery = true)
    Optional<MusicBand> findBandWithMaxCoordinates();

    List<MusicBand> findByEstablishmentDateBefore(Date date);

    @Query("SELECT DISTINCT m.albumsCount FROM MusicBand m ORDER BY m.albumsCount")
    List<Long> findDistinctAlbumsCount();

}
