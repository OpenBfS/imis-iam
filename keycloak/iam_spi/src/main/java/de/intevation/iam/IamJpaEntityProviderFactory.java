package de.intevation.iam;

import org.keycloak.Config.Scope;
import org.keycloak.connections.jpa.entityprovider.JpaEntityProvider;
import org.keycloak.connections.jpa.entityprovider.JpaEntityProviderFactory;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;

public class IamJpaEntityProviderFactory implements JpaEntityProviderFactory{

    protected static final String ID = "iam-entity-provider";

    @Override
    public JpaEntityProvider create(KeycloakSession session) {
        return new IamJpaEntityProvider();
    }

    @Override
    public void init(Scope config) { }

    @Override
    public void postInit(KeycloakSessionFactory factory) { }

    @Override
    public void close() { }

    @Override
    public String getId() {
        return ID;
    }

}
