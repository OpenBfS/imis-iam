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
    public boolean isAuthorized(
        Event data,
        RequestMethod requestMethod
    ) {
        UserModel requestingUser = session.getContext().getUserSession().getUser();
        if (requestingUser == null) {
            return false;
        }
        return IaMRole.CHIEF_EDITOR.isRoleOf(requestingUser, session);
    }

    @Override
    public List<Event> filter(
        List<Event> data,
        String userId
    ) {
        return data.stream()
            .map((event) -> {
                event.setReadonly(
                    !isAuthorized(event, null));
                return event;
            })
            .collect(Collectors.toList());
    }
}
