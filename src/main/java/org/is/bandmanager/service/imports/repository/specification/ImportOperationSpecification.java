package org.is.bandmanager.service.imports.repository.specification;

import org.is.bandmanager.service.imports.model.ImportOperation;
import org.springframework.data.jpa.domain.Specification;

public final class ImportOperationSpecification {

    public static Specification<ImportOperation> withFilter(ImportOperationFilter filter) {
        if (filter == null) {
            return null;
        }

        return Specification.<ImportOperation>where(
                        (root, query, cb) -> filter.getUsername() != null ?
                                cb.equal(root.get("user").get("username"), filter.getUsername()) : null)
                .and((root, query, cb) ->
                        filter.getFilename() != null ?
                                cb.like(cb.lower(root.get("filename")), filter.getFilename()) : null)
                .and((root, query, cb) ->
                        filter.getImportStatus() != null ?
                                cb.equal(root.get("status"), filter.getImportStatus()) : null)
                .and((root, query, cb) ->
                        filter.getCreatedEntitiesCountFrom() != null ?
                                cb.greaterThanOrEqualTo(root.get("createdEntitiesCount"), filter.getCreatedEntitiesCountFrom()) : null)
                .and((root, query, cb) ->
                        filter.getCreatedEntitiesCountTo() != null ?
                                cb.lessThanOrEqualTo(root.get("createdEntitiesCount"), filter.getCreatedEntitiesCountTo()) : null)
                .and((root, query, cb) ->
                        filter.getStartedAfter() != null ?
                                cb.greaterThanOrEqualTo(root.get("startedAt"), filter.getStartedAfter()) : null)
                .and((root, query, cb) ->
                        filter.getStartedBefore() != null ?
                                cb.lessThanOrEqualTo(root.get("startedAt"), filter.getStartedBefore()) : null)
                .and((root, query, cb) ->
                        filter.getCompletedAfter() != null ?
                                cb.greaterThanOrEqualTo(root.get("completedAt"), filter.getCompletedAfter()) : null)
                .and((root, query, cb) ->
                        filter.getCompletedBefore() != null ?
                                cb.lessThanOrEqualTo(root.get("completedAt"), filter.getCompletedBefore()) : null);
    }
}