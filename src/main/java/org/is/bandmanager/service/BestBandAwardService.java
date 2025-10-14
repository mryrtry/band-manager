package org.is.bandmanager.service;

import jakarta.validation.Valid;
import org.is.bandmanager.dto.BestBandAwardDto;
import org.is.bandmanager.dto.request.BestBandAwardRequest;
import org.is.bandmanager.model.MusicGenre;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface BestBandAwardService {

    BestBandAwardDto create(@Valid BestBandAwardRequest request);

    Page<BestBandAwardDto> getAll(MusicGenre genre, String bandName,
                                  Integer bandId, Pageable pageable);

    BestBandAwardDto get(Long id);

    BestBandAwardDto update(Long id, @Valid BestBandAwardRequest request);

    BestBandAwardDto delete(Long id);

    void deleteAllByBandId(Integer bandId);

}
