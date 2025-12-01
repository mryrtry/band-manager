package org.is.bandmanager.repository;

import org.is.bandmanager.model.MusicBand;
import org.is.bandmanager.repository.filter.MusicBandFilter;
import org.is.bandmanager.repository.specifications.MusicBandSpecifications;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;


@Repository("MusicBandRepository")
public interface MusicBandRepository extends JpaRepository<MusicBand, Long>, JpaSpecificationExecutor<MusicBand> {

	default Page<MusicBand> findWithFilter(MusicBandFilter filter, Pageable pageable) {
		Specification<MusicBand> specification = MusicBandSpecifications.withFilter(filter);
		return findAll(specification, pageable);
	}

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

	boolean existsByCoordinatesId(Long coordinatesId);

	boolean existsByBestAlbumId(Long bestAlbumId);

	boolean existsByName(String name);

	boolean existsByNameAndIdNot(String name, Long id);

}
