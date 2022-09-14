/* Copyright (C) 2022 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY!
 */
package de.intevation.iam.auth;

import java.util.List;

import javax.ws.rs.core.HttpHeaders;

import org.keycloak.models.ClientModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;

import de.intevation.iam.model.Mail;
import de.intevation.iam.util.AuthUtils;
import de.intevation.iam.util.Constants;
import de.intevation.iam.util.RequestMethod;

public class MailAuthorizer implements Authorizer<Mail> {

    @Override
    public boolean isAuthorizedById(
            Object data,
            RequestMethod requestMethod,
            HttpHeaders headers,
            KeycloakSession session) {
        String userId = headers.getHeaderString(Constants.SHIB_USER_HEADER);
        if (userId == null) {
            return false;
        }
        switch (requestMethod) {
            case GET: return authorizeGet(session, userId);
            case POST: return authorizeSendMail((Mail) data, session, userId);
            default: return false;
        }
    }

    private boolean authorizeGet(
        KeycloakSession session,
        String userId
    ) {
        RealmModel realm = session.getContext().getRealm();
        ClientModel client = realm.getClientByClientId(Constants.IAM_CLIENT_ID);
        UserModel requestingUser = session.users().getUserById(realm, userId);
        return AuthUtils.hasUserAnyRole(requestingUser, client);
    }

    private boolean authorizeSendMail(
            Mail mail,
            KeycloakSession session,
            String userId) {
        //Only allow users that are at least editors to send mails
        RealmModel realm = session.getContext().getRealm();
        ClientModel client = realm.getClientByClientId(Constants.IAM_CLIENT_ID);
        UserModel requestingUser = session.users().getUserById(realm, userId);
        if (requestingUser == null) {
            return false;
        }
        return AuthUtils.isUserAtLeastEditor(requestingUser, client);
    }

    @Override
    public List<Mail> filter(
            List<Mail> data,
            HttpHeaders headers,
            KeycloakSession session) {
        return data;
    }
}
