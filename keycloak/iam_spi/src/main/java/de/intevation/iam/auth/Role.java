/* Copyright (C) 2022 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY!
 */
package de.intevation.iam.auth;

import org.keycloak.models.KeycloakSession;
import org.keycloak.models.UserModel;

import de.intevation.iam.util.Constants;

/**
 * The user roles known to this application.
 */
public enum Role {
    USER,
    EDITOR,
    CHIEF_EDITOR;

    @Override
    public String toString() {
        return this.name().toLowerCase();
    }

    /**
     * Check whether a given user has this role.
     *
     * @param user The user to check.
     * @param session object used to fetch the RoleModel for this role.
     * @return true if user has this role, else false.
     */
    public boolean isRoleOf(UserModel user, KeycloakSession session) {
        return user.hasRole(session.getContext().getRealm().getClientByClientId(
                Constants.IAM_CLIENT_ID).getRole(this.toString()));
    }
}
