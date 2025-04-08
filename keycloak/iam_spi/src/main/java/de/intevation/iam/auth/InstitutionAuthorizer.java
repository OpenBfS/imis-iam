/* Copyright (C) 2022 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY!
 */
package de.intevation.iam.auth;

import org.keycloak.connections.jpa.JpaConnectionProvider;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.UserModel;

import de.intevation.iam.model.jpa.Institution;
import de.intevation.iam.model.jpa.Institution_;
import de.intevation.iam.util.RequestMethod;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class InstitutionAuthorizer extends Authorizer<Institution> {

    /**
     * Attributes that require IaMRole.CHIEF_EDITOR to be set.
     */
    private static final Map<String, Method> PRIVILEGED_ATTR_GETTERS;
    static {
        try {
            PRIVILEGED_ATTR_GETTERS = Map.of(
                Institution_.ACTIVE,
                new PropertyDescriptor(
                    Institution_.ACTIVE,
                    Institution.class).getReadMethod(),
                Institution_.MEAS_FACIL_ID,
                new PropertyDescriptor(
                    Institution_.MEAS_FACIL_ID,
                    Institution.class).getReadMethod(),
                Institution_.TAGS,
                new PropertyDescriptor(
                    Institution_.TAGS,
                    Institution.class).getReadMethod());
        } catch (IntrospectionException e) {
            throw new RuntimeException(e);
        }
    }

    public InstitutionAuthorizer(KeycloakSession session) {
        this.session = session;
    }

    @Override
    public void doAuthorize(
        Institution data,
        RequestMethod requestMethod
    ) throws AuthorizationException {
        UserModel requestingUser = session.getContext().getUserSession().getUser();

        switch (requestMethod) {
            case GET:
                IaMRole.USER.require(requestingUser, session);
                break;
            case PUT:
                if (!IaMRole.CHIEF_EDITOR.isRoleOf(requestingUser, session)) {
                    IaMRole.EDITOR.require(requestingUser, session);
                    anyPrivilegedAttrChanged(data);
                    networkEquals(requestingUser, data);
                    break;
                }
            case POST:
                if (!IaMRole.CHIEF_EDITOR.isRoleOf(requestingUser, session)) {
                    IaMRole.EDITOR.require(requestingUser, session);
                    anyPrivilegedAttrSet(data);
                    break;
                }
            default:
                IaMRole.CHIEF_EDITOR.require(requestingUser, session);
        }
    }

    private void networkEquals(
        UserModel requestingUser, Institution data
    ) throws AuthorizationException {
        String userNetwork = requestingUser.getFirstAttribute("network");
        String measFacilId = data.getMeasFacilId();
        if (userNetwork == null
            || measFacilId == null
            || !userNetwork.equals(measFacilId.substring(0, 2))
        ) {
            throw new AuthorizationException("Not authorized for network");
        }
    }

    private void anyPrivilegedAttrChanged(
        Institution data
    ) throws AuthorizationException {
        Institution persistent = session
            .getProvider(JpaConnectionProvider.class)
            .getEntityManager()
            .find(Institution.class, data.getId());
        List<String> forbidden = new ArrayList<>();
        try {
            for (String attr: PRIVILEGED_ATTR_GETTERS.keySet()) {
                Method getter = PRIVILEGED_ATTR_GETTERS.get(attr);
                if (!Objects.equals(
                        getter.invoke(data), getter.invoke(persistent))
                ) {
                    forbidden.add(attr);
                }
            }
            if (!forbidden.isEmpty()) {
                throw new AuthorizationException(
                    "Not allowed to change " + forbidden);
            }
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    private void anyPrivilegedAttrSet(
        Institution data
    ) throws AuthorizationException {
        List<String> forbidden = new ArrayList<>();
        try {
            for (String attr: PRIVILEGED_ATTR_GETTERS.keySet()) {
                Method getter = PRIVILEGED_ATTR_GETTERS.get(attr);
                Object val = getter.invoke(data);
                if (getter.getReturnType().equals(Boolean.TYPE)) {
                    if (val.equals(Boolean.TRUE)) {
                        forbidden.add(attr);
                    }
                } else if (val instanceof Collection<?> lst) {
                    if (!lst.isEmpty()) {
                        forbidden.add(attr);
                    }
                } else if (val != null) {
                    forbidden.add(attr);
                }
            }
            if (!forbidden.isEmpty()) {
                throw new AuthorizationException(
                    "Not allowed to set " + forbidden);
            }
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }
}
