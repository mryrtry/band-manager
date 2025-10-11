package org.is.bandmanager.service;

import jakarta.validation.Valid;
import org.is.bandmanager.dto.PersonDto;
import org.is.bandmanager.dto.request.PersonRequest;

import java.util.List;

public interface PersonService {

    PersonDto create(@Valid PersonRequest request);

    List<PersonDto> getAll();

    PersonDto get(Long id);

    PersonDto update(Long id, @Valid PersonRequest request);

    PersonDto delete(Long id);

}
