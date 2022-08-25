/* Copyright (C) 2022 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY!
 */
package de.intevation.iam.auth;

import java.util.List;
import java.util.Map;

import javax.ws.rs.core.HttpHeaders;

import org.keycloak.models.KeycloakSession;

import de.intevation.iam.model.Institution;
import de.intevation.iam.model.InstitutionCategory;
import de.intevation.iam.model.Mail;
import de.intevation.iam.model.MailList;
import de.intevation.iam.model.User;
import de.intevation.iam.util.RequestMethod;

public class Authorization {

    private Map<Class<?>, Authorizer> authorizers;
    private UserAuthorizer userAuthorizer;
    private InstitutionAuthorizer institutionAuthorizer;
    private InstitutionCategoryAuthorizer institutionCategoryAuthorizer;
    private MailAuthorizer mailAuthorizer;
    private MailListAuthorizer mailListAuthorizer;
    private KeycloakSession session;

    /**
     * Constructor.
     * @param session Keycloak session
     */
    public Authorization(KeycloakSession session) {
        this.session = session;
        this.userAuthorizer = new UserAuthorizer();
        this.institutionAuthorizer = new InstitutionAuthorizer();
        this.institutionCategoryAuthorizer = new InstitutionCategoryAuthorizer();
        this.mailAuthorizer = new MailAuthorizer();
        this.mailListAuthorizer = new MailListAuthorizer();
        this.authorizers = Map.ofEntries(
            Map.entry(User.class, userAuthorizer),
            Map.entry(Institution.class, institutionAuthorizer),
            Map.entry(InstitutionCategory.class, institutionCategoryAuthorizer),
            Map.entry(Mail.class, mailAuthorizer),
            Map.entry(MailList.class, mailListAuthorizer)
        );
    }

    /**
     * Check if user is authorized for the given data and request method.
     * @param data Data
     * @param requestMethod Request method used
     * @param headers Request headers
     * @param clazz Class to be authorized
     * @return True if authorized, else false
     */
    public boolean isAuthorizedById(
        Object data,
        RequestMethod requestMethod,
        HttpHeaders headers,
        Class clazz) {
        Authorizer authorizer = authorizers.get(clazz);
        return authorizer.isAuthorizedById(
            data, requestMethod, headers, this.session);
    }

    public List<Object> filter(
        List<Object> data,
        HttpHeaders headers) {
        Authorizer authorizer = authorizers.get(data.getClass());
        return authorizer.filter(data, headers, session);
    }
}
