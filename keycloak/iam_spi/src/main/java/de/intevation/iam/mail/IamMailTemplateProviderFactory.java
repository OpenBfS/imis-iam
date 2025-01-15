/* Copyright (C) 2022 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY!
 */
package de.intevation.iam.mail;

import org.keycloak.email.freemarker.FreeMarkerEmailTemplateProviderFactory;
import org.keycloak.models.KeycloakSession;

public class IamMailTemplateProviderFactory
        extends FreeMarkerEmailTemplateProviderFactory {

    @Override
    public IamMailTemplateProvider create(KeycloakSession session) {
        return new IamMailTemplateProvider(session);
    }

    @Override
    public String getId() {
        return "iam-mail-template-provider";
    }

    @Override
    public int order() {
        // All providers with priority <= 0 are ignored for automatic selection
        return 1;
    }
}
