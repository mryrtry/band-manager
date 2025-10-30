package org.is.bandmanager.service;

import jakarta.validation.Valid;
import org.is.bandmanager.dto.PersonDto;
import org.is.bandmanager.dto.request.PersonRequest;
import org.is.bandmanager.model.Person;

import java.util.List;

public interface PersonService {

	PersonDto create(@Valid PersonRequest request);

	List<PersonDto> getAll();

	PersonDto get(Long id);

	Person getEntity(Long id);

	PersonDto update(Long id, @Valid PersonRequest request);

	PersonDto delete(Long id);

}
