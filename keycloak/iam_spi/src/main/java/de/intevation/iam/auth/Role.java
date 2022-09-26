/* Copyright (C) 2022 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY!
 */
package de.intevation.iam.auth;

import java.util.Arrays;
import java.util.NoSuchElementException;

public enum Role {
    USER("Nutzer"),
    EDITOR("Redakteur"),
    CHIEF_EDITOR("Chefredakteur"),
    TECHADMIN("technischer Administrator");

    private String role;

    Role(String role) {
        this.role = role;
    }

    public String getRole() {
        return role;
    }

    /**
     * Get enum by role string.
     * @param role Role string
     * @return Enum or null if not found
     */
    public static Role get(String role) {
        try {
            return Arrays.stream(Role.values())
            .filter(env -> env.role.equals(role))
            .findFirst().get();
        } catch (NoSuchElementException e) {
            return null;
        }

    }
}
