package org.is.bandmanager.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = UniqueMusicBandNameValidator.class)
public @interface UniqueMusicBandName {

	String message() default "MusicBand.name должно быть уникальным";

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};

	long excludeId() default 0L;

}
