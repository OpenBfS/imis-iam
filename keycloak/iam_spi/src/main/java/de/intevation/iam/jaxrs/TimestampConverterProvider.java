/* Copyright (C) 2022 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY!
 */
package de.intevation.iam.jaxrs;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.sql.Timestamp;

import jakarta.ws.rs.ext.ParamConverter;
import jakarta.ws.rs.ext.ParamConverterProvider;
import jakarta.ws.rs.ext.Provider;

/**
 * Provide custom converter for method parameters annotated with
 * jakarta.ws.rs.*Param.
 */
@Provider
public class TimestampConverterProvider implements ParamConverterProvider {
    @Override
    public <T> ParamConverter<T> getConverter(
        Class<T> rawType, Type genericType, Annotation[] annotations
    ) {
        if (rawType.equals(Timestamp.class)) {
            @SuppressWarnings("unchecked")
            ParamConverter<T> timestampConverter =
                (ParamConverter<T>) new TimestampConverter();
            return timestampConverter;
        }
        return null;
    }
}
