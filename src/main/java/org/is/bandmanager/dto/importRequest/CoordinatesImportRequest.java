package org.is.bandmanager.dto.importRequest;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CoordinatesImportRequest {

	@NotNull(message = "Coordinates.X не может быть пустым")
	@DecimalMin(value = "-147", inclusive = false, message = "Coordinates.X должно быть больше -147")
	private Integer x;

	private Float y;

}
