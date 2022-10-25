/* Copyright (C) 2022 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY!
 */
package de.intevation.iam.mail;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.keycloak.connections.jpa.JpaConnectionProvider;
import org.keycloak.email.EmailException;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionTask;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.UserProvider;

import de.intevation.iam.model.jpa.UserAttributes;
import de.intevation.iam.util.Constants;
import de.intevation.iam.util.DateUtils;

/**
 * Task sending email reminders or notfications if run.
 */
public class MailTask implements KeycloakSessionTask {

    private static final String IMIS_REALM = "imis3";

    private static final Logger LOG = Logger.getLogger("MailTask");
    private IamMailTemplateProvider mailTemplateProvider;
    private RealmModel realm;

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
                root.get("expiredNotificationSent"), false);
        Predicate inactivityFilter = cb.lessThan(root.get("expiryDate"),
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
        if (users != null && users.size() > 0) {
            mailTemplateProvider.sendAccountExpiredNotification(
                getNotificationReceipient(session), users);
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
                root.get("inactivityNotificationSent"), false);
        Predicate inactivityFilter = cb.lessThan(root.get("expiryDate"),
        DateUtils.getAccountInactivityDate());
        filter = cb.and(sentFilter, inactivityFilter);
        query.where(filter);
        List<UserAttributes> userAttributes
                = em.createQuery(query).getResultList();
        if (userAttributes.size() == 0) {
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
                    getNotificationReceipient(session), users);
    }

    private void sendReminders(KeycloakSession session) {
        //TODO: Replace example reminders
        mailTemplateProvider.setRealm(realm);
        LOG.info(String.format("Sending reminders for realm %s",
                realm.getName()));
        Stream<UserModel> users = session.users()
                .getUsersStream(realm);
        List<UserModel> userList = users.collect(Collectors.toList());
        LOG.info("Users: " + userList.size());
        userList.forEach(user -> {
            LOG.info("Sending reminder to: " + user.getUsername());
            mailTemplateProvider.setUser(user);
            mailTemplateProvider.sendReminder(
                    userList, "exampleReminder Topic");
        });
    }

    private UserModel getNotificationReceipient(KeycloakSession session) {
        UserProvider userProvider = session.users();
        UserModel receipient = userProvider.getUserByEmail(
                realm, Constants.NOTIFICATION_RECEIPIENT);
        if (receipient == null) {
            receipient = userProvider.addUser(realm, Constants.NOTIFICATION_USERNAME);
            receipient.setEmail(Constants.NOTIFICATION_RECEIPIENT);
            receipient.setEnabled(false);
            receipient.setSingleAttribute("locale", "de");
        }
        return receipient;
    }

    @Override
    public void run(KeycloakSession session) {
        LOG.info("Running mail task");
        mailTemplateProvider
                = new IamMailTemplateProviderFactory().create(session);
        realm = session.realms().getRealmByName(IMIS_REALM);
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
