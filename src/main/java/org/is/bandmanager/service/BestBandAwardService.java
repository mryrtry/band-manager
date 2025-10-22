package org.is.bandmanager.service;

import jakarta.validation.Valid;
import org.is.bandmanager.dto.BestBandAwardDto;
import org.is.bandmanager.dto.request.BestBandAwardRequest;
import org.is.bandmanager.repository.filter.BestBandAwardFilter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface BestBandAwardService {

    BestBandAwardDto create(@Valid BestBandAwardRequest request);

    Page<BestBandAwardDto> getAll(BestBandAwardFilter filter, Pageable pageable);

    BestBandAwardDto get(Long id);

    BestBandAwardDto update(Long id, @Valid BestBandAwardRequest request);

    BestBandAwardDto delete(Long id);

    List<BestBandAwardDto> delete(List<Long> ids);

}
