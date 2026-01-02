/* Copyright (C) 2022 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY!
 */

package de.intevation.iam.auth;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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

    private static final String ENABLED_ATTRIBUTE_KEY = "enabled";
    private static final String HIDDEN_IN_ADDRESSBOOK_KEY = "hiddenInAddressbook";
    private static final String RETIRED_ATTRIBUTE_KEY = "retired";
    private static final String ROLE_ATTRIBUTE_KEY = "role";
    private static final String NETWORK_ATTRIBUTE_KEY = "network";
    private static final String TAGS_ATTRIBUTE_KEY = "tags";

    /**
     * Attributes that require IaMRole.CHIEF_EDITOR to be set.
     */
    private static final List<String> PRIVILEGED_ATTR =
        List.of(
            ENABLED_ATTRIBUTE_KEY,
            HIDDEN_IN_ADDRESSBOOK_KEY,
            RETIRED_ATTRIBUTE_KEY,
            ROLE_ATTRIBUTE_KEY,
            NETWORK_ATTRIBUTE_KEY,
            TAGS_ATTRIBUTE_KEY
        );

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

        switch (requestMethod) {
            case GET:
                checkVisible(data, session, requestingUser);
                break;
            case PUT:
                authorizeUpdate(data, session, requestingUser);
                break;
            case POST:
                authorizeCreate(data, session, requestingUser, client);
                break;
            default:
                throw new AuthorizationException();
        }
    }

    private void checkVisible(
            User user,
            KeycloakSession session,
            UserModel requestingUser
    ) throws AuthorizationException {
        String userNetwork = user.getNetwork();
        String requestingUserNetwork = getRequestingUserNetwork(requestingUser);

        // Retired users only visible to chief editors
        if (user.isRetired() && !IaMRole.CHIEF_EDITOR.isRoleOf(requestingUser, session)) {
            throw new AuthorizationException("Retired user is not visible");
        }

        if (
            (user.isEnabled() && !user.isHiddenInAddressbook())
            || IaMRole.EDITOR.isRoleOf(requestingUser, session)
            && requestingUserNetwork != null
            && requestingUserNetwork.equals(userNetwork)
            || IaMRole.CHIEF_EDITOR.isRoleOf(requestingUser, session)) {
            return;
        }
        throw new AuthorizationException("User is not visible");
    }

    private void checkPrivilegedAttributes(
        User user,
        UserModel requestingUser,
        UserModel oldUserModel,
        KeycloakSession session
    ) throws AuthorizationException {
        List<String> forbidden = new ArrayList<>();

        for (String attr : PRIVILEGED_ATTR) {
            Object newValue, oldValue = null;
            try {
                Method getter = (new PropertyDescriptor(attr, User.class))
                    .getReadMethod();
                newValue = getter.invoke(user);
                if (oldUserModel == null) {
                    // Allow boolean values set to false on user creation
                    if (getter.getReturnType().equals(Boolean.TYPE)
                        && newValue.equals(Boolean.FALSE)) {
                        continue;
                    }
                } else {
                    oldValue = getter.invoke(
                        new User(oldUserModel, session));
                }
            } catch (IntrospectionException ignored) {
                // Assume User Profile attribute if it's not a property
                newValue = user.getAttributes().get(attr);
                if (oldUserModel != null) {
                    oldValue = oldUserModel.getAttributes().get(attr);
                }
            } catch (ReflectiveOperationException e) {
                throw new RuntimeException(e);
            }

            // Checks for user creation
            if (oldUserModel == null) {
                if (attr.equals(ROLE_ATTRIBUTE_KEY)
                    // Allow to create user role
                    && newValue.equals(IaMRole.USER.toString())) {
                    continue;
                }
                if (attr.equals(NETWORK_ATTRIBUTE_KEY)
                    // Allow to create user in same network
                    && newValue.equals(getRequestingUserNetwork(requestingUser))) {
                    continue;
                }
            }
            if (!Objects.equals(newValue, oldValue)) {
                forbidden.add(attr);
            }
        }

        if (!forbidden.isEmpty()) {
            throw new AuthorizationException(
                "Not allowed to set " + forbidden);
        }
    }

    private void authorizeCreate(
        User user,
        KeycloakSession session,
        UserModel requestingUser,
        ClientModel client
    ) throws AuthorizationException {
        if (!IaMRole.CHIEF_EDITOR.isRoleOf(requestingUser, session)) {
            IaMRole.EDITOR.require(requestingUser, session);
            checkPrivilegedAttributes(user, requestingUser, null, session);
        }
    }

    private void authorizeUpdate(
        User user,
        KeycloakSession session,
        UserModel requestingUser
    ) throws AuthorizationException {
        checkVisible(user, session, requestingUser);

        if (!IaMRole.CHIEF_EDITOR.isRoleOf(requestingUser, session)) {
            if (!(IaMRole.EDITOR.isRoleOf(requestingUser, session)
                    && networkEquals(requestingUser, user))
                && !user.getId().equals(requestingUser.getId())
            ) {
                throw new AuthorizationException();
            }
            RealmModel realm = session.getContext().getRealm();
            UserModel oldUserModel
                = session.users().getUserById(realm, user.getId());
            checkPrivilegedAttributes(
                user, requestingUser, oldUserModel, session);
        }

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
