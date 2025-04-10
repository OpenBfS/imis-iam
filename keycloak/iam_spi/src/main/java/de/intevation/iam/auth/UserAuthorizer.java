/* Copyright (C) 2022 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY!
 */

package de.intevation.iam.auth;

import org.keycloak.connections.jpa.JpaConnectionProvider;
import org.keycloak.models.ClientModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;

import de.intevation.iam.model.jpa.UserAttributes;
import de.intevation.iam.model.representation.User;
import de.intevation.iam.util.Constants;
import de.intevation.iam.util.RequestMethod;

public class UserAuthorizer extends Authorizer<User> {

    private static final String NETWORK_ATTRIBUTE_KEY = "network";

    public UserAuthorizer(KeycloakSession session) {
        this.session = session;
    }

    @Override
    void doAuthorize(
        User data,
        RequestMethod requestMethod
    ) throws AuthorizationException {
        RealmModel realm = session.getContext().getRealm();
        ClientModel client = realm.getClientByClientId(Constants.IAM_CLIENT_ID);
        UserModel requestingUser = session.getContext().getUserSession().getUser();
        if (requestingUser == null) {
            throw new AuthorizationException();
        }

        boolean allowed = switch (requestMethod) {
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
        if (!allowed) {
            throw new AuthorizationException();
        }
    }

    private boolean isVisible(
            User user,
            KeycloakSession session,
            UserModel requestingUser
    ) {
        String userNetwork = user.getNetwork();
        String requestingUserNetwork = getRequestingUserNetwork(requestingUser);
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
        User oldUser = new User(oldUserModel, session);

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

        // If network shall be changed: Check if user is chief editor
        if (!user.getNetwork().equals(oldUser.getNetwork())
            && !IaMRole.CHIEF_EDITOR.isRoleOf(requestingUser, session)) {
            return false;
        }

        return IaMRole.EDITOR.isRoleOf(requestingUser, session)
            && networkEquals(requestingUser, user)
            || IaMRole.CHIEF_EDITOR.isRoleOf(requestingUser, session)
            || user.getId().equals(requestingUser.getId());
    }

    private boolean networkEquals(UserModel requestingUser, User data) {
        String dataNetwork = data.getNetwork();
        return getRequestingUserNetwork(requestingUser).equals(dataNetwork);
    }

    private String getRequestingUserNetwork(UserModel requestingUser) {
        UserAttributes userAttributes = session
            .getProvider(JpaConnectionProvider.class)
            .getEntityManager()
            .find(UserAttributes.class, requestingUser.getId());
        return userAttributes.getNetwork();
    }
}
