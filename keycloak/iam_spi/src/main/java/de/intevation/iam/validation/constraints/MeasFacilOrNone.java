/* Copyright (C) 2024 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY!
 */
package de.intevation.iam.validation.constraints;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;


/**
 * Checks if the validated entity has either both MeasFacil ID and name
 * or none of them.
 */
@Target({ TYPE })
@Retention(RUNTIME)
@Constraint(validatedBy = { MeasFacilOrNoneValidator.class })
@Documented
public @interface MeasFacilOrNone {

    static final String MSG =
        "{de.intevation.iam.validation.constraints.MeasFacilOrNone.message}";

    String message() default MSG;

    Class<?>[] groups() default { };

    Class<? extends Payload>[] payload() default { };
}
