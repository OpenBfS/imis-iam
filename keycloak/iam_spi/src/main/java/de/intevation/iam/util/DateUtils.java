/* Copyright (C) 2022 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY!
 */
package de.intevation.iam.util;

import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.time.DateTimeException;


public class DateUtils {

    private static final Duration EXPIRY_TERM = getDurationFromEnv(
        "IAM_ACCOUNT_EXPIRY_TIME");

    private DateUtils() { }

    /**
     * Obtains a Duration from environment variable with given name.
     *
     * @param env Name of environment variable.
     * @return The parsed duration.
     * @throws DateTimeException if the value of the environment variable
     * cannot be parsed to a duration.
     */
    public static Duration getDurationFromEnv(String env) {
        String envString = System.getenv(env);
        try {
            return Duration.parse(envString);
        } catch (DateTimeException dtpe) {
            throw new DateTimeException(String.format(
                    "Value '%s' of environment variable '%s' "
                    + "cannot be parsed to a Duration",
                    envString, env));
        }
    }

    /**
     * Get account expiry date based on EXPIRY_TERM.
     *
     * @return Date as sql timestamp
     */
    public static Timestamp getAccountExpiryDate() {
        return Timestamp.from(Instant.now().plus(EXPIRY_TERM));
    }
}
