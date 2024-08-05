/* Copyright (C) 2024 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY!
 */
package de.intevation.iam.validation.constraints;

import de.intevation.iam.model.jpa.Institution;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;


/**
 * Checks if the validated entity references either MeasFacil or none.
 */
public class MeasFacilOrNoneValidator
    implements ConstraintValidator<MeasFacilOrNone, Institution> {

    private String message;

    @Override
    public void initialize(MeasFacilOrNone constraintAnnotation) {
        this.message = constraintAnnotation.message();
    }

    @Override
    public boolean isValid(Institution institution, ConstraintValidatorContext ctx) {
        if (institution == null
            || institution.getMeasFacilName() == null
            || institution.getMeasFacilId() == null
        ) {
            return true;
        }

        if (!institution.getMeasFacilName().isBlank()
            && !institution.getMeasFacilId().isBlank()) {
                return true;
            }


        final String measFacilName = "measFacilName",
            measFacilId = "measFacilId";
        ctx.disableDefaultConstraintViolation();
        ctx.buildConstraintViolationWithTemplate(this.message)
            .addPropertyNode(measFacilName)
            .addConstraintViolation();
        ctx.buildConstraintViolationWithTemplate(this.message)
            .addPropertyNode(measFacilId)
            .addConstraintViolation();
        return false;
    }
}
