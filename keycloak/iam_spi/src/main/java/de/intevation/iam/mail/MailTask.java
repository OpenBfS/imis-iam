/* Copyright (C) 2022 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY!
 */
package de.intevation.iam.mail;

import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

import org.keycloak.connections.jpa.JpaConnectionProvider;
import org.keycloak.email.EmailException;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionTask;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;

import de.intevation.iam.model.jpa.UserAttributes;
import de.intevation.iam.model.jpa.UserAttributes_;


/**
 * Task sending email reminders or notfications if run.
 */
public class MailTask implements KeycloakSessionTask {

    private static final String IMIS_REALM = "imis3";
    private static final String IAM_NOTIFICATION_RECIPIENT_ENV
        = "IAM_NOTIFICATION_RECIPIENT";

    private static final Logger LOG = Logger.getLogger("MailTask");
    private IamMailTemplateProvider mailTemplateProvider;
    private RealmModel realm;

    private Duration inactivityWarningTerm;

    MailTask(Duration inactivityWarningTerm) {
        this.inactivityWarningTerm = inactivityWarningTerm;
    }

    /**
     * Check for expired user accounts, disable them and send email notfication.
     * @param session Keycloak session
     * @throws EmailException
     */
    private void checkForExpiredAcccounts(KeycloakSession session)
            throws EmailException {
        EntityManager em = session.getProvider(
            JpaConnectionProvider.class).getEntityManager();
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<UserAttributes> query
                = cb.createQuery(UserAttributes.class);
        Root<UserAttributes> root = query.from(UserAttributes.class);
        query.select(root);
        Predicate filter;
        Predicate sentFilter = cb.equal(
                root.get(UserAttributes_.expiredNotificationSent), false);
        Predicate inactivityFilter = cb.lessThan(
            root.get(UserAttributes_.expiryDate),
        new Timestamp(System.currentTimeMillis()));
        filter = cb.and(sentFilter, inactivityFilter);
        query.where(filter);
        List<UserAttributes> userAttributes
                = em.createQuery(query).getResultList();
        List<UserModel> users = new ArrayList<>();
        userAttributes.forEach(user -> {
            LOG.info(String.format("Expired user: %s", user.getId()));
            UserModel userModel = session.users().getUserById(
                    realm, user.getId());
            users.add(userModel);
            userModel.setEnabled(false);
            user.setExpiredNotificationSent(true);
            em.merge(user);
        });
        if (!users.isEmpty()) {
            mailTemplateProvider.sendAccountExpiredNotification(
                getNotificationRecipient(), users);
        }
    }

    private void sendAccountInactivityNotifications(KeycloakSession session)
            throws EmailException {
        EntityManager em = session.getProvider(
            JpaConnectionProvider.class).getEntityManager();
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<UserAttributes> query
                = cb.createQuery(UserAttributes.class);
        Root<UserAttributes> root = query.from(UserAttributes.class);
        query.select(root);
        Predicate filter;
        Predicate sentFilter = cb.equal(
                root.get(UserAttributes_.inactivityNotificationSent), false);
        Predicate inactivityFilter = cb.lessThan(
            root.get(UserAttributes_.expiryDate),
            Timestamp.from(Instant.now().plus(inactivityWarningTerm)));
        filter = cb.and(sentFilter, inactivityFilter);
        query.where(filter);
        List<UserAttributes> userAttributes
                = em.createQuery(query).getResultList();
        if (userAttributes.isEmpty()) {
            return;
        }
        List<UserModel> users = new ArrayList<>();
        userAttributes.forEach(user -> {
            LOG.info(String.format("Inactive user: %s", user.getId()));
            users.add(session.users().getUserById(realm, user.getId()));
            user.setInactivityNotificationSent(true);
            em.merge(user);
        });
        mailTemplateProvider.sendAccountInactivityNotification(
                    getNotificationRecipient(), users);
    }

    private String getNotificationRecipient() {
        String rec = System.getenv(IAM_NOTIFICATION_RECIPIENT_ENV);
        if (rec == null || rec.isEmpty()) {
            throw new RuntimeException(
                String.format("Env %s is not set",
                IAM_NOTIFICATION_RECIPIENT_ENV));
        }
        return rec;
    }

    @Override
    public void run(KeycloakSession session) {
        LOG.info("Running mail task");
        mailTemplateProvider
                = new IamMailTemplateProviderFactory().create(session);
        realm = session.realms().getRealmByName(IMIS_REALM);
        session.getContext().setRealm(realm);
        mailTemplateProvider.setRealm(realm);
        try {
            checkForExpiredAcccounts(session);
            sendAccountInactivityNotifications(session);
        } catch (EmailException e) {
            LOG.warning("MailTask failed");
            e.printStackTrace();
        }
    }
}
