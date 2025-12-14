package org.is.bandmanager.service.imports.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.is.bandmanager.service.imports.model.ImportOperation;
import org.is.bandmanager.service.imports.model.ImportStatus;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.time.LocalDateTime;
import java.util.Optional;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImportOperationDto {

    private Long id;
    private String filename;
    private String storageObjectKey;
    private String contentType;
    private Long fileSize;
    private String downloadUrl;
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
        String downloadUrl = Optional.ofNullable(operation.getStorageObjectKey())
                .map(key -> ServletUriComponentsBuilder.fromCurrentContextPath()
                        .path("/import/operations/")
                        .path(String.valueOf(operation.getId()))
                        .path("/file")
                        .toUriString())
                .orElse(null);

        return ImportOperationDto.builder()
                .id(operation.getId())
                .filename(operation.getFilename())
                .storageObjectKey(operation.getStorageObjectKey())
                .contentType(operation.getContentType())
                .fileSize(operation.getFileSize())
                .downloadUrl(downloadUrl)
                .status(operation.getStatus())
                .createdEntitiesCount(operation.getCreatedEntitiesCount())
                .errorMessage(operation.getErrorMessage())
                .startedAt(operation.getStartedAt())
                .completedAt(operation.getCompletedAt())
                .createdBy(operation.getCreatedBy())
                .createdDate(operation.getCreatedDate())
                .lastModifiedBy(operation.getLastModifiedBy())
                .lastModifiedDate(operation.getLastModifiedDate())
                .build();
    }

}
