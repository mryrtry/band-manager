package org.is.bandmanager.repository;

import org.is.bandmanager.model.Coordinates;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository("CoordinatesRepository")
public interface CoordinatesRepository extends JpaRepository<Coordinates, Long> {

    @Query("SELECT p FROM Coordinates p WHERE NOT EXISTS (SELECT 1 FROM MusicBand m WHERE m.coordinates.id = p.id)")
    List<Coordinates> findUnusedCoordinates();

}