/* Copyright (C) 2022 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY!
 */
package de.intevation.iam.util;

public class Constants {
    private Constants() { }

    /**
     * Time in months after which an account will be considered inactive.
     */
    public static final int ACCOUNT_INACTIVITY_WARNING_TIME = 6;

    /**
     * Time in months after which an account will expire.
     */
    public static final int ACCOUNT_EXPIRY_TIME = 12;

    public static final String SHIB_USER_HEADER = "X-SHIB-user";
}
