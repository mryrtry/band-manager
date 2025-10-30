package org.is.bandmanager.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;


@Builder
@Data
public class LocationRequest {

    private Integer x;

    @NotNull(message = "Location.Y не может быть пустым")
    private Long y;

    @NotNull(message = "Location.Z не может быть пустым")
    private Long z;

}
