package org.is.bandmanager.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LocationDto {

    private Long id;

    private Integer x;

    private Long y;

    private Long z;

}