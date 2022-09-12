/* Copyright (C) 2022 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY!
 */
package de.intevation.iam.util;

import java.sql.Timestamp;
import java.util.Calendar;

public class DateUtils {
    private DateUtils() { }

    /**
     * Get account expiry date based of the current time.
     * @return Date as sql timestamp
     */
    public static Timestamp getAccountExpiryDate() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MONTH, Constants.ACCOUNT_EXPIRY_TIME);
        return new Timestamp(calendar.getTimeInMillis());
    }

    /**
     * Get the account expiry date until which an account
     * is considered inactive.
     * @return Date as sql timestamp
     */
    public static Timestamp getAccountInactivityDate() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MONTH,
                Constants.ACCOUNT_INACTIVITY_WARNING_TIME);
        return new Timestamp(calendar.getTimeInMillis());
    }
}
