package org.is.bandmanager.service;

import org.is.bandmanager.dto.MusicBandDto;
import org.is.bandmanager.dto.request.MusicBandRequest;

import java.util.List;

public interface MusicBandService {

    MusicBandDto create(MusicBandRequest request);

    List<MusicBandDto> getAll();

    MusicBandDto get(Integer id);

    MusicBandDto update(Integer id, MusicBandRequest request);

    MusicBandDto delete(Integer id);

}
