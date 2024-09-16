/* Copyright (C) 2024 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY!
 */
package de.intevation.iam.validation;

import jakarta.persistence.EntityManager;

public interface EntityManagerAwareValidator {
    void setEntityManager(EntityManager entityManager);
}
