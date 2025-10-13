package org.is.bandmanager.repository;

import org.is.bandmanager.model.BestBandAward;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BestBandAwardRepository extends JpaRepository<BestBandAward, Long> {
}

