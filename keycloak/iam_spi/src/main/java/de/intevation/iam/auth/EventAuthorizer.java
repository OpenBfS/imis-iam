/* Copyright (C) 2024 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY!
 */
package de.intevation.iam.auth;

import java.util.List;
import java.util.stream.Collectors;

import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;

import de.intevation.iam.model.jpa.Event;
import de.intevation.iam.util.Constants;
import de.intevation.iam.util.RequestMethod;
import jakarta.ws.rs.core.HttpHeaders;

/**
 * Authorizer for event entities.
 */
public class EventAuthorizer extends Authorizer<Event> {

    /**
     * Constructor.
     * @param session Keycloak session
     */
    public EventAuthorizer(KeycloakSession session) {
        this.session = session;
    }

    @Override
    public boolean isAuthorizedById(
            Event data,
            RequestMethod requestMethod,
            HttpHeaders headers) {
        String userId = headers.getHeaderString(Constants.SHIB_USER_HEADER);
        RealmModel realm = session.getContext().getRealm();
        UserModel requestingUser = session.users().getUserById(realm, userId);
        if (requestingUser == null) {
            return false;
        }
        return Role.TECHADMIN.isRoleOf(requestingUser, session);
    }

    /**
     * Filter or modify the given list of objects.
     * The default implementation returns data unchanged.
     *
     * @param data List of objects
     * @param headers Request headers
     * @return Filtered list
     */
    public List<Event> filter(
        List<Event> data,
        HttpHeaders headers
    ) {
        return data.stream()
            .map((event) -> {
                event.setReadonly(
                    isAuthorizedById(event, null, headers));
                return event;
            })
            .collect(Collectors.toList());
    }
}
