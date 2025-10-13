package org.is.bandmanager.dto;

import org.is.bandmanager.dto.request.PersonRequest;
import org.is.bandmanager.model.Person;
import org.is.bandmanager.service.LocationService;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(componentModel = "spring")
public abstract class PersonMapper {

    @Autowired
    private LocationService locationService;

    @Mapping(target = "location", source = "location")
    public abstract PersonDto toDto(Person person);

    @Mapping(target = "location", source = "location")
    public abstract Person toEntity(PersonDto personDto);

    @Mapping(target = "location", ignore = true)
    public Person toEntity(PersonRequest request) {
        return Person.builder()
                .name(request.getName())
                .eyeColor(request.getEyeColor())
                .hairColor(request.getHairColor())
                .location(locationService.getEntity(request.getLocationId()))
                .weight(request.getWeight())
                .nationality(request.getNationality())
                .build();
    }

}