/* Copyright (C) 2022 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY!
 */

package de.intevation.iam.auth;

import java.util.List;

import org.keycloak.models.KeycloakSession;

import de.intevation.iam.util.RequestMethod;
import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.core.Response;


public abstract class Authorizer<T> {

    protected KeycloakSession session;

    /**
     * Check if user is authorized for the given data and request method.
     * @param data Data
     * @param requestMethod Request method used
     * @return True if authorized, else false
     */
    public boolean isAuthorized(
        T data,
        RequestMethod requestMethod
    ) {
        try {
            doAuthorize(data, requestMethod);
            return true;
        } catch (AuthorizationException ae) {
            return false;
        }
    }

    /**
     * Authorize request for given data.
     * @param data Data
     * @param requestMethod Request method used
     * @throws ForbiddenException if current user is not authorized
     */
    public void authorize(
        T data,
        RequestMethod requestMethod
    ) {
        try {
            doAuthorize(data, requestMethod);
        } catch (AuthorizationException ae) {
            throw new ForbiddenException(
                Response.status(
                    Response.Status.FORBIDDEN.getStatusCode(),
                    ae.getMessage()
                ).build());
        }
    }

    abstract void doAuthorize(
        T data,
        RequestMethod requestMethod
    ) throws AuthorizationException;

    /**
     * Filter or modify the given list of objects.
     * The default implementation returns all elements authorized for GET.
     *
     * @param data List of objects
     * @return Filtered list
     */
    public List<T> filter(List<T> data) {
        return data.stream().filter(object -> isAuthorized(
                object, RequestMethod.GET)).toList();
    }
}
