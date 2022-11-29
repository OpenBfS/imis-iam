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

import de.intevation.iam.model.jpa.Institution;
import de.intevation.iam.util.Constants;
import de.intevation.iam.util.RequestMethod;

public class InstitutionAuthorizer extends Authorizer<Institution> {

    public InstitutionAuthorizer(KeycloakSession session) {
        this.session = session;
    }

    @Override
    public boolean isAuthorizedById(
        Institution data,
        RequestMethod requestMethod,
        HttpHeaders headers
    ) {
        String userId = headers.getHeaderString(Constants.SHIB_USER_HEADER);
        if (userId == null) {
            return false;
        }
        switch (requestMethod) {
            case GET: return true;
            case PUT:
                return authorizeUpdate(data, session, userId);
            case POST:
                return authorizeCreate(data, session, userId);
            case DELETE:
                return authorizeDelete(data, session, userId);
            default: return false;
                }
    }

    @Override
    public List<Institution> filter(
        List<Institution> data,
        HttpHeaders headers
    ) {
        String userId = headers.getHeaderString(Constants.SHIB_USER_HEADER);
        data.forEach(institution -> {
            institution.setReadonly(
                !authorizeUpdate(institution, session, userId));
        });
        return data;
    }

    private boolean authorizeCreate(
        Institution institution,
        KeycloakSession session,
        String userId
    ) {
        //Only allow users that are at least editors to create
        RealmModel realm = session.getContext().getRealm();
        ClientModel client = realm.getClientByClientId(Constants.IAM_CLIENT_ID);
        UserModel requestingUser = session.users().getUserById(realm, userId);
        if (requestingUser == null) {
            return false;
        }
        return Utils.isUserAtLeastEditor(requestingUser, client);
    }

    private boolean authorizeUpdate(
        Institution institution,
        KeycloakSession session,
        String userId
    ) {
        //Only allow users that are at least editors to edit
        RealmModel realm = session.getContext().getRealm();
        ClientModel client = realm.getClientByClientId(Constants.IAM_CLIENT_ID);
        UserModel requestingUser = session.users().getUserById(realm, userId);
        if (requestingUser == null) {
            return false;
        }
        return Utils.isUserAtLeastEditor(requestingUser, client);
    }

    private boolean authorizeDelete(
        Institution institution,
        KeycloakSession session,
        String userId
    ) {
        //Only allow users that are at least editors to delete
        RealmModel realm = session.getContext().getRealm();
        ClientModel client = realm.getClientByClientId(Constants.IAM_CLIENT_ID);
        UserModel requestingUser = session.users().getUserById(realm, userId);
        if (requestingUser == null) {
            return false;
        }
        return Utils.isUserAtLeastEditor(requestingUser, client);
    }

}
