/* Copyright (C) 2022 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY!
 */
package de.intevation.iam.mail;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

import org.keycloak.email.DefaultEmailSenderProvider;
import org.keycloak.email.EmailException;
import org.keycloak.email.freemarker.FreeMarkerEmailTemplateProvider;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.UserModel;
import org.keycloak.theme.FreeMarkerException;
import org.keycloak.theme.Theme;
import org.keycloak.theme.beans.MessageFormatterMethod;

/**
 * Class providing email templates and sending custom mails.
 */
public class IamMailTemplateProvider extends FreeMarkerEmailTemplateProvider {

    /**
     * Theme to use for mail templates.
     */
    private static final String IAM_THEME = "iam_theme";

    private static final String DEFAULT_LOCALE = "de";

    DefaultEmailSenderProvider sender;

    /**
     * Constructor.
     * @param session Keycloak session
     * @param freeMarker FreeMarker util
     */
    public IamMailTemplateProvider(KeycloakSession session) {
        super(session);
        sender = new DefaultEmailSenderProvider(session);
    }

    /**
     * Send a notifcation about inactive accounts to the given recipient.
     * @param recipient Recipient email address
     * @param inactiveAccounts List of inactive accounts
     * @throws EmailException
     */
    public void sendAccountInactivityNotification(
        String recipient,
        List<UserModel> inactiveAccounts
    ) throws EmailException {
        StringBuilder usernamesBuilder = new StringBuilder();
        inactiveAccounts.forEach(acc -> {
            if (usernamesBuilder.length() > 0) {
                usernamesBuilder.append(", ");
            }
            usernamesBuilder.append(acc.getUsername());
        });
        Map<String, Object> bodyAttributes = new HashMap<>();
        bodyAttributes.put("users", usernamesBuilder.toString());
        Map<String, String> realmSmtpConfig = realm.getSmtpConfig();

        EmailTemplate template = processTemplate(
            "accountInactiveSubject", Collections.emptyList(),
            "accountInactive.ftl", bodyAttributes);
        sender.send(realmSmtpConfig, recipient,
                template.getSubject(),
                template.getTextBody(), template.getHtmlBody());
    }

    /**
     * Send notfications for expired accounts.
     * @param recipient Recipient email address
     * @param expiredUsers Expired account
     * @throws EmailException
     */
    public void sendAccountExpiredNotification(
            String recipient,
            List<UserModel> expiredUsers) throws EmailException {
        StringBuilder usernamesBuilder = new StringBuilder();
        expiredUsers.forEach(acc -> {
            if (usernamesBuilder.length() > 0) {
                usernamesBuilder.append(", ");
            }
            usernamesBuilder.append(acc.getUsername());
        });

        Map<String, Object> bodyAttributes = new HashMap<>();
        bodyAttributes.put("username", usernamesBuilder.toString());
        Map<String, String> realmSmtpConfig = realm.getSmtpConfig();
        EmailTemplate template = processTemplate(
            "accountExpiredSubject", Collections.emptyList(),
            "accountExpired.ftl", bodyAttributes);
        sender.send(realmSmtpConfig, recipient,
                template.getSubject(),
                template.getTextBody(), template.getHtmlBody());
    }

    /**
     * Send a reminder to a user to extend the account.
     * @param user User to send notification to
     * @throws EmailException
     */
    public void sendExtendAccountValidityReminder(UserModel user)
            throws EmailException {
        Map<String, Object> bodyAttributes = new HashMap<>();
        bodyAttributes.put("username", user.getUsername());
        Map<String, String> realmSmtpConfig = realm.getSmtpConfig();
        this.setUser(user);
        EmailTemplate template = processTemplate(
            "extendAccountValiditySubject", Collections.emptyList(),
            "extendAccountValidity.ftl", bodyAttributes);
        sender.send(realmSmtpConfig, user,
                template.getSubject(),
                template.getTextBody(), template.getHtmlBody());
    }

    /**
     * Send generic reminder to the given user list.
     * @param users User list to send mail to.
     * @param topic String used for subject line
     */
    public void sendReminder(List<UserModel> users, String topic) {
        Map<String, Object> bodyAttributes = new HashMap<>();
        bodyAttributes.put("topic", topic);
        List<Object> subjectAttributes = new ArrayList<>();
        subjectAttributes.add(topic);
        Map<String, String> realmSmtpConfig = realm.getSmtpConfig();

        users.forEach(user -> {
            try {
                this.setUser(user);
                EmailTemplate template = processTemplate(
                        "reminderSubject", subjectAttributes,
                        "reminder.ftl", bodyAttributes);
                sender.send(realmSmtpConfig, user,
                        template.getSubject(),
                        template.getTextBody(), template.getHtmlBody());
            } catch (EmailException e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    protected EmailTemplate processTemplate(
            String subjectKey,
            List<Object> subjectAttributes,
            String template,
            Map<String, Object> attributes) throws EmailException {
        try {
            Theme theme = getTheme();
            Locale locale = Locale.forLanguageTag(DEFAULT_LOCALE);
            attributes.put("locale", locale);
            Properties rb = new Properties();
            rb.putAll(theme.getMessages(locale));
            rb.putAll(realm.getRealmLocalizationTextsByLocale(
                    locale.toLanguageTag()));
            attributes.put("msg", new MessageFormatterMethod(locale, rb));
            attributes.put("properties", theme.getProperties());
            attributes.put("realmName", getRealmName());
            String subject = new MessageFormat(
                    rb.getProperty(subjectKey, subjectKey), locale)
                    .format(subjectAttributes.toArray());
            String textTemplate = String.format("text/%s", template);
            String textBody;
            try {
                textBody = freeMarker.processTemplate(
                        attributes, textTemplate, theme);
            } catch (final FreeMarkerException e) {
                throw new EmailException(
                        "Failed to template plain text email.", e);
            }
            String htmlTemplate = String.format("html/%s", template);
            String htmlBody;
            try {
                htmlBody = freeMarker.processTemplate(
                        attributes, htmlTemplate, theme);
            } catch (final FreeMarkerException e) {
                throw new EmailException("Failed to template html email.", e);
            }

            return new EmailTemplate(subject, textBody, htmlBody);
        } catch (Exception e) {
            throw new EmailException("Failed to template email", e);
        }
    }

    @Override
    protected Theme getTheme() throws IOException {
        return session.theme().getTheme(IAM_THEME, Theme.Type.EMAIL);
    }
}
