package org.is.bandmanager.repository;

import org.is.bandmanager.model.Location;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LocationRepository extends JpaRepository<Location, Long> {

	@Query("SELECT p FROM Location p WHERE NOT EXISTS (SELECT 1 FROM Person m WHERE m.location.id = p.id)")
	List<Location> findUnusedLocations();

}
