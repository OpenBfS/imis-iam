/* Copyright (C) 2022 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY!
 */
package de.intevation.iam.util;

import java.sql.Timestamp;
import java.time.Duration;
import java.util.Calendar;

public class DateUtils {
    private DateUtils() { }

    /**
     * Get the number of seconds stored in the given env.
     *
     * The expected format is P[n]DT[n]H[n]M[n]S as they are parsed using
     * java.time.Duration.
     * See java.time.Duration.parse(CharSequence text) documentation for
     * further details.
     * @param env Env name
     * @return Seconds as long
     */
    public static long getSecondsFromDurationEnv(String env) {
        Duration duration = getDurationFromEnv(env);
        int seconds = (int) duration.toMillis()/1000;
        return seconds;
    }

    /**
     * Get the number of days stored in the given env.
     *
     * The expected format is P[n]DT[n]H[n]M[n]S as they are parsed using
     * java.time.Duration.
     * See java.time.Duration.parse(CharSequence text) documentation for
     * further details.
     * @param env Env name
     * @return Days as int
     */
    public static int getDaysFromDurationEnv(String env) {
        return (int) getDurationFromEnv(env).toDays();
    }

    private static Duration getDurationFromEnv(String env) {
        String envString = System.getenv(env);
        Duration duration = Duration.parse(envString);
        return duration;
    }

    /**
     * Get account expiry date based of the current time.
     * @return Date as sql timestamp
     */
    public static Timestamp getAccountExpiryDate() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(
            Calendar.DAY_OF_YEAR,
            getDaysFromDurationEnv("IAM_ACCOUNT_EXPIRY_TIME"));
        return new Timestamp(calendar.getTimeInMillis());
    }

    /**
     * Get the account expiry date until which an account
     * is considered inactive.
     * @return Date as sql timestamp
     */
    public static Timestamp getAccountInactivityDate() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(
            Calendar.DAY_OF_YEAR,
            getDaysFromDurationEnv("IAM_ACCOUNT_INACITIVITY_WARNING_TIME"));
        return new Timestamp(calendar.getTimeInMillis());
    }
}
