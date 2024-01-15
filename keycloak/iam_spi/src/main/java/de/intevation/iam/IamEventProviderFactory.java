/* Copyright (C) 2024 by Bundesamt fuer Strahlenschutz
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

public class IamEventProviderFactory implements RealmResourceProviderFactory {
    public static final String ID = "event";

    @Override
    public void close() { }

    @Override
    public RealmResourceProvider create(KeycloakSession session) {
        return new IamEventProvider(session);
    }

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public void init(Scope arg0) { }

    @Override
    public void postInit(KeycloakSessionFactory arg0) { }
}
