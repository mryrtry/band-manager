package org.is.bandmanager.model.dto;

import org.is.bandmanager.model.Person;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {LocationMapper.class})
public interface PersonMapper {

    @Mapping(target = "location", source = "location")
    PersonDto toDto(Person person);

    @Mapping(target = "location", source = "location")
    Person toEntity(PersonDto personDto);

}