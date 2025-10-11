package org.is.bandmanager.service;

import org.is.bandmanager.dto.LocationDto;
import org.is.bandmanager.dto.request.LocationRequest;

import java.util.List;

public interface LocationService {

    LocationDto create(LocationRequest request);

    List<LocationDto> getAll();

    LocationDto get(Long id);

    LocationDto update(Long id, LocationRequest request);

    LocationDto delete(Long id);

}
