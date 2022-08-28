/* Copyright (C) 2022 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY!
 */
package de.intevation.iam.mail;

import org.keycloak.email.freemarker.FreeMarkerEmailTemplateProviderFactory;
import org.keycloak.models.KeycloakSession;
import org.keycloak.theme.FreeMarkerUtil;

public class IamMailTemplateProviderFactory
        extends FreeMarkerEmailTemplateProviderFactory {

    private FreeMarkerUtil freeMarker;

    @Override
    public IamMailTemplateProvider create(KeycloakSession session) {
        freeMarker = new FreeMarkerUtil();
        return new IamMailTemplateProvider(session, freeMarker);
    }

    @Override
    public String getId() {
        return "iam-mail-template-provider";
    }
}
