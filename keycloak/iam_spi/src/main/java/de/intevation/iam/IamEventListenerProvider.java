/* Copyright (C) 2022 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY!
 */
package de.intevation.iam;

import java.util.logging.Logger;

import org.keycloak.events.Event;
import org.keycloak.events.EventListenerProvider;
import org.keycloak.events.admin.AdminEvent;
import org.keycloak.models.KeycloakSession;

import de.intevation.iam.mail.MailScheduler;

public class IamEventListenerProvider implements EventListenerProvider {

    private KeycloakSession session;

    private static final Logger LOG = Logger.getLogger("event-listener");

    private MailScheduler mailScheduler;

    /**
     * Constructor.
     * @param session Keycloak session
     */
    public IamEventListenerProvider(KeycloakSession session) {
        LOG.info("Creating event listener");
        this.session = session;
        mailScheduler = MailScheduler.instance(session);
        mailScheduler.start();
    }

    @Override
    public void close() { }

    @Override
    public void onEvent(Event event) { }

    @Override
    public void onEvent(AdminEvent event, boolean includeRepresentation) { }
}
