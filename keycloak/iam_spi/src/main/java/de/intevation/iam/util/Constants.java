/* Copyright (C) 2022 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY!
 */


package de.intevation.iam.util;

public class Constants {
    private Constants() { }
    public static final String IAM_CLIENT_ID = "iam-client";

    /**
     * Time in months after which an account will be considered inactive.
     */
    public static final int ACCOUNT_INACTIVITY_WARNING_TIME = 6;

    /**
     * Time in months after which an account will expire.
     */
    public static final int ACCOUNT_EXPIRY_TIME = 12;

    /**
     * Header containing userid.
     */
    public static final String SHIB_USER_HEADER = "X-SHIB-user";

    /**
     * Email address to send notifications to.
     */
    public static final String NOTIFICATION_RECIPIENT = "notifications@recipient.example";

    public static final String NOTIFICATION_USERNAME = "imis3Notifications";
}
