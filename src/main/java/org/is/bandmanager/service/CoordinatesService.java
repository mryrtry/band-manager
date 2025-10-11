package org.is.bandmanager.service;

import org.is.bandmanager.dto.CoordinatesDto;
import org.is.bandmanager.dto.request.CoordinatesRequest;

import java.util.List;

public interface CoordinatesService {

    CoordinatesDto create(CoordinatesRequest request);

    List<CoordinatesDto> getAll();

    CoordinatesDto get(Long id);

    CoordinatesDto update(Long id, CoordinatesRequest request);

    CoordinatesDto delete(Long id);

}
