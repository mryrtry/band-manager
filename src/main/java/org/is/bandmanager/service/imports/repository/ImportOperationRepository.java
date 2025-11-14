package org.is.bandmanager.service.imports.repository;

import org.is.bandmanager.service.imports.model.ImportOperation;
import org.is.bandmanager.service.imports.repository.specification.ImportOperationFilter;
import org.is.bandmanager.service.imports.repository.specification.ImportOperationSpecification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface ImportOperationRepository extends JpaRepository<ImportOperation, Long>, JpaSpecificationExecutor<ImportOperation> {

    default Page<ImportOperation> findByUserUsername(ImportOperationFilter filter, String username, Pageable pageable) {
        ImportOperationFilter adjustedFilter = filter != null ? filter : ImportOperationFilter.builder().build();
        adjustedFilter.setUsername(username);
        Specification<ImportOperation> specification = ImportOperationSpecification.withFilter(adjustedFilter);
        return findAll(specification, pageable);
    }

    default Page<ImportOperation> find(ImportOperationFilter filter, Pageable pageable) {
        Specification<ImportOperation> specification = ImportOperationSpecification.withFilter(filter);
        return findAll(specification, pageable);
    }

}