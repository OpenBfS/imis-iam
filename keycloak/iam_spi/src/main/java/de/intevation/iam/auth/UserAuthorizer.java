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

import de.intevation.iam.model.representation.User;
import de.intevation.iam.util.Constants;
import de.intevation.iam.util.RequestMethod;

public class UserAuthorizer extends Authorizer<User> {

    public UserAuthorizer(KeycloakSession session) {
        this.session = session;
    }

    @Override
    public boolean isAuthorizedById(
        User data,
        RequestMethod requestMethod,
        HttpHeaders headers
    ) {
        String userId = headers.getHeaderString(Constants.SHIB_USER_HEADER);
        if (userId == null) {
            return false;
        }

        RealmModel realm = session.getContext().getRealm();
        ClientModel client = realm.getClientByClientId(Constants.IAM_CLIENT_ID);
        UserModel requestingUser = session.users().getUserById(realm, userId);
        if (requestingUser == null) {
            return false;
        }

        switch (requestMethod) {
            case GET: return authorizeGet(requestingUser, client);
            case PUT: return authorizeUpdate(
                data, session, requestingUser, client);
            case POST: return authorizeCreate(
                data, requestingUser, client);
            default: return false;
        }
    }

    @Override
    public List<User> filter(
            List<User> data,
            HttpHeaders headers
    ) {
        RealmModel realm = session.getContext().getRealm();
        String userId = headers.getHeaderString(Constants.SHIB_USER_HEADER);
        UserModel requestingUser = session.users().getUserById(realm, userId);
        ClientModel client = realm.getClientByClientId(Constants.IAM_CLIENT_ID);
        data.forEach(user -> {
            user.setReadonly(
                !authorizeUpdate(
                    user, session, requestingUser, client));
        });
        return data;
    }

    private boolean authorizeGet(
        UserModel requestingUser,
        ClientModel client
    ) {
        return Utils.hasUserAnyRole(requestingUser, client);
    }

    private boolean authorizeCreate(
        User user,
        UserModel requestingUser,
        ClientModel client
    ) {
        //Only allow users that are at least editor create
        //If no roles are set, check if user is editor
        //Else check if user is chief editor
        if (user.getRoles() == null || user.getRoles().isEmpty()) {
            return Utils.isUserAtLeastEditor(requestingUser, client);
        } else {
            return Utils.isUserAtLeastChiefEditor(
                    requestingUser, client);
        }
    }

    private boolean authorizeUpdate(
        User user,
        KeycloakSession session,
        UserModel requestingUser,
        ClientModel client
    ) {
        RealmModel realm = session.getContext().getRealm();
        //If roles shall be changed:
        //Check if user is chief editor or admin
        UserModel oldUserModel
            = session.users().getUserById(realm, user.getId());
        List<String> oldRoles = new ArrayList<String>();
        oldUserModel.getClientRoleMappingsStream(client)
                .forEach(role -> oldRoles.add(role.getName()));
        List<String> newRoles = user.getRoles() != null
            ? user.getRoles()
            : new ArrayList<>();
        if (oldRoles.size() != newRoles.size()) {
            return Utils.isUserAtLeastChiefEditor(
                    requestingUser, client);
        }
        for (int i = 0; i < newRoles.size(); i++) {
            if (!oldRoles.contains(newRoles.get(i))
                || !newRoles.contains(oldRoles.get(i))) {
                return Utils.isUserAtLeastChiefEditor(
                        requestingUser, client);
            }
        }
        // Else only allow users with other roles than user to edit
        // or allow users to edit their own profile
        return Utils.isUserAtLeastEditor(requestingUser, client)
            || user.getId().equals(requestingUser.getId());
    }
}
