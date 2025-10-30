package org.is.bandmanager.service;

import jakarta.validation.Valid;
import org.is.bandmanager.dto.MusicBandDto;
import org.is.bandmanager.dto.request.MusicBandRequest;
import org.is.bandmanager.model.MusicBand;
import org.is.bandmanager.dto.request.MusicBandFilter;
import org.is.bandmanager.service.pageable.PageableConfig;
import org.springframework.data.domain.Page;

import java.util.Date;
import java.util.List;


public interface MusicBandService {

    MusicBandDto create(@Valid MusicBandRequest request);

    Page<MusicBandDto> getAll(@Valid MusicBandFilter filter, PageableConfig config);

    MusicBandDto get(Integer id);

    MusicBand getEntity(Integer id);

    MusicBandDto getWithMaxCoordinates();

    List<MusicBandDto> getByEstablishmentDateBefore(Date date);

    List<Long> getDistinctAlbumsCount();

    MusicBandDto update(Integer id, @Valid MusicBandRequest request);

    MusicBandDto delete(Integer id);

    List<MusicBandDto> delete(List<Integer> ids);

    MusicBandDto removeParticipant(Integer id);

}
