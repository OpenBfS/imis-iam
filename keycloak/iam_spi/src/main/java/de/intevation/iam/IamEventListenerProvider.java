/* Copyright (C) 2022 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY!
 */
package de.intevation.iam;

import java.util.logging.Logger;

import jakarta.persistence.EntityManager;

import org.keycloak.connections.jpa.JpaConnectionProvider;
import org.keycloak.events.Event;
import org.keycloak.events.EventListenerProvider;
import org.keycloak.events.EventType;
import org.keycloak.events.admin.AdminEvent;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.UserModel;
import org.keycloak.models.RealmModel;

import de.intevation.iam.mail.MailScheduler;
import de.intevation.iam.model.jpa.UserAttributes;
import de.intevation.iam.model.representation.User;
import de.intevation.iam.util.DateUtils;

public class IamEventListenerProvider implements EventListenerProvider {

    private KeycloakSession session;

    private static final Logger LOG = Logger.getLogger("event-listener");

    private MailScheduler mailScheduler;

    /**
     * Constructor.
     *
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
    public void onEvent(Event event) {
        // Update user account expiry date after login
        if (event.getType() == EventType.LOGIN) {
            RealmModel realm = session.getContext().getRealm();
            UserModel userModel = session.users().getUserById(
                    realm, event.getUserId());
            EntityManager em = session.getProvider(
                    JpaConnectionProvider.class).getEntityManager();
            User user = new User(userModel, em);
            UserAttributes attributes = user.createOrUpdateJpaModel(em);
            attributes.setExpiryDate(
                    DateUtils.getAccountExpiryDate());
            attributes.setExpiredNotificationSent(false);
            attributes.setInactivityNotificationSent(false);
            em.merge(attributes);
        }
    }

    @Override
    public void onEvent(AdminEvent event, boolean includeRepresentation) {
    }
}
