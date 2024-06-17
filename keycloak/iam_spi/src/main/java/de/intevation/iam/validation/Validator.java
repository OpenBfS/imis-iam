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

import jakarta.persistence.EntityManager;
import jakarta.validation.ValidatorContext;
import jakarta.validation.ValidatorFactory;
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

    public static class ValidationError {
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

    public static class BeanKey {
        Locale locale;
        EntityManager entityManager;

        public BeanKey(Locale locale, EntityManager entityManager) {
            this.locale = locale;
            this.entityManager = entityManager;
        }

        @Override
        public int hashCode() {
            return locale.hashCode() ^ entityManager.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof BeanKey bk)) {
                return false;
            }
            if (this == obj) {
                return true;
            }
            return locale.equals(bk.locale) && entityManager.equals(bk.entityManager);
        }
    }

    private final HashMap<BeanKey, jakarta.validation.Validator> beanValidators =
        new HashMap<>();

    /**
     * @param locale Set locale for message localization.
     * @return Validator with locale set.
     */
    private synchronized jakarta.validation.Validator getBeanValidator(
        Locale locale,
        EntityManager em
    ) {
        if (locale == null) {
            locale = Locale.getDefault();
        }
        jakarta.validation.Validator validator = beanValidators.get(new BeanKey(locale, em));
        if (validator == null) {
            ValidatorFactory validatorFactory = Validation.byProvider(HibernateValidator.class)
                .configure()
                .defaultLocale(locale)
                .buildValidatorFactory();
            ValidatorContext validatorContext = validatorFactory.usingContext();
            validatorContext.constraintValidatorFactory(new ConstraintValidatorFactoryImpl(em));
            validator = validatorContext.getValidator();
            beanValidators.put(new BeanKey(locale, em), validator);
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
    public void validate(Object object, Locale locale, EntityManager em) {
        jakarta.validation.Validator beanValidator = getBeanValidator(locale, em);
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
