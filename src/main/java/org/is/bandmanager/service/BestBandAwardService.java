package org.is.bandmanager.service;

import jakarta.validation.Valid;
import org.is.bandmanager.dto.BestBandAwardDto;
import org.is.bandmanager.dto.request.BestBandAwardRequest;
import org.is.bandmanager.dto.request.BestBandAwardFilter;
import org.is.bandmanager.service.pageable.PageableConfig;
import org.springframework.data.domain.Page;

import java.util.List;

public interface BestBandAwardService {

    BestBandAwardDto create(@Valid BestBandAwardRequest request);

    Page<BestBandAwardDto> getAll(BestBandAwardFilter filter, PageableConfig config);

    BestBandAwardDto get(Long id);

    BestBandAwardDto update(Long id, @Valid BestBandAwardRequest request);

    BestBandAwardDto delete(Long id);

    List<BestBandAwardDto> delete(List<Long> ids);

}
