/* Copyright (C) 2022 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY!
 */
package de.intevation.iam;

import java.sql.Timestamp;
import java.time.OffsetDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.CriteriaBuilder.In;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

import org.keycloak.connections.jpa.JpaConnectionProvider;
import org.keycloak.email.DefaultEmailSenderProvider;
import org.keycloak.email.EmailException;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;

import de.intevation.iam.auth.Authorizer;
import de.intevation.iam.auth.MailAuthorizer;
import de.intevation.iam.model.jpa.Mail;
import de.intevation.iam.model.jpa.Mail_;
import de.intevation.iam.model.jpa.MailType;
import de.intevation.iam.util.RequestMethod;
import de.intevation.iam.validation.Validator;

/**
 * Class providing rest interfaces for mails, mail types and mailing lists.
 * @author Alexander Woestmann<awoestmann@intevation>
 */
@Produces(MediaType.APPLICATION_JSON)
public class MailResource {

    private static final Logger LOG = Logger.getLogger("MailProvider");

    private static final String FROM_ADDRESS = "from";

    //Keycloak session
    private KeycloakSession session;

    private Authorizer<Mail> auth;

    private Validator validator;

    private EntityManager entityManager;

    /**
     * Constructor.
     * @param session Keycloak session
     */
    public MailResource(KeycloakSession session) {
        this.session = session;
        this.auth = new MailAuthorizer(session);
        this.validator = new Validator();
        this.entityManager = session.getProvider(JpaConnectionProvider.class)
            .getEntityManager();
    }

    /**
     * Get all mail types.
     *
     * Request:
     * GET to /mail/type
     *
     * Response:
     * <pre>
     * <code>
     *[{
     *  id: [Integer], //Type id
     *  name: [String] //Type name
     *}, {
     *   //...
     *}]
     * </code>
     * </pre>
     * @return Response containing mail types as json array
     */
    @GET
    @Path("/type")
    public Response getTypes() {
        EntityManager em = session.getProvider(
            JpaConnectionProvider.class).getEntityManager();
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<MailType> critQuery = cb.createQuery(MailType.class);
        Root<MailType> root = critQuery.from(MailType.class);
        critQuery.select(root);
        TypedQuery<MailType> query = em.createQuery(critQuery);
        List<MailType> types = query.getResultList();
        return Response.ok(types).build();
    }

    /**
     * Get mails.
     *
     * @param types Filter by type IDs given with URL parameter "type"
     * @param count Restrict number of returned mails
     * @param archived if true only archived mails returned
     * @param start Return only mails send after given timestamp
     * @param end Return only mails send before given timestamp
     * @param sender Return only mails from given sender
     * @return Response containing mails as json array
     */
    @GET
    @SuppressWarnings("checkstyle:parameternumber")
    public Response getMails(
        @QueryParam("type") List<Integer> types,
        @QueryParam("count") Integer count,
        @QueryParam("archived") boolean archived,
        @QueryParam("start") OffsetDateTime start,
        @QueryParam("end") OffsetDateTime end,
        @QueryParam("sender") String sender,
        @QueryParam("recipients") List<String> recipients
    ) {
        EntityManager em = session.getProvider(
            JpaConnectionProvider.class).getEntityManager();

        if (!auth.isAuthorized(
                null,
                RequestMethod.GET)
        ) {
            System.out.println(session.getContext().getUserSession().getId());
            return Response.status(Status.UNAUTHORIZED).build();
        }

        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Mail> critQuery = cb.createQuery(Mail.class);
        Root<Mail> root = critQuery.from(Mail.class);
        critQuery.select(root);
        critQuery.orderBy(cb.desc(root.get(Mail_.sendDate)));
        Predicate filter;

        //Filter by recipients
        In<String> recipientFilter = cb.in(root.get("recipients"));
        if (recipients != null && !recipients.isEmpty()) {
            for (String recipient: recipients) {
                recipientFilter.value(recipient);
            }
        }

        //Filter by mails according to "archived" value
        Predicate archiveFilter = cb.equal(root.get(Mail_.archived), archived);

        //Filter by expiry date
        Timestamp now = new Timestamp(new Date().getTime());
        Predicate expiredFilter = cb.greaterThan(
                root.<Timestamp>get(Mail_.expiryDate), now);
        Predicate noDateFilter = cb.isNull(root.get(Mail_.expiryDate));
        Predicate dateExpiredOrNoDateFilter = cb.or(
            expiredFilter, noDateFilter);

        filter = cb.and(recipientFilter, archiveFilter);
        filter = cb.and(filter, dateExpiredOrNoDateFilter);

        //Filter mails by start and end date
        if (start != null) {
            Timestamp startTimestamp = Timestamp.from(start.toInstant());
            Predicate dateFilter = cb.greaterThanOrEqualTo(
                root.<Timestamp>get(Mail_.sendDate), startTimestamp);
            filter = cb.and(filter, dateFilter);
        }
        if (end != null) {
            Timestamp endTimestamp = Timestamp.from(end.toInstant());
            Predicate dateFilter = cb.lessThanOrEqualTo(
                root.<Timestamp>get(Mail_.sendDate), endTimestamp);
            filter = cb.and(filter, dateFilter);
        }

        if (sender != null && !sender.equals("")) {
            Predicate senderFilter = cb.equal(root.get(Mail_.sender), sender);
            filter = cb.and(filter, senderFilter);
        }

        //Filter by mail type
        In<Integer> typeFilter;
        if (types != null && !types.isEmpty()) {
            typeFilter = cb.in(root.get(Mail_.type));
            for (Integer type: types) {
                typeFilter.value(type);
            }
            filter = cb.and(typeFilter, filter);
        }

        critQuery.where(filter);

        TypedQuery<Mail> query = em.createQuery(critQuery);
        if (count != null) {
            query.setFirstResult(0);
            query.setMaxResults(count);
        }
        List<Mail> result = query.getResultList();
        return Response.ok(result).build();
    }

