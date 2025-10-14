package org.is.bandmanager.repository;

import org.is.bandmanager.model.MusicBand;
import org.is.bandmanager.model.MusicGenre;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

    @Query("""
                SELECT mb FROM MusicBand mb
                LEFT JOIN mb.coordinates coord
                LEFT JOIN mb.frontMan fm
                LEFT JOIN mb.bestAlbum album
                WHERE
                (:name IS NULL OR mb.name = :name) AND
                (:description IS NULL OR mb.description = :description) AND
                (:genre IS NULL OR mb.genre = :genre) AND
                (:frontManName IS NULL OR fm.name = :frontManName) AND
                (:bestAlbumName IS NULL OR album.name = :bestAlbumName) AND
                (:minParticipants IS NULL OR mb.numberOfParticipants >= :minParticipants) AND
                (:maxParticipants IS NULL OR mb.numberOfParticipants <= :maxParticipants) AND
                (:minSingles IS NULL OR mb.singlesCount >= :minSingles) AND
                (:maxSingles IS NULL OR mb.singlesCount <= :maxSingles) AND
                (:minAlbumsCount IS NULL OR mb.albumsCount >= :minAlbumsCount) AND
                (:maxAlbumsCount IS NULL OR mb.albumsCount <= :maxAlbumsCount) AND
                (:minCoordinateX IS NULL OR coord.x >= :minCoordinateX) AND
                (:maxCoordinateX IS NULL OR coord.x <= :maxCoordinateX) AND
                (:minCoordinateY IS NULL OR coord.y >= :minCoordinateY) AND
                (:maxCoordinateY IS NULL OR coord.y <= :maxCoordinateY)
            """)
    Page<MusicBand> findAllWithFilters(
            @Param("name") String name,
            @Param("description") String description,
            @Param("genre") MusicGenre genre,
            @Param("frontManName") String frontManName,
            @Param("bestAlbumName") String bestAlbumName,
            @Param("minParticipants") Long minParticipants,
            @Param("maxParticipants") Long maxParticipants,
            @Param("minSingles") Long minSingles,
            @Param("maxSingles") Long maxSingles,
            @Param("minAlbumsCount") Long minAlbumsCount,
            @Param("maxAlbumsCount") Long maxAlbumsCount,
            @Param("minCoordinateX") Integer minCoordinateX,
            @Param("maxCoordinateX") Integer maxCoordinateX,
            @Param("minCoordinateY") Float minCoordinateY,
            @Param("maxCoordinateY") Float maxCoordinateY,
            Pageable pageable);

    boolean existsByCoordinatesId(Long coordinatesId);

    boolean existsByBestAlbumId(Long bestAlbumId);

    boolean existsByFrontManId(Long frontManId);

    Long countByCoordinatesId(Long coordinatesId);

    Long countByFrontManId(Long frontManId);

    Long countByBestAlbumId(Long albumId);

}
