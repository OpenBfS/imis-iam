/* Copyright (C) 2022 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY!
 */
package de.intevation.iam.auth;

/**
 * The user roles known to this application.
 */
public enum Role {
    USER,
    EDITOR,
    CHIEF_EDITOR,
    TECHADMIN;

    @Override
    public String toString() {
        return this.name().toLowerCase();
    }
}
