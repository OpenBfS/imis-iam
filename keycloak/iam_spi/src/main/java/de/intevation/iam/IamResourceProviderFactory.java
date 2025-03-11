/* Copyright (C) 2022 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY!
 */
package de.intevation.iam;

import org.keycloak.Config.Scope;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.services.resource.RealmResourceProvider;
import org.keycloak.services.resource.RealmResourceProviderFactory;

/**
 * RealmResourceProviderFactory used to create the UserUrlProvider.
 *
 * @author Alexander Woestmann <awoestmann@intevation.de>
 */
public class IamResourceProviderFactory implements RealmResourceProviderFactory {

    public static final String ID = "iam";

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public RealmResourceProvider create(KeycloakSession session) {
        return new IamResourceProvider(session);
    }

    @Override
    public void init(Scope config) {
    }

    @Override
    public void postInit(KeycloakSessionFactory factory) {
    }

    @Override
    public void close() {
    }
}
