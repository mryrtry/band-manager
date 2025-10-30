package org.is.bandmanager.service;

import jakarta.validation.Valid;
import org.is.bandmanager.dto.CoordinatesDto;
import org.is.bandmanager.dto.request.CoordinatesRequest;
import org.is.bandmanager.model.Coordinates;

import java.util.List;


public interface CoordinatesService {

    CoordinatesDto create(@Valid CoordinatesRequest request);

    List<CoordinatesDto> getAll();

    CoordinatesDto get(Long id);

    Coordinates getEntity(Long id);

    CoordinatesDto update(Long id, @Valid CoordinatesRequest request);

    CoordinatesDto delete(Long id);

}
