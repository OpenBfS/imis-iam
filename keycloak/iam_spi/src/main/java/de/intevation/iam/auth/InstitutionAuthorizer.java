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
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;

import de.intevation.iam.model.jpa.Institution;
import de.intevation.iam.util.RequestMethod;

public class InstitutionAuthorizer extends Authorizer<Institution> {

    public InstitutionAuthorizer(KeycloakSession session) {
        this.session = session;
    }

    @Override
    public boolean isAuthorizedById(
        Institution data,
        RequestMethod requestMethod,
        String userId
    ) {
        if (userId == null) {
            return false;
        }
        RealmModel realm = session.getContext().getRealm();
        UserModel requestingUser = session.users().getUserById(realm, userId);

        switch (requestMethod) {
            case GET: return Role.USER.isRoleOf(requestingUser, session);
            // Only Role.CHIEF_EDITOR is allowed to set/edit imisId
            case PUT:
                EntityManager em = session.getProvider(
                    JpaConnectionProvider.class).getEntityManager();
                return data.getMeasFacilId() == em.find(
                        Institution.class, data.getId()).getMeasFacilId()
                    && Role.EDITOR.isRoleOf(requestingUser, session)
                    || Role.CHIEF_EDITOR.isRoleOf(requestingUser, session);
            case POST:
                if (data.getMeasFacilId() != null) {
                    return Role.CHIEF_EDITOR.isRoleOf(requestingUser, session);
                }
            case DELETE: return Role.EDITOR.isRoleOf(requestingUser, session);
            default: return false;
        }
    }
}
