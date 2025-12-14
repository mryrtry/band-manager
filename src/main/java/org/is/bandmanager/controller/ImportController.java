package org.is.bandmanager.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.is.auth.service.security.SecurityService;
import org.is.bandmanager.service.imports.ImportService;
import org.is.bandmanager.service.imports.model.ImportOperation;
import org.is.bandmanager.service.imports.model.dto.ImportOperationDto;
import org.is.bandmanager.service.imports.repository.specification.ImportOperationFilter;
import org.is.util.pageable.PageableRequest;
import org.springframework.data.domain.Page;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/import")
@RequiredArgsConstructor
public class ImportController {

    private final ImportService importService;

    private final SecurityService securityService;

    @PostMapping("/music-bands")
    @PreAuthorize("@securityService.canCreate()")
    public ResponseEntity<ImportOperationDto> importMusicBands(@RequestParam("file") MultipartFile file) {
        log.info("Starting music bands import from file: {}", file.getOriginalFilename());

        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        ImportOperation operation = importService.startImport(file);

        ImportOperationDto operationDto = ImportOperationDto.toDto(operation);

        log.info("Import operation started: {}", operation.getId());
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(operationDto);
    }

    @GetMapping("/operations")
    @PreAuthorize("@securityService.hasAnyPermission('READ_OWN_IMPORT', 'READ_ALL_IMPORT')")
    public ResponseEntity<Page<ImportOperationDto>> getImportOperations(
            @ModelAttribute @Valid ImportOperationFilter filter,
            @ModelAttribute @Valid PageableRequest config) {

        Page<ImportOperationDto> operations;
        if (securityService.hasPermission("READ_ALL_IMPORT")) {
            operations = importService.getAllImportHistory(filter, config)
                    .map(ImportOperationDto::toDto);
        } else {
            operations = importService.getUserImportHistory(filter, config)
                    .map(ImportOperationDto::toDto);
        }

        return ResponseEntity.ok(operations);
    }

    @GetMapping("/operations/{id}")
    @PreAuthorize("@securityService.canReadImport(#id, 'ImportOperation')")
    public ResponseEntity<ImportOperationDto> getImportOperation(@PathVariable Long id) {
        ImportOperation operation = importService.getImportOperation(id);
        return ResponseEntity.ok(ImportOperationDto.toDto(operation));
    }

    @GetMapping("/operations/{id}/file")
    @PreAuthorize("@securityService.canReadImport(#id, 'ImportOperation')")
    public ResponseEntity<InputStreamResource> downloadImportFile(@PathVariable Long id) {
        var stored = importService.downloadImportFile(id);
        MediaType mediaType = Optional.ofNullable(stored.contentType())
                .map(MediaType::parseMediaType)
                .orElse(MediaType.APPLICATION_OCTET_STREAM);
        return ResponseEntity.ok()
                .contentType(mediaType)
                .contentLength(stored.size())
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + stored.filename() + "\"")
                .body(new InputStreamResource(stored.inputStream()));
    }

    @GetMapping("/supported-formats")
    @PreAuthorize("@securityService.canCreate()")
    public ResponseEntity<List<String>> getSupportedFormats() {
        return ResponseEntity.ok(importService.getSupportedFormats());
    }

}
