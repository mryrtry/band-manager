package org.is.bandmanager.service.imports.repository;

import org.is.bandmanager.service.imports.model.ImportOperation;
import org.is.bandmanager.service.imports.model.ImportStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ImportOperationRepository extends JpaRepository<ImportOperation, Long> {

    Page<ImportOperation> findByUserIdOrderByCreatedDateDesc(Long userId, Pageable pageable);

    Page<ImportOperation> findAllByOrderByCreatedDateDesc(Pageable pageable);

    List<ImportOperation> findByStatus(ImportStatus status);

    @Query("SELECT io FROM ImportOperation io WHERE io.user.username = :username ORDER BY io.createdDate DESC")
    Page<ImportOperation> findByUsernameOrderByCreatedDateDesc(@Param("username") String username, Pageable pageable);

}