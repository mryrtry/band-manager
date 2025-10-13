package org.is.bandmanager.service;

import jakarta.validation.Valid;
import org.is.bandmanager.dto.MusicBandDto;
import org.is.bandmanager.dto.request.MusicBandRequest;

import java.util.List;

public interface MusicBandService {

    MusicBandDto create(@Valid MusicBandRequest request);

    List<MusicBandDto> getAll();

    MusicBandDto get(Integer id);

    MusicBandDto getWithMaxCoordinates();

    MusicBandDto update(Integer id, @Valid MusicBandRequest request);

    MusicBandDto delete(Integer id);

}
