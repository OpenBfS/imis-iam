/* Copyright (C) 2022 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY!
 */
package de.intevation.iam.auth;

import jakarta.persistence.EntityManager;

import org.keycloak.connections.jpa.JpaConnectionProvider;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.UserModel;

import de.intevation.iam.model.jpa.Institution;
import de.intevation.iam.util.RequestMethod;

import java.util.Objects;

public class InstitutionAuthorizer extends Authorizer<Institution> {

    public InstitutionAuthorizer(KeycloakSession session) {
        this.session = session;
    }

    @Override
    public void doAuthorize(
        Institution data,
        RequestMethod requestMethod
    ) throws AuthorizationException {
        UserModel requestingUser = session.getContext().getUserSession().getUser();

        EntityManager em = session.getProvider(
            JpaConnectionProvider.class).getEntityManager();
        boolean allowed = switch (requestMethod) {
            case GET ->
                IaMRole.USER.isRoleOf(requestingUser, session);
            // Only Role.CHIEF_EDITOR is allowed to set/edit measFacilId
            case PUT -> (
                IaMRole.CHIEF_EDITOR.isRoleOf(requestingUser, session)
                || Objects.equals(data.getMeasFacilId(), em.find(
                        Institution.class, data.getId()).getMeasFacilId())
                && networkEquals(requestingUser, data)
                && IaMRole.EDITOR.isRoleOf(requestingUser, session));
            case POST -> (
                data.getMeasFacilId() == null
                && IaMRole.EDITOR.isRoleOf(requestingUser, session)
                || IaMRole.CHIEF_EDITOR.isRoleOf(requestingUser, session));
            case DELETE ->
                IaMRole.CHIEF_EDITOR.isRoleOf(requestingUser, session);
            default -> false;
        };
        if (!allowed) {
            throw new AuthorizationException();
        }
    }

    private boolean networkEquals(
        UserModel requestingUser, Institution data
    ) {
        String userNetwork = requestingUser.getFirstAttribute("network");
        String measFacilId = data.getMeasFacilId();
        return userNetwork != null
            && measFacilId != null
            && userNetwork.equals(measFacilId.substring(0, 2));
    }
}
