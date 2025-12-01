package org.is.bandmanager.dto.importRequest;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.is.bandmanager.model.Color;
import org.is.bandmanager.model.Country;


@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PersonImportRequest {

	@NotBlank(message = "Person.Name не может быть пустым")
	private String name;

	@NotNull(message = "Person.EyeColor не может быть пустым")
	private Color eyeColor;

	@NotNull(message = "Person.HairColor не может быть пустым")
	private Color hairColor;

	@Valid
	@NotNull(message = "Person.Location не может быть пустым")
	private LocationImportRequest location;

	@NotNull(message = "Person.Weight не может быть пустым")
	@DecimalMin(value = "0", inclusive = false, message = "Person.Weight должно быть > 0")
	private Float weight;

	@NotNull(message = "Person.Country не может быть пустым")
	private Country nationality;

}
