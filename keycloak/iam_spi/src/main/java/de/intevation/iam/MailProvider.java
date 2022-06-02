package de.intevation.iam;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.keycloak.connections.jpa.JpaConnectionProvider;
import org.keycloak.email.DefaultEmailSenderProvider;
import org.keycloak.email.EmailException;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.services.resource.RealmResourceProvider;

import de.intevation.iam.model.Mail;
import de.intevation.iam.model.MailList;
import de.intevation.iam.model.MailListUser;
import de.intevation.iam.model.MailType;

/**
 * Class providing rest interfaces for mails, mail types and mailing lists.
 * @author Alexander Woestmann<awoestmann@intevation>
 */
public class MailProvider implements RealmResourceProvider{
    //Constants
    private static final String FROM_DISPLAY_NAME = "fromDisplayName";
    private static final String FROM_ADDRESS = "from";

    //Keycloak session
    private KeycloakSession session;

    public MailProvider(KeycloakSession session) {
        this.session = session;
    }

    @Override
    public void close() {}

    @Override
    public Object getResource() {
        return this;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/type")
    public Response getTypes() {
        EntityManager em = session.getProvider(JpaConnectionProvider.class).getEntityManager();
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<MailType> critQuery = cb.createQuery(MailType.class);
        Root<MailType> root = critQuery.from(MailType.class);
        critQuery.select(root);
        TypedQuery<MailType> query = em.createQuery(critQuery);
        List<MailType> types = query.getResultList();
        return Response.ok(types).build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/list")
    public Response getLists() {
        EntityManager em = session.getProvider(JpaConnectionProvider.class).getEntityManager();
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<MailList> critQuery = cb.createQuery(MailList.class);
        Root<MailList> root = critQuery.from(MailList.class);
        critQuery.select(root);
        TypedQuery<MailList> query = em.createQuery(critQuery);
        List<MailList> lists = query.getResultList();
        return Response.ok(lists).build();
    }

    @GET
    @Path("/list/{id}")
    public Response getListById(@PathParam("id") Integer id) {
        EntityManager em = session.getProvider(JpaConnectionProvider.class).getEntityManager();
        MailList list = em.find(MailList.class, id);
        if (list == null) {
            return Response.status(Status.NOT_FOUND).build();
        }
        return Response.ok(list).build();
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/list")
    public Response createList(final MailList list) {
        if (list == null) {
            return Response.status(Status.BAD_REQUEST).build();
        }
        EntityManager em = session.getProvider(JpaConnectionProvider.class).getEntityManager();
        em.persist(list);
        return Response.ok(list).build();
    } 

    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/list")
    public Response updateList(final MailList list) {
        EntityManager em = session.getProvider(JpaConnectionProvider.class).getEntityManager();

        if (list == null || list.getId() == null
                || em.find(MailList.class, list.getId()) == null) {
            return Response.status(Status.BAD_REQUEST).build();
        }
        MailList mergedList = em.merge(list);
        return Response.ok(mergedList).build();
    }

    @DELETE
    @Path("/list/{id}")
    public Response removeList(@PathParam("id") Integer id) {
        EntityManager em = session.getProvider(JpaConnectionProvider.class).getEntityManager();
        MailList list = em.find(MailList.class, id);
        if (list == null) {
            return Response.status(Status.NOT_FOUND).build();
        }
        em.remove(list);
        return Response.ok().build();
    }

    @GET
    @Path("/list/{id}/join")
    public Response joinList(
        @Context HttpHeaders headers,
        @PathParam("id") Integer id) {
            String userId = headers.getHeaderString("X-SHIB-user");
            if (userId == null) {
                return Response.status(Status.FORBIDDEN).build();
            }
        EntityManager em = session.getProvider(JpaConnectionProvider.class).getEntityManager();
        MailListUser entry = new MailListUser();
        entry.setMailListId(id);
        entry.setUserId(userId);
        em.persist(entry);
        return Response.ok().build();
    }

    @GET
    @Path("/list/{id}/leave")
    public Response leaveList(
        @Context HttpHeaders headers,
        @PathParam("id") Integer mailListId) {
        String userId = headers.getHeaderString("X-SHIB-user");
        if (userId == null) {
            return Response.status(Status.FORBIDDEN).build();
        }
        EntityManager em = session.getProvider(JpaConnectionProvider.class).getEntityManager();
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<MailListUser> critQuery = cb.createQuery(MailListUser.class);
        Root<MailListUser> root = critQuery.from(MailListUser.class);
        Predicate filter = cb.and(
            cb.equal(root.get("userId"), userId),
            cb.equal(root.get("mailListId"), mailListId)
        );
        critQuery.select(root).where(filter);
        TypedQuery<MailListUser> query = em.createQuery(critQuery);
        List<MailListUser> entries = query.getResultList();
        if (entries == null || entries.size() == 0) {
            return Response.status(Status.NOT_FOUND).build();
        }
        em.remove(entries.get(0));
        return Response.ok().build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getMails(
        @QueryParam("type") Integer type
    ) {
        EntityManager em = session.getProvider(JpaConnectionProvider.class).getEntityManager();
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Mail> critQuery = cb.createQuery(Mail.class);
        Root<Mail> root = critQuery.from(Mail.class);
        critQuery.select(root);
        if (type != null) {
            Predicate filter = cb.equal(root.get("type"), type);
            critQuery.where(filter);
        }
        TypedQuery<Mail> query = em.createQuery(critQuery);
        List<Mail> result = query.getResultList();
        return Response.ok(result).build();
    }

    @POST
    public Response sendMail(
        @Context HttpHeaders headers,
        final Mail mail) {
        String userId = headers.getHeaderString("X-SHIB-user");
        if (userId == null) {
            return Response.status(Status.FORBIDDEN).build();
        }

        EntityManager em = session.getProvider(JpaConnectionProvider.class).getEntityManager();
        RealmModel realm = session.getContext().getRealm();
        DefaultEmailSenderProvider senderProvider = new DefaultEmailSenderProvider(session);
        Map<String, String> realmSmtpConfig = realm.getSmtpConfig();
        Map<String, String> smtpConfig = new HashMap<String, String>();
        smtpConfig.putAll(realmSmtpConfig);

        //Update mail object
        Timestamp now = new Timestamp(System.currentTimeMillis());
        mail.setSendDate(now);
        mail.setUserId(userId);

        //Set smtp Config
        smtpConfig.put(FROM_ADDRESS, mail.getSender());

        //Get receipient users
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<MailListUser> critQuery = cb.createQuery(MailListUser.class);
        Root<MailListUser> root = critQuery.from(MailListUser.class);
        Predicate filter = cb.equal(root.get("mailListId"), mail.getReceipient());
        critQuery.select(root).where(filter);
        TypedQuery<MailListUser> query = em.createQuery(critQuery);
        List<MailListUser> entries = query.getResultList();

        for(MailListUser mlu: entries) {
            UserModel user = session.users().getUserById(realm, mlu.getUserId());
            try {
                senderProvider.send(smtpConfig, user, mail.getSubject(), mail.getText(), mail.getText());
            } catch (EmailException e) {
                e.printStackTrace();
                return Response.status(Status.INTERNAL_SERVER_ERROR).build();
            }
        }

        em.persist(mail);
        return Response.ok().build();
    }
}
