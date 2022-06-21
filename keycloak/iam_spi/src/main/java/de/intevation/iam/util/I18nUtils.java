package de.intevation.iam.util;

import java.util.Locale;

import org.keycloak.models.UserModel;
import org.keycloak.models.RealmModel;
import org.keycloak.locale.LocaleSelectorProvider;
import org.keycloak.locale.DefaultLocaleSelectorProviderFactory;
import org.keycloak.models.KeycloakSession;

import java.util.ResourceBundle;

public class I18nUtils {

    private I18nUtils() { }

    private static final String BUNDLE_FILE = "iam";

    /**
     * Get the i18n bundle for the given user.
     * @param session Session
     * @param realm Realm
     * @param user Usermodel
     * @return Bundle
     */
    public static ResourceBundle getI18nBundle(
            KeycloakSession session,
            RealmModel realm,
            UserModel user) {
        Locale locale = getUserLocale(session, realm, user);
        return ResourceBundle.getBundle(BUNDLE_FILE, locale);
    }

    /**
     * Get the given users locale.
     * @param session Session
     * @param realm Realm
     * @param user Usermodel
     * @return Locale
     */
    public static Locale getUserLocale(
            KeycloakSession session,
            RealmModel realm,
            UserModel user) {
        DefaultLocaleSelectorProviderFactory factory
            = new DefaultLocaleSelectorProviderFactory();
        LocaleSelectorProvider dlsp = factory.create(session);
        return dlsp.resolveLocale(realm, user);
    }
}
