package org.is.bandmanager.dto;

import org.is.bandmanager.dto.request.PersonRequest;
import org.is.bandmanager.model.Location;
import org.is.bandmanager.model.Person;
import org.is.bandmanager.service.LocationService;
import org.mapstruct.*;

@Mapper(componentModel = "spring", uses = {LocationMapper.class})
public abstract class PersonMapper {

    @Mapping(target = "location", source = "location")
    public abstract PersonDto toDto(Person person);

    @Mapping(target = "location", ignore = true)
    public abstract Person toEntity(PersonRequest request);

    @Mapping(target = "location", source = "location")
    public abstract Person toEntity(PersonDto personDto);

    @AfterMapping
    protected void mapLocationIdToLocation(PersonRequest request,
                                           @MappingTarget Person person,
                                           @Context LocationService locationService) {
        if (request.getLocationId() != null) {
            Location location = locationService.getEntity(request.getLocationId());
            person.setLocation(location);
        }
    }

}