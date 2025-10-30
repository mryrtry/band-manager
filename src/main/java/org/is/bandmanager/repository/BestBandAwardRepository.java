package org.is.bandmanager.repository;

import org.is.bandmanager.repository.filter.BestBandAwardFilter;
import org.is.bandmanager.model.BestBandAward;
import org.is.bandmanager.repository.specifications.BestBandAwardSpecifications;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface BestBandAwardRepository extends JpaRepository<BestBandAward, Long>, JpaSpecificationExecutor<BestBandAward> {

    default Page<BestBandAward> findWithFilter(BestBandAwardFilter filter, Pageable pageable) {
        Specification<BestBandAward> specification = BestBandAwardSpecifications.withFilter(filter);
        return findAll(specification, pageable);
    }

    List<BestBandAward> deleteAllByBandIdIn(List<Integer> bandIds);

}

