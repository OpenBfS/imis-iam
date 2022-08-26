/* Copyright (C) 2022 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY!
 */

package de.intevation.iam.auth;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.HttpHeaders;

import org.keycloak.models.ClientModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;

import de.intevation.iam.model.User;
import de.intevation.iam.util.AuthUtils;
import de.intevation.iam.util.Constants;
import de.intevation.iam.util.RequestMethod;

public class UserAuthorizer implements Authorizer<User> {

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
            case PUT: return authorizeUpdate((User) data, session, userId);
            case POST: return authorizeCreate((User) data, session, userId);
            default: return false;
        }
    }

    @Override
    public List<User> filter(
            List<User> data,
            HttpHeaders headers,
            KeycloakSession session) {
        String userId = headers.getHeaderString(Constants.SHIB_USER_HEADER);
        data.forEach(user -> {
            user.setReadonly(
                !authorizeUpdate(user, session, userId));
        });
        return data;
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
        User user,
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
        User user,
        KeycloakSession session,
        String userId
    ) {
        //Check if user is updating its own profile
        if (user.getId().equals(userId)) {
            return true;
        }
        RealmModel realm = session.getContext().getRealm();
        ClientModel client = realm.getClientByClientId(Constants.IAM_CLIENT_ID);
        UserModel requestingUser = session.users().getUserById(realm, userId);
        if (requestingUser == null) {
            return false;
        }
        //If roles shall be changed:
        //Check if user is "Chefredakteur" or "tech. Admin"
        UserModel oldUserModel
            = session.users().getUserById(realm, user.getId());
        List<String> oldRoles = new ArrayList<String>();
        oldUserModel.getClientRoleMappingsStream(client)
                .forEach(role -> oldRoles.add(role.getName()));
        List<String> newRoles = user.getRoles();
        if (oldRoles.size() != newRoles.size()) {
            return false;
        }
        for (int i = 0; i < newRoles.size(); i++) {
            if (!oldRoles.contains(newRoles.get(i))
                || !newRoles.contains(oldRoles.get(i))) {
                return AuthUtils.isUserAtLeastChefredakteur(
                        requestingUser, client);
            }
        }
        // Else only allow users with other roles than "Nutzer" to edit
        return AuthUtils.isUserAtLeastRedakteur(requestingUser, client);
    }
}
