/* Copyright (C) 2024 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY!
 */
package de.intevation.iam.validation;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import org.hibernate.validator.HibernateValidator;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;


/**
 * Bean object validation.
 *
 * @author <a href="mailto:pschwabauer@intevation.de">Paul Schwabauer</a>
 */
public class Validator {

    public class ValidationError {
        private static final Object[] EMPTY_PARAMETERS = {};
        private final String message;
        private final Object[] messageParameters;

        ValidationError(String message, Object... messageParameters) {
            this.message = message;
            this.messageParameters = messageParameters == null
                ? EMPTY_PARAMETERS
                : messageParameters.clone();
        }

        public String getMessage() {
            return message;
        }

        public Object[] getMessageParameters() {
            return messageParameters;
        }
    }

    private HashMap<Locale, jakarta.validation.Validator> beanValidators =
        new HashMap<>();

    /**
     * @param locale Set locale for message localization.
     * @return Validator with locale set.
     */
    private synchronized jakarta.validation.Validator getBeanValidator(
        Locale locale
    ) {
        if (locale == null) {
            locale = Locale.getDefault();
        }
        jakarta.validation.Validator validator = beanValidators.get(locale);
        if (validator == null) {
            validator = Validation.byProvider(HibernateValidator.class)
                .configure()
                .defaultLocale(locale)
                .buildValidatorFactory()
                .getValidator();
            beanValidators.put(locale, validator);
        }
        return validator;
    }

    /**
     * Validate objects with bean validation.
     *
     * @param object The object to be validated
     * @param locale The language of the validation message
     * @throws BadRequestException in case validation fails
     */
    public void validate(Object object, Locale locale) {
        jakarta.validation.Validator beanValidator = getBeanValidator(locale);
        Set<ConstraintViolation<Object>> beanViolations =
            beanValidator.validate(object);
        if (!beanViolations.isEmpty()) {
            Set<ValidationError> validationErrors =
                new HashSet<ValidationError>();

            for (ConstraintViolation<Object> violation: beanViolations) {
                validationErrors.add(new ValidationError(
                    violation.getMessage(),
                    violation.getPropertyPath().toString()));
            }
            throw new BadRequestException(
                Response.status(Status.BAD_REQUEST)
                    .type(MediaType.APPLICATION_JSON)
                    .entity(validationErrors)
                    .build());
        }
    }
}
