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
import java.util.Map;
import java.util.ResourceBundle;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.CriteriaBuilder.In;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
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
import org.keycloak.models.jpa.entities.UserEntity;
import org.keycloak.services.resource.RealmResourceProvider;

import de.intevation.iam.auth.Authorizer;
import de.intevation.iam.auth.MailAuthorizer;
import de.intevation.iam.auth.MailListAuthorizer;
import de.intevation.iam.model.jpa.Mail;
import de.intevation.iam.model.jpa.MailList;
import de.intevation.iam.model.jpa.MailType;
import de.intevation.iam.util.Constants;
import de.intevation.iam.util.I18nUtils;
import de.intevation.iam.util.RequestMethod;

/**
 * Class providing rest interfaces for mails, mail types and mailing lists.
 * @author Alexander Woestmann<awoestmann@intevation>
 */
@Produces(MediaType.APPLICATION_JSON)
public class MailProvider implements RealmResourceProvider {

    private static final String FROM_ADDRESS = "from";
    private static final String USER_ID_HEADER = "X-SHIB-user";
    private static final String ERROR_EMPTY_LIST_KEY = "error_mail_list_empty";
    private static final String ERROR_LIST_NAME_ALREADY_USED_KEY
        = "error_mail_list_name_already_used";

    //Keycloak session
    private KeycloakSession session;

    private Authorizer<Mail> auth;
    private Authorizer<MailList> authList;

    /**
     * Constructor.
     * @param session Keycloak session
     */
    public MailProvider(KeycloakSession session) {
        this.session = session;
        this.auth = new MailAuthorizer(session);
        this.authList = new MailListAuthorizer(session);
    }

    @Override
    public void close() {}

