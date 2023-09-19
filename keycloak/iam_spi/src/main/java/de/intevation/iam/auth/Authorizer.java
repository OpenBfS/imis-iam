/* Copyright (C) 2022 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY!
 */

package de.intevation.iam.auth;

import java.util.List;

import jakarta.ws.rs.core.HttpHeaders;

import org.keycloak.models.KeycloakSession;

import de.intevation.iam.util.RequestMethod;

public abstract class Authorizer<T> {

    protected KeycloakSession session;

    /**
     * Check if user is authorized for the given data and request method.
     * @param data Data
     * @param requestMethod Request method used
     * @param headers Request headers
     * @return True if authorized, else false
     */
    public abstract boolean isAuthorizedById(
        T data,
        RequestMethod requestMethod,
        HttpHeaders headers
    );

    /**
     * Filter or modify the given list of objects.
     * The default implementation returns data unchanged.
     *
     * @param data List of objects
     * @param headers Request headers
     * @return Filtered list
     */
    public List<T> filter(
        List<T> data,
        HttpHeaders headers
    ) {
        return data;
    }
}
