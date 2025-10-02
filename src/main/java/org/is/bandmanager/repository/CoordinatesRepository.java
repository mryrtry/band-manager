package org.is.bandmanager.repository;

import org.is.bandmanager.model.Coordinates;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface CoordinatesRepository extends JpaRepository<Coordinates, Long> {

    @Query("SELECT c FROM Coordinates c ORDER BY (c.x * c.x + c.y * c.y) DESC LIMIT 1")
    Coordinates findMaxCoordinates();

}