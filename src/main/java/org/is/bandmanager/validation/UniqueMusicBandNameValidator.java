package org.is.bandmanager.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.is.bandmanager.repository.MusicBandRepository;
import org.springframework.beans.factory.annotation.Autowired;


public class UniqueMusicBandNameValidator implements ConstraintValidator<UniqueMusicBandName, String> {

	@Autowired
	private MusicBandRepository musicBandRepository;

	private long excludeId;

	@Override
	public void initialize(UniqueMusicBandName constraintAnnotation) {
		this.excludeId = constraintAnnotation.excludeId();
	}

	@Override
	public boolean isValid(String name, ConstraintValidatorContext context) {
		if (name == null || name.trim().isEmpty()) {
			return true;
		}

		boolean exists = musicBandRepository.existsByNameAndIdNot(name, excludeId);

		if (exists) {
			context.buildConstraintViolationWithTemplate("Группа с именем '%s' уже существует".formatted(name)).addConstraintViolation().disableDefaultConstraintViolation();
			return false;
		}

		return true;
	}

}
