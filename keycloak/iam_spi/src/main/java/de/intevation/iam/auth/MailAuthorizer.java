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
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;

import de.intevation.iam.model.jpa.Mail;
import de.intevation.iam.util.Constants;
import de.intevation.iam.util.RequestMethod;

public class MailAuthorizer extends Authorizer<Mail> {

    public MailAuthorizer(KeycloakSession session) {
        this.session = session;
    }

    @Override
    public boolean isAuthorizedById(
        Mail data,
        RequestMethod requestMethod,
        HttpHeaders headers
    ) {
        String userId = headers.getHeaderString(Constants.SHIB_USER_HEADER);
        if (userId == null) {
            return false;
        }
        RealmModel realm = session.getContext().getRealm();
        UserModel requestingUser = session.users().getUserById(realm, userId);

        switch (requestMethod) {
            case GET: return Role.USER.isRoleOf(requestingUser, session);
            case POST: return Role.EDITOR.isRoleOf(requestingUser, session);
            default: return false;
        }
    }

    @Override
    public List<Mail> filter(
        List<Mail> data,
        HttpHeaders headers
    ) {
        return data;
    }
}
