/* Copyright (C) 2025 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY!
 */

package de.intevation.iam;

import org.keycloak.models.KeycloakSession;

import jakarta.ws.rs.Path;


public class IamResource {

    private KeycloakSession session;

    IamResource(KeycloakSession session) {
        this.session = session;
    }

    @Path("user")
    public UserResource getUserResource() {
        return new UserResource(this.session);
    }

    @Path("mail")
    public MailResource getMailResource() {
        return new MailResource(this.session);
    }

    @Path("institution")
    public InstitutionResource getInstitutionResource() {
        return new InstitutionResource(this.session);
    }

    @Path("event")
    public IamEventResource getEventResource() {
        return new IamEventResource(this.session);
    }

    @Path("export")
    public ExportResource getExportResource() {
        return new ExportResource(this.session);
    }

    @Path("networks")
    public NetworkResource getNetworkResource() {
        return new NetworkResource(this.session);
    }
}
