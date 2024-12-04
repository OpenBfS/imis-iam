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
import de.intevation.iam.util.RequestMethod;


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
        String userId
    ) {
        RealmModel realm = session.getContext().getRealm();
        UserModel requestingUser = session.users().getUserById(realm, userId);
        if (requestingUser == null) {
            return false;
        }
        return Role.CHIEF_EDITOR.isRoleOf(requestingUser, session);
    }

    @Override
    public List<Event> filter(
        List<Event> data,
        String userId
    ) {
        return data.stream()
            .map((event) -> {
                event.setReadonly(
                    !isAuthorizedById(event, null, userId));
                return event;
            })
            .collect(Collectors.toList());
    }
}
