package org.is.bandmanager.repository;

import org.is.bandmanager.model.Person;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface PersonRepository extends JpaRepository<Person, Long> {

	@Query("SELECT p FROM Person p WHERE NOT EXISTS (SELECT 1 FROM MusicBand m WHERE m.frontMan.id = p.id)")
	List<Person> findUnusedPersons();

	boolean existsByLocationId(Long locationId);

}
