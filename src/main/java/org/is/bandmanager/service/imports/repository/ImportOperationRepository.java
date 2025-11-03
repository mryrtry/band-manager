package org.is.bandmanager.service.imports.repository;

import org.is.bandmanager.service.imports.model.ImportOperation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ImportOperationRepository extends JpaRepository<ImportOperation, Long> {

    Page<ImportOperation> findAllByOrderByCreatedDateDesc(Pageable pageable);

    @Query("SELECT io FROM ImportOperation io WHERE io.user.username = :username ORDER BY io.createdDate DESC")
    Page<ImportOperation> findByUsernameOrderByCreatedDateDesc(@Param("username") String username, Pageable pageable);

}