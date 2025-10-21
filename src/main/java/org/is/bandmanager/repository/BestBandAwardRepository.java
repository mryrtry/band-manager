package org.is.bandmanager.repository;

import org.is.bandmanager.model.BestBandAward;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BestBandAwardRepository extends JpaRepository<BestBandAward, Long>, JpaSpecificationExecutor<BestBandAward> {

    List<BestBandAward> findAllByBandId(Integer bandId);

}

