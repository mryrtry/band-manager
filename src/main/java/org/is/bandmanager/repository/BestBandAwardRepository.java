package org.is.bandmanager.repository;

import org.is.bandmanager.model.BestBandAward;
import org.is.bandmanager.model.MusicGenre;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BestBandAwardRepository extends JpaRepository<BestBandAward, Long> {

    @Query("""
                SELECT a FROM BestBandAward a 
                LEFT JOIN a.band band 
                WHERE 
                (:genre IS NULL OR a.genre = :genre) AND
                (:bandName IS NULL OR band.name = :bandName) AND
                (:bandId IS NULL OR band.id = :bandId)
            """)
    Page<BestBandAward> findAllWithFilters(
            @Param("genre") MusicGenre genre,
            @Param("bandName") String bandName,
            @Param("bandId") Integer bandId,
            Pageable pageable);

    List<BestBandAward> findAllByBandId(Integer bandId);

}

