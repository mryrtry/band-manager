package org.is.bandmanager.service.imports;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.is.auth.model.User;
import org.is.auth.service.user.UserService;
import org.is.bandmanager.dto.importRequest.MusicBandImportRequest;
import org.is.bandmanager.service.imports.model.ImportOperation;
import org.is.bandmanager.service.imports.model.ImportStatus;
import org.is.bandmanager.service.imports.parser.FileParserFacade;
import org.is.bandmanager.service.imports.processor.MusicBandImportProcessor;
import org.is.bandmanager.service.imports.repository.ImportOperationRepository;
import org.is.bandmanager.service.imports.repository.specification.ImportOperationFilter;
import org.is.util.pageable.PageableFactory;
import org.is.util.pageable.PageableRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ImportServiceImpl implements ImportService {

    private static final int MAX_ERROR_MESSAGE_LENGTH = 1000;

    private final ImportOperationRepository repository;

    private final FileParserFacade fileParserFacade;

    private final MusicBandImportProcessor processor;

    private final UserService userService;

    private final PageableFactory pageableFactory;

    @Override
    public ImportOperation startImport(MultipartFile file) {
        User user = userService.getEntity(userService.getAuthenticatedUser().getId());

        ImportOperation operation = ImportOperation.builder()
                .user(user)
                .filename(file.getOriginalFilename())
                .status(ImportStatus.PENDING)
                .startedAt(LocalDateTime.now())
                .build();

        ImportOperation savedOperation = repository.save(operation);

        processImportAsync(savedOperation, file);

        return savedOperation;
    }

    @Override
    @Async("importTaskExecutor")
    public void processImportAsync(ImportOperation operation, MultipartFile file) {
        try {
            log.info("Starting import processing for operation {}: {}", operation.getId(), file.getOriginalFilename());
            operation.setStatus(ImportStatus.PROCESSING);
            repository.save(operation);
            List<MusicBandImportRequest> importRequests = fileParserFacade.parseFile(file);
            log.info("Parsed {} records from file {}", importRequests.size(), file.getOriginalFilename());
            if (importRequests.isEmpty()) {
                throw new IllegalArgumentException("Import file is empty");
            }
            List<Long> createdBandIds = processor.processImport(importRequests);
            operation.setStatus(ImportStatus.COMPLETED);
            operation.setCreatedEntitiesCount(createdBandIds.size());
            operation.setCompletedAt(LocalDateTime.now());
            log.info("Import operation {} completed successfully. Created {} MusicBand entities",
                    operation.getId(), createdBandIds.size());
        } catch (Exception e) {
            log.error("Import operation {} failed: {}", operation.getId(), e.getMessage());
            operation.setStatus(ImportStatus.FAILED);
            operation.setErrorMessage(getErrorMessage(e));
            operation.setCompletedAt(LocalDateTime.now());
            repository.save(operation);
        }
    }

    @Override
    public Page<ImportOperation> getUserImportHistory(ImportOperationFilter filter, PageableRequest config) {
        String username = userService.getAuthenticatedUser().getUsername();
        Pageable pageable = pageableFactory.create(config, ImportOperation.class);
        return repository.findByUserUsername(filter, username, pageable);
    }

    @Override
    public Page<ImportOperation> getAllImportHistory(ImportOperationFilter filter, PageableRequest config) {
        Pageable pageable = pageableFactory.create(config, ImportOperation.class);
        return repository.find(filter, pageable);
    }

    @Override
    public ImportOperation getImportOperation(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Import operation not found: " + id));
    }

    @Override
    public List<String> getSupportedFormats() {
        return fileParserFacade.getSupportedFormats();
    }


    private String getErrorMessage(Exception e) {
        String message = e.getMessage();
        if (message != null && message.length() > MAX_ERROR_MESSAGE_LENGTH) {
            message = message.substring(0, MAX_ERROR_MESSAGE_LENGTH) + "... [truncated]";
        }
        return message;
    }

}