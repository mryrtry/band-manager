package org.is.bandmanager.service;

import org.is.bandmanager.dto.PersonDto;
import org.is.bandmanager.dto.request.PersonRequest;

import java.util.List;

public interface PersonService {

    PersonDto create(PersonRequest request);

    List<PersonDto> getAll();

    PersonDto get(Long id);

    PersonDto update(Long id, PersonRequest request);

    PersonDto delete(Long id);

}
