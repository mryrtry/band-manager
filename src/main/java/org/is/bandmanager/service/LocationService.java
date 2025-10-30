package org.is.bandmanager.service;

import jakarta.validation.Valid;
import org.is.bandmanager.dto.LocationDto;
import org.is.bandmanager.dto.request.LocationRequest;
import org.is.bandmanager.model.Location;

import java.util.List;

public interface LocationService {

	LocationDto create(@Valid LocationRequest request);

	List<LocationDto> getAll();

	LocationDto get(Long id);

	Location getEntity(Long id);

	LocationDto update(Long id, @Valid LocationRequest request);

	LocationDto delete(Long id);

}
