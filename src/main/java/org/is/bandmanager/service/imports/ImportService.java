package org.is.bandmanager.service.imports;

import org.is.bandmanager.service.imports.model.ImportOperation;
import org.is.util.pageable.PageableConfig;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ImportService {

    ImportOperation startImport(MultipartFile file);

    void processImportAsync(Long operationId, MultipartFile file);

    Page<ImportOperation> getUserImportHistory(PageableConfig pageable);

    Page<ImportOperation> getAllImportHistory(PageableConfig pageable);

    ImportOperation getImportOperation(Long id);

    List<String> getSupportedFormats();

}