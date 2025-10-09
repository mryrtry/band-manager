package org.is.bandmanager.model.dto;

import org.is.bandmanager.model.Person;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring", uses = {LocationMapper.class})
public interface PersonMapper {

    PersonMapper INSTANCE = Mappers.getMapper(PersonMapper.class);

    @Mapping(target = "location", source = "location")
    PersonDto toDto(Person person);

    @Mapping(target = "location", source = "location")
    Person toEntity(PersonDto personDto);

}