    @Override
    public Object getResource() {
        return this;
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
     * Get mailing lists.
     *
     * Request:
     * <pre>
     * GET to mail/list?subscribed=true
     *  Query Params:
     *    - subscribed: Filter to only show lists
     *                  the current user is subscribed to, optional
     * </pre>
     * Response:
     * <pre>
     * <code>
     *[{
     *  id: [Integer], //List id
     *  name: [String] //List name
     *}, {
     * //...
     *}]
     * </code>
     * </pre>
     * @param headers Request headers
     * @param subscribed If true only list are returned
     *                   that the user is subscribed to
     * @return Response containing mailing lists as json array
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/list")
    public Response getLists(
            @Context HttpHeaders headers,
            @QueryParam("subscribed") boolean subscribed) {
        List<MailList> lists
            = getMailLists(headers.getHeaderString(USER_ID_HEADER), subscribed);
        return Response.ok(lists).build();
    }

    /**
     * Get a mailing list by given id.
     *
     * Request:
     * GET to mail/list/{id}
     *
     * Response:
     * <pre>
     * <code>
     *{
     *  id: [Integer], //List id
     *  name: [String] //List name
     *}
     * </code>
     * </pre>
     *
     * @param id List id
     * @return Response containing list as json object
     */
    @GET
    @Path("/list/{id}")
    public Response getListById(@PathParam("id") Integer id) {
        EntityManager em = session.getProvider(
            JpaConnectionProvider.class).getEntityManager();
        MailList list = em.find(MailList.class, id);
        if (list == null) {
            return Response.status(Status.NOT_FOUND).build();
        }
        return Response.ok(list).build();
    }

    /**
     * Create a new mailing list.
     *
     * Request:
     * POST to mail/list
     * Body:
     * <pre>
     * <code>
     *{
     *  name: [String] //New list name
     *}
     * </code>
     * </pre>
     *
     * Response:
     * <pre>
     * <code>
     *{
     *  id: [Integer], //List id
     *  name: [String] //List name
     *}
     * </code>
     * </pre>
     * @param list List model
     * @param headers Request headers
     * @return Response containing new list as json
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/list")
    public Response createList(final MailList list,
            @Context HttpHeaders headers) {
        if (list == null) {
            return Response.status(Status.BAD_REQUEST).build();
        }
        if (!authList.isAuthorizedById(list, RequestMethod.POST, headers)) {
            return Response.status(Status.UNAUTHORIZED).build();
        }
        EntityManager em = session.getProvider(
            JpaConnectionProvider.class).getEntityManager();
        RealmModel realm = session.getContext().getRealm();
        String id = headers.getHeaderString(Constants.SHIB_USER_HEADER);
        UserModel requestingUser = session.users().getUserById(realm, id);
        ResourceBundle i18n
            = I18nUtils.getI18nBundle(session, realm, requestingUser);

        if (getMailListByName(list.getName(), em) != null) {
            return Response.status(Status.CONFLICT)
            .type(MediaType.APPLICATION_JSON)
            .entity(i18n.getString(ERROR_LIST_NAME_ALREADY_USED_KEY))
            .build();
        }
        //Update user entities
        list.updateUserEntities(em);
        if (list.getUsers() == null
                || list.getUsers().size() == 0) {
            return Response.status(Status.BAD_REQUEST)
                .type(MediaType.APPLICATION_JSON)
                .entity(i18n.getString(ERROR_EMPTY_LIST_KEY))
                .build();
        }
        em.persist(list);
        return Response.ok(list).build();
    }

    /**
     * Update a mailing list.
     *
     * <pre>
     * Request:
     * PUT to mail/list
     * Body:
     * <code>
     *{
     *  id: [Integer], //List id
     *  name: [String], //List name
     *}
     * </code>
     * Response:
     * <code>
     *{
     *  id: [Integer], //List id
     *  name: [String], //List name
     *}
     * </code>
     * </pre>
     * @param list List model
     * @param headers Request headers
     * @return Response containing updated list as json
     */
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/list")
    public Response updateList(final MailList list,
            @Context HttpHeaders headers) {
        EntityManager em = session.getProvider(
            JpaConnectionProvider.class).getEntityManager();
        MailList originalList = em.find(MailList.class, list.getId());
        list.updateUserEntities(em);
        if (list == null || list.getId() == null
                || originalList == null) {
            return Response.status(Status.BAD_REQUEST).build();
        }
        if (!authList.isAuthorizedById(list, RequestMethod.PUT, headers)) {
            return Response.status(Status.UNAUTHORIZED).build();
        }
        RealmModel realm = session.getContext().getRealm();
        String id = headers.getHeaderString(Constants.SHIB_USER_HEADER);
        UserModel requestingUser = session.users().getUserById(realm, id);
        ResourceBundle i18n
            = I18nUtils.getI18nBundle(session, realm, requestingUser);
        MailList foundList = getMailListByName(list.getName(), em);
        if (foundList != null
            && !foundList.getId().equals(list.getId())) {
            return Response.status(Status.CONFLICT)
            .type(MediaType.APPLICATION_JSON)
            .entity(i18n.getString(ERROR_LIST_NAME_ALREADY_USED_KEY))
            .build();
        }

        if (list.getUsers() == null
                || list.getUsers().size() == 0) {
            return Response.status(Status.BAD_REQUEST)
                .type(MediaType.APPLICATION_JSON)
                .entity(i18n.getString(ERROR_EMPTY_LIST_KEY))
                .build();
        }
        MailList mergedList = em.merge(list);
        return Response.ok(mergedList).build();
    }

    /**
     * Delete the mailing list with the given id.
     *
     * Request:
     * DELETE to mail/list/{id}
     * @param id List id
     * @return 200 if deleted successfully, 404 if list was not found
     */
    @DELETE
    @Path("/list/{id}")
    public Response removeList(@PathParam("id") Integer id,
            @Context HttpHeaders headers) {
        EntityManager em = session.getProvider(
            JpaConnectionProvider.class).getEntityManager();
        MailList list = em.find(MailList.class, id);
        if (list == null) {
            return Response.status(Status.NOT_FOUND).build();
        }
        if (!authList.isAuthorizedById(list, RequestMethod.DELETE, headers)) {
            return Response.status(Status.UNAUTHORIZED).build();
        }
        em.remove(list);
        return Response.ok().type(MediaType.APPLICATION_JSON).build();
    }

    /**
     * Join the mailing list with the given id.
     *
     * Request:
     * GET to mail/list/{id}/join
     * @param headers Request headers
     * @param id Mailing list id
     * @return 200 if joined successfully, 404 if not found
     */
    @GET
    @Path("/list/{id}/join")
    public Response joinList(
        @Context HttpHeaders headers,
        @PathParam("id") Integer id) {
        String userId = headers.getHeaderString(USER_ID_HEADER);
        if (userId == null) {
            return Response.status(Status.FORBIDDEN).build();
        }
        EntityManager em = session.getProvider(
            JpaConnectionProvider.class).getEntityManager();
        MailList list = em.find(MailList.class, id);
        if (list == null) {
            return Response.status(Status.NOT_FOUND).build();
        }
        UserEntity user = em.find(UserEntity.class, userId);
        list.getUserEntities().add(user);
        em.merge(list);
        return Response.ok().type(MediaType.APPLICATION_JSON).build();
    }

    /**
     * Leave the mailing list with the given id.
     *
     * Request:
     * GET to mail/list/{id}/leave
     * @param headers Request headers
     * @param mailListId Mail list id
     * @return 200 if left successfully, 404 if not found
     */
    @GET
    @Path("/list/{id}/leave")
    public Response leaveList(
        @Context HttpHeaders headers,
        @PathParam("id") Integer mailListId) {
        String userId = headers.getHeaderString(USER_ID_HEADER);
        if (userId == null) {
            return Response.status(Status.FORBIDDEN).build();
        }
        EntityManager em = session.getProvider(
            JpaConnectionProvider.class).getEntityManager();
        MailList list = em.find(MailList.class, mailListId);
        if (list == null) {
            return Response.status(Status.NOT_FOUND).build();
        }
        UserEntity user = em.find(UserEntity.class, userId);
        list.getUserEntities().remove(user);
        em.merge(list);

        return Response.ok().type(MediaType.APPLICATION_JSON).build();
    }

    /**
     * Get mails.
     *
     * @param headers Request headers
     * @param types Filter by type IDs given with URL parameter "type"
     * @param count Restrict number of returned mails
     * @param archived if true only archived mails returned
     * @param start Return only mails send after given timestamp
     * @param end Return only mails send before given timestamp
     * @param sender Return only mails from given sender
     * @param lists Filter by mailing list IDs given with URL parameter "list"
     * @return Response containing mails as json array
     */
    @GET
    @SuppressWarnings("checkstyle:parameternumber")
    public Response getMails(
        @Context HttpHeaders headers,
        @QueryParam("type") List<Integer> types,
        @QueryParam("count") Integer count,
        @QueryParam("archived") boolean archived,
        @QueryParam("start") OffsetDateTime start,
        @QueryParam("end") OffsetDateTime end,
        @QueryParam("sender") String sender,
        @QueryParam("list") List<Integer> lists
    ) {
        String userId = headers.getHeaderString(USER_ID_HEADER);
        EntityManager em = session.getProvider(
            JpaConnectionProvider.class).getEntityManager();

        if (!auth.isAuthorizedById(null, RequestMethod.GET, headers)) {
            return Response.status(Status.UNAUTHORIZED).build();
        }

        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Mail> critQuery = cb.createQuery(Mail.class);
        Root<Mail> root = critQuery.from(Mail.class);
        critQuery.select(root);
        critQuery.orderBy(cb.desc(root.get("sendDate")));
        Predicate filter;

        //Filter by mailing lists the user is subscribed to
        List<MailList> mailLists = getMailLists(userId, true);
        In<Integer> listFilter = cb.in(root.get("recipient"));
        if (lists != null && !lists.isEmpty()) {
            for (MailList list: mailLists) {
                if (lists.contains(list.getId())) {
                    listFilter.value(list.getId());
                }
            }
        } else {
            for (MailList list: mailLists) {
                listFilter.value(list.getId());
            }

        }

        //Filter by mails according to "archived" value
        Predicate archiveFilter = cb.equal(root.get("archived"), archived);

        //Filter by expiry date
        Timestamp now = new Timestamp(new Date().getTime());
        Predicate expiredFilter = cb.greaterThan(
                root.<Timestamp>get("expiryDate"), now);
        Predicate noDateFilter = cb.isNull(root.get("expiryDate"));
        Predicate dateExpiredOrNoDateFilter = cb.or(
            expiredFilter, noDateFilter);

        filter = cb.and(listFilter, archiveFilter);
        filter = cb.and(filter, dateExpiredOrNoDateFilter);

        //Filter mails by start and end date
        if (start != null) {
            Timestamp startTimestamp = Timestamp.from(start.toInstant());
            Predicate dateFilter = cb.greaterThanOrEqualTo(
                root.<Timestamp>get("sendDate"), startTimestamp);
            filter = cb.and(filter, dateFilter);
        }
        if (end != null) {
            Timestamp endTimestamp = Timestamp.from(end.toInstant());
            Predicate dateFilter = cb.lessThanOrEqualTo(
                root.<Timestamp>get("sendDate"), endTimestamp);
            filter = cb.and(filter, dateFilter);
        }

        if (sender != null && !sender.equals("")) {
            Predicate senderFilter = cb.equal(root.get("sender"), sender);
            filter = cb.and(filter, senderFilter);
        }

        //Filter by mail type
        In<Integer> typeFilter;
        if (types != null && !types.isEmpty()) {
            typeFilter = cb.in(root.get("type"));
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
        String userId = headers.getHeaderString(USER_ID_HEADER);
        if (userId == null) {
            return Response.status(Status.FORBIDDEN).build();
        }
        if (!auth.isAuthorizedById(mail, RequestMethod.POST, headers)) {
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

        //Get mail list
        MailList list = em.find(MailList.class, mail.getRecipient());

        for (UserEntity entity: list.getUserEntities()) {
            UserModel user = session.users()
                .getUserById(realm, entity.getId());
            try {
                senderProvider.send(
                    smtpConfig, user, mail.getSubject(),
                    mail.getText(), mail.getText());
            } catch (EmailException e) {
                e.printStackTrace();
                return Response.status(Status.INTERNAL_SERVER_ERROR).build();
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
    public Response archiveMail(@PathParam("id") Integer mailId,
            @Context HttpHeaders headers) {
        EntityManager em = session.getProvider(
            JpaConnectionProvider.class).getEntityManager();
        Mail mail = em.find(Mail.class, mailId);
        if (mail == null) {
            return Response.status(Status.NOT_FOUND).build();
        }
        if (!auth.isAuthorizedById(mail, RequestMethod.POST, headers)) {
            return Response.status(Status.UNAUTHORIZED).build();
        }
        mail.setArchived(true);
        em.persist(mail);
        return Response.ok().type(MediaType.APPLICATION_JSON).build();
    }

    private List<MailList> getMailLists(String userId, boolean subscribed) {
        EntityManager em = session.getProvider(
            JpaConnectionProvider.class).getEntityManager();
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<MailList> critQuery = cb.createQuery(MailList.class);
        Root<MailList> root = critQuery.from(MailList.class);
        critQuery.select(root);
        if (subscribed) {
            Join<MailList, UserEntity> join =
                root.join("userEntities", JoinType.LEFT);
            Predicate filter = cb.equal(join.get("id"), userId);
            critQuery.where(filter);
        }
        critQuery.orderBy(cb.asc(root.get("name")));
        TypedQuery<MailList> query = em.createQuery(critQuery);
        List<MailList> lists = query.getResultList();
        return lists;
    }

    private MailList getMailListByName(String name, EntityManager em) {
        String queryString = "from MailList m where m.name = :name";
        TypedQuery<MailList> query
            = em.createQuery(queryString, MailList.class);
        query.setParameter("name", name);
        try {
            return query.getSingleResult();
        } catch (NoResultException nre) {
            return null;
        }
    }
}
