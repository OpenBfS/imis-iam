/* Copyright (C) 2022 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY!
 */
package de.intevation.iam.mail;

import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionTask;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;

/**
 * Task sending email reminders or notfications if run.
 */
public class MailTask implements KeycloakSessionTask {

    private static final String IMIS_REALM = "imis3";

    private static final Logger LOG = Logger.getLogger("MailTask");
    private IamMailTemplateProvider mailTemplateProvider;

    private void sendReminders(KeycloakSession session) {
        //TODO: Replace example reminders
        RealmModel realm = session.realms().getRealmByName(IMIS_REALM);
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

    @Override
    public void run(KeycloakSession session) {
        LOG.info("Running mail task");
        mailTemplateProvider
                = new IamMailTemplateProviderFactory().create(session);
        sendReminders(session);
    }
}
