package org.is.bandmanager.repo;

import org.is.bandmanager.model.MusicBand;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MusicBandRepository extends JpaRepository<MusicBand, Long> {
}
