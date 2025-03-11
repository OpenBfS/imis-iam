/* Copyright (C) 2022 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY!
 */
package de.intevation.iam;

import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.NotAuthorizedException;

import org.keycloak.authorization.util.Tokens;
import org.keycloak.models.KeycloakSession;
import org.keycloak.representations.AccessToken;
import org.keycloak.services.resource.RealmResourceProvider;

import de.intevation.iam.auth.IaMRole;


public class IamResourceProvider implements RealmResourceProvider {

    private KeycloakSession session;

    /**
     * Constructor.
     * @param session Session
     */
    public IamResourceProvider(KeycloakSession session) {
        this.session = session;
    }

    @Override
    public void close() { }

    @Override
    public Object getResource() {
        // Authenticate that user is granted access to this application.
        AccessToken accessToken = Tokens.getAccessToken(session);
        if (accessToken == null) {
            throw new NotAuthorizedException("Bearer token required");
        }
        if (!IaMRole.USER.isRoleOf(
                session.getContext().getUserSession().getUser(),
                session)) {
            throw new ForbiddenException();
        }

        return new IamResource(this.session);
    }
}
