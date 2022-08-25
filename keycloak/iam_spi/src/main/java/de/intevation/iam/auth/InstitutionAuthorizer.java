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

import de.intevation.iam.model.Institution;
import de.intevation.iam.util.AuthUtils;
import de.intevation.iam.util.Constants;
import de.intevation.iam.util.RequestMethod;

public class InstitutionAuthorizer implements Authorizer {

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
            case GET: return true;
            case PUT:
                return authorizeUpdate((Institution) data, session, userId);
            case POST:
                return authorizeCreate((Institution) data, session, userId);
            case DELETE:
                return authorizeDelete((Institution) data, session, userId);
            default: return false;
                }
    }

    @Override
    public List<Object> filter(
            List<Object> data,
            HttpHeaders headers,
            KeycloakSession session) {
        return data;
    }

    private boolean authorizeCreate(
        Institution institution,
        KeycloakSession session,
        String userId
    ) {
        //Only allow users with other roles than "Nutzer" to create
        RealmModel realm = session.getContext().getRealm();
        ClientModel client = realm.getClientByClientId(Constants.IAM_CLIENT_ID);
        UserModel requestingUser = session.users().getUserById(realm, userId);
        if (requestingUser == null) {
            return false;
        }
        return AuthUtils.isUserAtLeastRedakteur(requestingUser, client);
    }

    private boolean authorizeUpdate(
        Institution institution,
        KeycloakSession session,
        String userId
    ) {
        //Only allow users with other roles than "Nutzer" to edit
        RealmModel realm = session.getContext().getRealm();
        ClientModel client = realm.getClientByClientId(Constants.IAM_CLIENT_ID);
        UserModel requestingUser = session.users().getUserById(realm, userId);
        if (requestingUser == null) {
            return false;
        }
        return AuthUtils.isUserAtLeastRedakteur(requestingUser, client);
    }

    private boolean authorizeDelete(
        Institution institution,
        KeycloakSession session,
        String userId
    ) {
        //Only allow users with other roles than "Nutzer" to edit
        RealmModel realm = session.getContext().getRealm();
        ClientModel client = realm.getClientByClientId(Constants.IAM_CLIENT_ID);
        UserModel requestingUser = session.users().getUserById(realm, userId);
        if (requestingUser == null) {
            return false;
        }
        return AuthUtils.isUserAtLeastRedakteur(requestingUser, client);
    }

}
