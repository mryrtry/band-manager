package org.is.bandmanager.service.imports.repository.specification;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.is.bandmanager.repository.filter.EntityFilter;
import org.is.bandmanager.service.imports.model.ImportStatus;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Getter
@Setter
@Builder
public class ImportOperationFilter implements EntityFilter {
    private String username;
    private String filename;
    private ImportStatus importStatus;
    private Integer createdEntitiesCountFrom;
    private Integer createdEntitiesCountTo;
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate startedBefore;
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate startedAfter;
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate completedBefore;
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate completedAfter;
}
