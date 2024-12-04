/* Copyright (C) 2022 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY!
 */

package de.intevation.iam.auth;

import java.util.List;

import org.keycloak.models.ClientModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;

import de.intevation.iam.model.representation.ObjectList;
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
        String userId
    ) {
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
            case GET: return Role.USER.isRoleOf(requestingUser, session);
            case PUT: return authorizeUpdate(
                data, session, requestingUser, client);
            case POST:
                return (!data.isEnabled()
                    && Role.EDITOR.isRoleOf(requestingUser, session)
                    || Role.CHIEF_EDITOR.isRoleOf(requestingUser, session))
                    // Not allowed granting roles superior to own role
                    && requestingUser.hasRole(client.getRole(data.getRole()));
            default: return false;
        }
    }

    @Override
    public List<User> filter(
        List<User> data,
        String userId
    ) {
        RealmModel realm = session.getContext().getRealm();
        UserModel requestingUser = session.users().getUserById(realm, userId);
        ClientModel client = realm.getClientByClientId(Constants.IAM_CLIENT_ID);
        data.forEach(user -> {
            user.setReadonly(
                !authorizeUpdate(
                    user, session, requestingUser, client));
        });
        return data;
    }

    @Override
    public ObjectList<User> filterObjectList(
        ObjectList<User> data,
        String userId
    ) {
        RealmModel realm = session.getContext().getRealm();
        UserModel requestingUser = session.users().getUserById(realm, userId);
        ClientModel client = realm.getClientByClientId(Constants.IAM_CLIENT_ID);
        data.getList().forEach(user -> {
            user.setReadonly(
                !authorizeUpdate(
                    user, session, requestingUser, client));
        });
        return data;
    }

    private boolean authorizeUpdate(
        User user,
        KeycloakSession session,
        UserModel requestingUser,
        ClientModel client
    ) {
        RealmModel realm = session.getContext().getRealm();
        UserModel oldUserModel
            = session.users().getUserById(realm, user.getId());
        if (user.isEnabled() != oldUserModel.isEnabled()
            && !Role.CHIEF_EDITOR.isRoleOf(requestingUser, session)) {
            return false;
        }
        //If role shall be changed:
        //Check if user is chief editor
        String oldRole = oldUserModel.getClientRoleMappingsStream(client)
            .map(role -> role.getName()).findFirst().orElse(null);
        if (oldRole == null || !oldRole.equals(user.getRole())) {
            return Role.CHIEF_EDITOR.isRoleOf(requestingUser, session);
        }
        // Else only allow users with other roles than user to edit
        // or allow users to edit their own profile
        return Role.EDITOR.isRoleOf(requestingUser, session)
            || user.getId().equals(requestingUser.getId());
    }
}
