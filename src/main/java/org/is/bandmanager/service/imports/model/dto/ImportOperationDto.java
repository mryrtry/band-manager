package org.is.bandmanager.service.imports.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.is.bandmanager.service.imports.model.ImportOperation;
import org.is.bandmanager.service.imports.model.ImportStatus;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImportOperationDto {

    private Long id;
    private String filename;
    private ImportStatus status;
    private Integer createdEntitiesCount;
    private String errorMessage;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private String createdBy;
    private LocalDateTime createdDate;
    private String lastModifiedBy;
    private LocalDateTime lastModifiedDate;

    public static ImportOperationDto toDto(ImportOperation operation) {
        return ImportOperationDto.builder()
                .id(operation.getId())
                .filename(operation.getFilename())
                .status(operation.getStatus())
                .createdEntitiesCount(operation.getCreatedEntitiesCount())
                .errorMessage(operation.getErrorMessage())
                .startedAt(operation.getStartedAt())
                .completedAt(operation.getCompletedAt())
                .createdBy(operation.getCreatedBy())
                .createdDate(operation.getCreatedDate())
                .build();
    }

}