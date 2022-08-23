/* Copyright (C) 2022 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY!
 */

package de.intevation.iam.auth;

import java.util.List;

import javax.ws.rs.core.HttpHeaders;

import org.keycloak.models.KeycloakSession;

import de.intevation.iam.util.RequestMethod;

public interface Authorizer {

    /**
     * Check if user is authorized for the given data and request method.
     * @param data Data
     * @param requestMethod Request method used
     * @param headers Request headers
     * @param session Keycloak session
     * @return True if authorized, else false
     */
    boolean isAuthorizedById(
        Object data,
        RequestMethod requestMethod,
        HttpHeaders headers,
        KeycloakSession session);

    /**
     * Filter the given list of objects.
     * @param data List of objects
     * @param headers Request headers
     * @param session Keycloak session
     * @return Filtered list
     */
    List<Object> filter(
        List<Object> data,
        HttpHeaders headers,
        KeycloakSession session);
}
