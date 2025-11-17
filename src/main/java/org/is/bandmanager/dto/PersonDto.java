package org.is.bandmanager.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.is.bandmanager.model.Color;
import org.is.bandmanager.model.Country;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PersonDto {

    private Long id;
    private String name;
    private Color eyeColor;
    private Color hairColor;
    private LocationDto location;
    private Float weight;
    private Country nationality;
    private String createdBy;
    private LocalDateTime createdDate;
    private String lastModifiedBy;
    private LocalDateTime lastModifiedDate;

}