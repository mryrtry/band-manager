package org.is.bandmanager.service.imports;

import org.is.bandmanager.service.imports.model.ImportOperation;
import org.is.bandmanager.service.imports.repository.specification.ImportOperationFilter;
import org.is.util.pageable.PageableRequest;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ImportService {

    ImportOperation startImport(MultipartFile file);

    void processImportAsync(ImportOperation operation, MultipartFile file);

    Page<ImportOperation> getUserImportHistory(ImportOperationFilter filter, PageableRequest pageable);

    Page<ImportOperation> getAllImportHistory(ImportOperationFilter filter, PageableRequest pageable);

    ImportOperation getImportOperation(Long id);

    List<String> getSupportedFormats();

}