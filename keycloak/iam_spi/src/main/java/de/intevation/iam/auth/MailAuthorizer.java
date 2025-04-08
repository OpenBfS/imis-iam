/* Copyright (C) 2022 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY!
 */
package de.intevation.iam.auth;

import org.keycloak.models.KeycloakSession;
import org.keycloak.models.UserModel;

import de.intevation.iam.model.jpa.Mail;
import de.intevation.iam.util.RequestMethod;

public class MailAuthorizer extends Authorizer<Mail> {

    public MailAuthorizer(KeycloakSession session) {
        this.session = session;
    }

    @Override
    void doAuthorize(
        Mail data,
        RequestMethod requestMethod
    ) throws AuthorizationException {
        UserModel requestingUser = session.getContext().getUserSession().getUser();

        boolean allowed = switch (requestMethod) {
            case GET -> IaMRole.USER.isRoleOf(requestingUser, session);
            case POST -> IaMRole.EDITOR.isRoleOf(requestingUser, session);
            default -> false;
        };
        if (!allowed) {
            throw new AuthorizationException();
        }
    }
}
