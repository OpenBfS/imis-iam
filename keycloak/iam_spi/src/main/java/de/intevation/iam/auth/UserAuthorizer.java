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

import de.intevation.iam.model.representation.User;
import de.intevation.iam.util.Constants;
import de.intevation.iam.util.RequestMethod;

public class UserAuthorizer extends Authorizer<User> {

    private static final String NETWORK_ATTRIBUTE_KEY = "network";

    public UserAuthorizer(KeycloakSession session) {
        this.session = session;
    }

    @Override
    public boolean isAuthorized(
        User data,
        RequestMethod requestMethod
    ) {
        RealmModel realm = session.getContext().getRealm();
        ClientModel client = realm.getClientByClientId(Constants.IAM_CLIENT_ID);
        UserModel requestingUser = session.getContext().getUserSession().getUser();
        if (requestingUser == null) {
            return false;
        }

        return switch (requestMethod) {
            case GET -> isVisible(data, session, requestingUser);
            case PUT -> authorizeUpdate(
                    data, session, requestingUser, client);
            case POST -> (!data.isEnabled()
                    && networkEquals(requestingUser, data)
                    && IaMRole.EDITOR.isRoleOf(requestingUser, session)
                    || IaMRole.CHIEF_EDITOR.isRoleOf(requestingUser, session))
                    // Not allowed granting roles superior to own role
                    && requestingUser.hasRole(client.getRole(data.getRole()));
            default -> false;
        };
    }

    private boolean isVisible(
            User user,
            KeycloakSession session,
            UserModel requestingUser
    ) {
        String userNetwork =
            user.getAttributes().get(NETWORK_ATTRIBUTE_KEY) != null
            ? user.getAttributes().get(NETWORK_ATTRIBUTE_KEY).get(0)
            : null;
        String requestingUserNetwork =
            requestingUser.getFirstAttribute(NETWORK_ATTRIBUTE_KEY);
        return user.isEnabled()
            || IaMRole.EDITOR.isRoleOf(requestingUser, session)
            && requestingUserNetwork != null
            && requestingUserNetwork.equals(userNetwork)
            || IaMRole.CHIEF_EDITOR.isRoleOf(requestingUser, session);
    }

    private boolean authorizeUpdate(
        User user,
        KeycloakSession session,
        UserModel requestingUser,
        ClientModel client
    ) {
        if (!isVisible(user, session, requestingUser)) {
            return false;
        }
        RealmModel realm = session.getContext().getRealm();
        UserModel oldUserModel
            = session.users().getUserById(realm, user.getId());
        if (user.isEnabled() != oldUserModel.isEnabled()
            && !IaMRole.CHIEF_EDITOR.isRoleOf(requestingUser, session)) {
            return false;
        }
        //If role shall be changed:
        //Check if user is chief editor
        String oldRole = oldUserModel.getClientRoleMappingsStream(client)
            .map(role -> role.getName()).findFirst().orElse(null);
        if (oldRole == null || !oldRole.equals(user.getRole())) {
            return IaMRole.CHIEF_EDITOR.isRoleOf(requestingUser, session);
        }

        return IaMRole.EDITOR.isRoleOf(requestingUser, session)
            && networkEquals(requestingUser, user)
            || IaMRole.CHIEF_EDITOR.isRoleOf(requestingUser, session)
            || user.getId().equals(requestingUser.getId());
    }

    private boolean networkEquals(UserModel requestingUser, User data) {
        final String networkKey = "network";
        String requestingUserNetwork =
            requestingUser.getFirstAttribute(networkKey);
        List<String> dataNetwork = data.getAttributes().get(networkKey);
        return requestingUserNetwork != null
            && dataNetwork != null
            && !dataNetwork.isEmpty()
            && requestingUserNetwork.equals(dataNetwork.get(0));
    }
}
