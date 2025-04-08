/* Copyright (C) 2025 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY!
 */
package de.intevation.iam.auth;


import java.io.Serial;

public class AuthorizationException extends Exception {

    @Serial
    private static final long serialVersionUID = 1L;

    AuthorizationException() {
        super();
    }

    AuthorizationException(String message) {
        super(message);
    }
}
