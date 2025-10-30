package org.is.bandmanager.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;
import org.is.bandmanager.model.Color;
import org.is.bandmanager.model.Country;

@Builder
@Data
public class PersonRequest {

	@NotBlank(message = "Person.Name не может быть пустым")
	private String name;

	@NotNull(message = "Person.EyeColor не может быть пустым")
	private Color eyeColor;

	@NotNull(message = "Person.HairColor не может быть пустым")
	private Color hairColor;

	@NotNull(message = "Person.LocationId не может быть пустым")
	private Long locationId;

	@NotNull(message = "Person.Weight не может быть пустым")
	@DecimalMin(value = "0", inclusive = false, message = "Person.Weight должно быть > 0")
	private Float weight;

	@NotNull(message = "Person.Country не может быть пустым")
	private Country nationality;

}
