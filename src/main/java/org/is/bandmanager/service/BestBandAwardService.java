package org.is.bandmanager.service;

import jakarta.validation.Valid;
import org.is.bandmanager.dto.BestBandAwardDto;
import org.is.bandmanager.dto.request.BestBandAwardRequest;

import java.util.List;

public interface BestBandAwardService {

    BestBandAwardDto create(@Valid BestBandAwardRequest request);

    List<BestBandAwardDto> getAll();

    BestBandAwardDto get(Long id);

    BestBandAwardDto update(Long id, @Valid BestBandAwardRequest request);

    BestBandAwardDto delete(Long id);

    void deleteAllByBandId(Integer bandId);

}
