/* Copyright (C) 2024 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY!
 */
package de.intevation.iam.validation;

import jakarta.persistence.EntityManager;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorFactory;

public class ConstraintValidatorFactoryImpl
    implements ConstraintValidatorFactory {

    private final EntityManager entityManager;

    public ConstraintValidatorFactoryImpl(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public <T extends ConstraintValidator<?, ?>> T getInstance(Class<T> key) {
        T instance;
        try {
            instance = key.getDeclaredConstructor().newInstance();
        } catch (ReflectiveOperationException e) {
            // could not instantiate class
            throw new RuntimeException(e);
        }

        if (EntityManagerAwareValidator.class.isAssignableFrom(key)) {
            EntityManagerAwareValidator validator =
                (EntityManagerAwareValidator) instance;
            validator.setEntityManager(entityManager);
        }

        return instance;
    }

    @Override
    public void releaseInstance(
        ConstraintValidator<?, ?> constraintValidator) { }
}
