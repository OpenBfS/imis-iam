/* Copyright (C) 2022 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY!
 */
package de.intevation.iam.auth;

import java.util.stream.Stream;

import org.keycloak.models.ClientModel;
import org.keycloak.models.RoleModel;
import org.keycloak.models.UserModel;


/**
 * Utility methods used by the authorizers.
 */
public class Utils {
    private Utils() { };

    /**
     * Checks if a user has any role.
     * @param requestingUser User to check
     * @param client Client to check in
     * @return True if user has any role
     */
    public static boolean hasUserAnyRole(
        UserModel requestingUser,
        ClientModel client
    ) {
        Stream<RoleModel> roles
            = requestingUser.getClientRoleMappingsStream(client);
        Stream<RoleModel> filteredRoles = roles.filter(role -> {
            String roleName = role.getName();
            if (roleName.equals(Role.USER.toString())
                    || roleName.equals(Role.EDITOR.toString())
                    || roleName.equals(Role.CHIEF_EDITOR.toString())
                    || roleName.equals(Role.TECHADMIN.toString())) {
                return true;
            }
            return false;
        });
        return filteredRoles.count() > 0;
    }

    /**
     * Checks if the given user has at least the editor role.
     * @param requestingUser User to check
     * @param client Client to check in
     * @return True if user has at least the editor role
     */
    public static boolean isUserAtLeastEditor(
            UserModel requestingUser,
            ClientModel client) {
        Stream<RoleModel> roles
            = requestingUser.getClientRoleMappingsStream(client);
        Stream<RoleModel> filteredRoles = roles.filter(role -> {
            String roleName = role.getName();
            if (roleName.equals(Role.EDITOR.toString())
                    || roleName.equals(Role.CHIEF_EDITOR.toString())
                    || roleName.equals(Role.TECHADMIN.toString())) {
                return true;
            }
            return false;
        });
        return filteredRoles.count() > 0;
    }


    /**
     * Checks if the given user has at least the chief editor role.
     * @param requestingUser User to check
     * @param client Client to check in
     * @return True if user has at least chief editor role
     */
    public static boolean isUserAtLeastChiefEditor(
            UserModel requestingUser,
            ClientModel client) {
        Stream<RoleModel> roles
            = requestingUser.getClientRoleMappingsStream(client);
        Stream<RoleModel> filteredRoles = roles.filter(role -> {
            String roleName = role.getName();
            if (roleName.equals(Role.CHIEF_EDITOR.toString())
                    || roleName.equals(Role.TECHADMIN.toString())) {
                return true;
            }
            return false;
        });
        return filteredRoles.count() > 0;
    }
}
