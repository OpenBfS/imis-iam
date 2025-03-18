/* Copyright (C) 2024 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY!
 */
package de.intevation.iam;

import java.util.List;
import java.util.Locale;

import org.keycloak.connections.jpa.JpaConnectionProvider;
import org.keycloak.models.KeycloakSession;

import de.intevation.iam.auth.EventAuthorizer;
import de.intevation.iam.model.jpa.Event;
import de.intevation.iam.util.RequestMethod;
import de.intevation.iam.validation.Validator;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;


public class IamEventResource {

    private KeycloakSession session;
    private EventAuthorizer auth;

    private Validator validator;

    private EntityManager entityManager;

    /**
     * Constructor.
     * @param session Keycloak session
     */
    public IamEventResource(KeycloakSession session) {
        this.session = session;
        this.auth = new EventAuthorizer(session);
        this.validator = new Validator();
        this.entityManager = session.getProvider(JpaConnectionProvider.class)
            .getEntityManager();
    }

    /**
     * Get all events.
     * @return Response containing event array.
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getEvents() {
        EntityManager em = session.getProvider(
            JpaConnectionProvider.class).getEntityManager();
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Event> query = cb.createQuery(Event.class);
        Root<Event> root = query.from(Event.class);
        query.select(root);
        List<Event> result = auth.filter(
            em.createQuery(query).getResultList()
        );
        return Response.ok(result).build();
    }

    /**
     * Get an event by id.
     * @param id Event id
     * @return Event json or 404 if not found
     */
    @GET
    @Path("/{id}")
    public Response getEventById(@PathParam("id") Integer id) {
        EntityManager em = session.getProvider(
            JpaConnectionProvider.class).getEntityManager();
        Event event = em.find(Event.class, id);
        if (event == null) {
            return Response.status(Status.NOT_FOUND).build();
        }
        event.setReadonly(
            auth.isAuthorized(
                event,
                RequestMethod.PUT));
        return Response.ok(event).build();
    }

    /**
     * Create a new event.
     * @param rep Event representation.
     * @param headers Request headers.
     * @return Persisted event
     */
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public Response createEvent(
        final Event rep,
        @Context HttpHeaders headers
    ) {
        List<Locale> languages = headers.getAcceptableLanguages();
        validator.validate(rep, languages.get(0), entityManager);
        if (rep == null) {
            return Response.status(Status.BAD_REQUEST).build();
        }
        if (!auth.isAuthorized(
                rep,
                RequestMethod.POST)
        ) {
            return Response.status(Status.UNAUTHORIZED).build();
        }
        rep.setReadonly(false);
        EntityManager em = session.getProvider(
            JpaConnectionProvider.class).getEntityManager();
        em.persist(rep);
        return Response.ok(rep).build();
    }

    /**
     * Update an event.
     * @param rep Event representation.
     * @param headers Request headers.
     * @return Updated event
     */
    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateEvent(
        final Event rep,
        @Context HttpHeaders headers
    ) {
        List<Locale> languages = headers.getAcceptableLanguages();
        validator.validate(rep, languages.get(0), entityManager);
        if (rep == null) {
            return Response.status(Status.BAD_REQUEST).build();
        }
        if (!auth.isAuthorized(
                rep,
                RequestMethod.PUT)
        ) {
            return Response.status(Status.UNAUTHORIZED).build();
        }
        EntityManager em = session.getProvider(
            JpaConnectionProvider.class).getEntityManager();
        Event merged = em.merge(rep);
        merged.setReadonly(false);
        return Response.ok(merged).build();
    }

    /**
     * Remove an event by id.
     * @param id Event id
     * @return 200 if done, 404 if not found, 403 if not authorized
     */
    @DELETE
    @Path("/{id}")
    public Response deleteEvent(@PathParam("id") Integer id) {
        EntityManager em = session.getProvider(
            JpaConnectionProvider.class).getEntityManager();
        Event event = em.find(Event.class, id);
        if (event == null) {
            return Response.status(Status.NOT_FOUND).build();
        }
        if (!auth.isAuthorized(
                event,
                RequestMethod.DELETE)
        ) {
            return Response.status(Status.UNAUTHORIZED).build();
        }
        em.remove(event);
        return Response.ok().type(MediaType.APPLICATION_JSON).build();
    }
}
