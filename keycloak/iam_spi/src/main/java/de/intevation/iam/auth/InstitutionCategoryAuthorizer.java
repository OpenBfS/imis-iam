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

import de.intevation.iam.model.InstitutionCategory;
import de.intevation.iam.util.AuthUtils;
import de.intevation.iam.util.Constants;
import de.intevation.iam.util.RequestMethod;

public class InstitutionCategoryAuthorizer
    implements Authorizer<InstitutionCategory> {

    @Override
    public boolean isAuthorizedById(
            Object data,
            RequestMethod requestMethod,
            HttpHeaders headers,
            KeycloakSession session) {
        String userId = headers.getHeaderString(Constants.SHIB_USER_HEADER);
        if (userId == null || userId.isEmpty()) {
            return false;
        }
        switch (requestMethod) {
            case GET: return authorizeGet(session, userId);
            case POST:
                return authorizeCreate(
                    (InstitutionCategory) data, session, userId);
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
        return AuthUtils.isUserAtLeastNutzer(requestingUser, client);
    }

    private boolean authorizeCreate(
        InstitutionCategory category,
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

    @Override
    public List<InstitutionCategory> filter(
            List<InstitutionCategory> data,
            HttpHeaders headers,
            KeycloakSession session) {
        return data;
    }

}