    /**
     * Send a mail to a mailing list.
     *
     * @param headers Request headers
     * @param mail Mail Model
     * @return 200 if send successfully
     */
    @POST
    public Response sendMail(
        @Context HttpHeaders headers,
        final Mail mail
    ) {
        List<Locale> languages = headers.getAcceptableLanguages();
        validator.validate(mail, languages.get(0), entityManager);
        String userId = session.getContext().getUserSession().getId();
        if (userId == null) {
            return Response.status(Status.FORBIDDEN).build();
        }
        if (!auth.isAuthorized(
                mail,
                RequestMethod.POST)) {
            return Response.status(Status.UNAUTHORIZED).build();
        }

        EntityManager em = session.getProvider(
            JpaConnectionProvider.class).getEntityManager();
        RealmModel realm = session.getContext().getRealm();
        DefaultEmailSenderProvider senderProvider
            = new DefaultEmailSenderProvider(session);
        Map<String, String> smtpConfig = new HashMap<String, String>();
        smtpConfig.putAll(realm.getSmtpConfig());

        //Update mail object
        Timestamp now = new Timestamp(System.currentTimeMillis());
        mail.setSendDate(now);
        mail.setUserId(userId);

        //Set smtp Config
        smtpConfig.put(FROM_ADDRESS, mail.getSender());

        for (String user_id: mail.getRecipients()) {
            UserModel user = session.users()
                .getUserById(realm, user_id);
            try {
                senderProvider.send(
                    smtpConfig, user, mail.getSubject(),
                    mail.getText(), mail.getText());
            } catch (EmailException e) {
                LOG.log(Level.SEVERE,
                    "Failed sending mail to user " + user.getUsername());
            }
        }
        em.persist(mail);
        return Response.ok().type(MediaType.APPLICATION_JSON).build();
    }

    /**
     * Archive the mail with the given id.
     * @param mailId Mail id
     * @return 200, 404 if mail could not be found
     */
    @GET
    @Path("/archive/{id}")
    public Response archiveMail(@PathParam("id") Integer mailId) {
        EntityManager em = session.getProvider(
            JpaConnectionProvider.class).getEntityManager();
        Mail mail = em.find(Mail.class, mailId);
        if (mail == null) {
            return Response.status(Status.NOT_FOUND).build();
        }
        if (!auth.isAuthorized(
                mail,
                RequestMethod.POST)) {
            return Response.status(Status.UNAUTHORIZED).build();
        }
        mail.setArchived(true);
        em.persist(mail);
        return Response.ok().type(MediaType.APPLICATION_JSON).build();
    }
}
