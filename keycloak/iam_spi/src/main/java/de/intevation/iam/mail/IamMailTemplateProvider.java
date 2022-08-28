/* Copyright (C) 2022 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY!
 */
package de.intevation.iam.mail;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.keycloak.email.DefaultEmailSenderProvider;
import org.keycloak.email.EmailException;
import org.keycloak.email.freemarker.FreeMarkerEmailTemplateProvider;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.UserModel;
import org.keycloak.theme.FreeMarkerUtil;

public class IamMailTemplateProvider extends FreeMarkerEmailTemplateProvider {

    DefaultEmailSenderProvider sender;

    public IamMailTemplateProvider(KeycloakSession session, FreeMarkerUtil freeMarker) {
        super(session, freeMarker);
        setRealm(session.getContext().getRealm());
        sender = new DefaultEmailSenderProvider(session);
    }

    public void sendReminder(List<UserModel> users, String topic) {
        Map<String, Object> bodyAttributes = new HashMap<>();
        bodyAttributes.put("topic", topic);
        List<Object> subjectAttributes = new ArrayList<>();
        subjectAttributes.add(topic);
        Map<String, String> realmSmtpConfig = realm.getSmtpConfig();

        users.forEach(user -> {
            try {
                EmailTemplate template = processTemplate(
                        "reminderSubject", subjectAttributes,
                        "reminder.ftl", bodyAttributes);
                sender.send(realmSmtpConfig, user,
                        template.getSubject(), template.getTextBody(), template.getHtmlBody());
            } catch (EmailException e) {
                e.printStackTrace();
            }
        });
    }
}
