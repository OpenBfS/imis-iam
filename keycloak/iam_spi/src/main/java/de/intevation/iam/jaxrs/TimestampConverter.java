/* Copyright (C) 2022 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY!
 */
package de.intevation.iam.jaxrs;

import java.sql.Timestamp;

import javax.ws.rs.ext.ParamConverter;

/**
 * Convert values used with method parameters annotated with
 * javax.ws.rs.*Param to/from java.sql.Timestamp.
 */
public class TimestampConverter implements ParamConverter<Timestamp> {
    @Override
    public Timestamp fromString(String value) {
        return new Timestamp(Long.parseLong(value));
    }

    @Override
    public String toString(Timestamp value) {
        return Long.toString(value.getTime());
    }
}
