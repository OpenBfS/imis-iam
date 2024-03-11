/* Copyright (C) 2022 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY!
 */
package de.intevation.iam.model.representation;

import org.keycloak.models.RoleModel;


/**
 * Readonly representation of a user role.
 */
public class Role {

    private String name;
    private String description;

    /**
     * Create a Role instance from the given role model.
     * @param role Role model to use
     */
    public Role(RoleModel role) {
        this.name = role.getName();
        this.description = role.getDescription();
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }
}
