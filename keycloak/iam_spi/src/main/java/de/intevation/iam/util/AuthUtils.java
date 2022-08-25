/* Copyright (C) 2022 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY!
 */
package de.intevation.iam.util;

import java.util.stream.Stream;

import org.keycloak.models.ClientModel;
import org.keycloak.models.RoleModel;
import org.keycloak.models.UserModel;

import de.intevation.iam.auth.Role;

/**
 * Utility methods used by the authorizers.
 */
public class AuthUtils {
    private AuthUtils() { };

    /**
     * Checks if a user has at least the "Nutzer" role.
     * @param requestingUser User to check.
     * @param client Client to check in
     * @return True if user has at least "Nutzer" role.
     */
    public static boolean isUserAtLeastNutzer(
        UserModel requestingUser,
        ClientModel client
    ) {
        Stream<RoleModel> roles
            = requestingUser.getClientRoleMappingsStream(client);
        Stream<RoleModel> filteredRoles = roles.filter(role -> {
            String roleName = role.getName();
            if (roleName.equals(Role.NUTZER.getRole())
                    || roleName.equals(Role.REDAKTEUR.getRole())
                    || roleName.equals(Role.CHEFREDAKTEUR.getRole())
                    || roleName.equals(Role.TECHADMIN.getRole())) {
                return true;
            }
            return false;
        });
        return filteredRoles.count() > 0;
    }

    /**
     * Checks if the given user has at least the "Readakteur" role.
     * @param requestingUser User to check
     * @param client Client to check in
     * @return True if user has only "Nutzer" role
     */
    public static boolean isUserAtLeastRedakteur(
            UserModel requestingUser,
            ClientModel client) {
        Stream<RoleModel> roles
            = requestingUser.getClientRoleMappingsStream(client);
        Stream<RoleModel> filteredRoles = roles.filter(role -> {
            String roleName = role.getName();
            if (roleName.equals(Role.REDAKTEUR.getRole())
                    || roleName.equals(Role.CHEFREDAKTEUR.getRole())
                    || roleName.equals(Role.TECHADMIN.getRole())) {
                return true;
            }
            return false;
        });
        return filteredRoles.count() > 0;
    }


    /**
     * Checks if the given user has at least the "Chefredakteur" role.
     * @param requestingUser User to check
     * @param client Client to check in
     * @return True if user has only "Nutzer" role
     */
    public static boolean isUserAtLeastChefredakteur(
            UserModel requestingUser,
            ClientModel client) {
        Stream<RoleModel> roles
            = requestingUser.getClientRoleMappingsStream(client);
        Stream<RoleModel> filteredRoles = roles.filter(role -> {
            String roleName = role.getName();
            if (roleName.equals(Role.CHEFREDAKTEUR.getRole())
                    || roleName.equals(Role.TECHADMIN.getRole())) {
                return true;
            }
            return false;
        });
        return filteredRoles.count() > 0;
    }
}
