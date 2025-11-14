package org.is.bandmanager.service.imports.repository;

import org.is.bandmanager.service.imports.model.ImportOperation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface ImportOperationRepository extends JpaRepository<ImportOperation, Long>, JpaSpecificationExecutor<ImportOperation> {

    Page<ImportOperation> findByUserUsername(String username, Pageable pageable);

    default Page<ImportOperation> find(Pageable pageable) {
        return findAll(pageable);
    }

}