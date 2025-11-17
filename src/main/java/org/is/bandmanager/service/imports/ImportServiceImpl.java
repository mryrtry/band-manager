package org.is.bandmanager.service.imports;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.is.auth.model.User;
import org.is.auth.service.user.UserService;
import org.is.bandmanager.service.imports.handler.ImportHandler;
import org.is.bandmanager.service.imports.model.ImportOperation;
import org.is.bandmanager.service.imports.model.ImportStatus;
import org.is.bandmanager.service.imports.parser.FileParserFacade;
import org.is.bandmanager.service.imports.repository.ImportOperationRepository;
import org.is.bandmanager.service.imports.repository.specification.ImportOperationFilter;
import org.is.util.pageable.PageableFactory;
import org.is.util.pageable.PageableRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

// todo: mass refactor
@Slf4j
@Service
@RequiredArgsConstructor
public class ImportServiceImpl implements ImportService {

    private final ImportOperationRepository repository;

    private final FileParserFacade fileParserFacade;

    private final UserService userService;

    private final PageableFactory pageableFactory;

	private final ImportHandler handler;

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
		byte[] fileContent;
		try {
			fileContent = file.getBytes();
		} catch (IOException e) {
			log.error("Failed to read file content: {}", file.getOriginalFilename());
			operation.setStatus(ImportStatus.FAILED);
			operation.setErrorMessage("Failed to read file content: " + file.getOriginalFilename());
			repository.save(operation);
			throw new RuntimeException("Failed to read file content: " + file.getOriginalFilename());
		}
		handler.processImport(savedOperation.getId(), fileContent, file.getOriginalFilename(), file.getContentType());
		return savedOperation;
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

}