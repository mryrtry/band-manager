package org.is.bandmanager.service.musicBand;

import jakarta.validation.Valid;
import org.is.bandmanager.dto.MusicBandDto;
import org.is.bandmanager.dto.request.MusicBandRequest;
import org.is.bandmanager.model.MusicBand;
import org.is.bandmanager.repository.filter.MusicBandFilter;
import org.is.util.pageable.PageableRequest;
import org.springframework.data.domain.Page;

import java.util.Date;
import java.util.List;


public interface MusicBandService {

    MusicBandDto create(@Valid MusicBandRequest request);

    Page<MusicBandDto> getAll(@Valid MusicBandFilter filter, PageableRequest config);

    MusicBandDto get(Long id);

    MusicBand getEntity(Long id);

    MusicBandDto getWithMaxCoordinates();

    List<MusicBandDto> getByEstablishmentDateBefore(Date date);

    List<Long> getDistinctAlbumsCount();

    MusicBandDto update(Long id, @Valid MusicBandRequest request);

    MusicBandDto delete(Long id);

    List<MusicBandDto> delete(List<Long> ids);

    MusicBandDto removeParticipant(Long id);

}
