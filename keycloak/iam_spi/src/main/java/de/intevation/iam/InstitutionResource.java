/* Copyright (C) 2022 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY!
 */
package de.intevation.iam;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Objects;
import java.util.Map;
import java.util.List;
import java.util.Set;
import java.util.Locale;

import de.intevation.iam.model.jpa.Institution_;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotFoundException;
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

import org.hibernate.query.criteria.JpaExpression;
import org.keycloak.connections.jpa.JpaConnectionProvider;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.UserModel;

import de.intevation.iam.auth.Authorizer;
import de.intevation.iam.auth.InstitutionAuthorizer;
import de.intevation.iam.auth.IaMRole;
import de.intevation.iam.model.jpa.Institution;
import de.intevation.iam.model.jpa.InstitutionTag;
import de.intevation.iam.model.representation.ObjectList;
import de.intevation.iam.util.RequestMethod;
import de.intevation.iam.validation.Validator;
import org.keycloak.utils.SearchQueryUtils;

/**
 * Class providing rest interfaces to create and get Institutions.
 * @author Alexander Woestmann <awoestmann@intevation.de>
 */
@Produces(MediaType.APPLICATION_JSON)
public class InstitutionResource {

    public enum SortOrder {
        asc,
        desc
    }

    private KeycloakSession session;

    private Authorizer<Institution> auth;

    private Validator validator;

    private EntityManager entityManager;

    /**
     * Constructor.
     * @param session Keycloak session
     */
    public InstitutionResource(KeycloakSession session) {
        this.session = session;
        this.auth = new InstitutionAuthorizer(session);
        this.validator = new Validator();
        this.entityManager = session.getProvider(JpaConnectionProvider.class)
            .getEntityManager();
    }

    private Predicate getFilterQuery(Root<Institution> root, CriteriaBuilder cb, Map<String, String> attributes) {
        List<Predicate> predicates = new ArrayList<>();

        String generalSearch = attributes.get("search");
        if (generalSearch != null) {
            String value = "%" + generalSearch.toLowerCase() + "%";
            predicates.add(cb.or(
                    cb.like(
                            cb.lower(root.get("name")),
                            value),
                    cb.like(
                            cb.lower(root.get("measFacilId")),
                            value)
            ));
        }

        for (Map.Entry<String, String> entry : attributes.entrySet()) {
            if (entry.getKey().equals("search")) {
                continue;
            }

            String value = "%" + entry.getValue().toLowerCase() + "%";
            try {
                Field field = Institution.class.getDeclaredField(entry.getKey());
                if (field.getType().isAssignableFrom(List.class)
                    || field.getType().isAssignableFrom(Set.class)) {
                    Join<Institution, ?> join = root.join(entry.getKey());
                    if (Objects.equals(entry.getKey(), Institution_.TAGS)) {
                        predicates.add(cb.like(
                                cb.lower(join.get("name")),
                                value));
                    } else {
                        predicates.add(cb.like(
                                cb.lower(((JpaExpression) join).cast(String.class)),
                                value));
                    }

                } else {
                    predicates.add(cb.like(
                            cb.lower(((JpaExpression) root.get(entry.getKey())).cast(String.class)),
                            value
                    ));
                }
            } catch (NoSuchFieldException ignored) { }
        }
        return cb.and(predicates.toArray(new Predicate[0]));
    }

    /**
     * Get all institutions.
     * Response:
     * <pre>
     * <code>
     * [{
     *   id: [String] Institution ID,
     *   name: [String] Institution Name,
     *   shortName: [String] Institution short name,
     *   category: [Integer] Institution category id,
     *   serviceBuildingStreet: [String] Institution service building street,
     *   serviceBuildingPostalCode: [String] Institution service building
     *      postal code,
     *   serviceBuildingLocation: [String] Institution service building
     *      location,
     *   addressStreet: [String] Institution address street,
     *   addressPostalCode: [String] Institution address postal code,
     *   addressLocation: [String] Institution address location,
     *   centralPhone: [String] Central phone
     *   centralFax: [String] Central fax
     *   centralMail: [String] Central mail,
     *   imisId: [String] Institution imis id,
     *   xCoordinate: [Float] X coordinate,
     *   yCoordinate: [Float] Y coordinate,
     *   active: [Boolean] True if institution is active
     * },
     * {
     *   ...
     * }]
     * </code>
     * </pre>
     * @param search Optional search parameter
     * @param firstResult First result to return
     * @param maxResults Maximum numbers of results to return
     * @param sortBy Attribute to sort for
     * @param order Sort order: ascending and descending
     * @return Array of institutions
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public ObjectList<Institution> getInstitutions(
            @QueryParam("search") String search,
            @QueryParam("firstResult") Integer firstResult,
            @QueryParam("maxResults") Integer maxResults,
            @QueryParam("sortBy") String sortBy,
            @QueryParam("order") SortOrder order) {
        Map<String, String> attributes = search == null
                ? Collections.emptyMap()
                : SearchQueryUtils.getFields(search);


        EntityManager em = session.getProvider(
            JpaConnectionProvider.class).getEntityManager();


        long size = 0;
        if (firstResult != null || maxResults != null) {
            CriteriaBuilder cbSize = em.getCriteriaBuilder();
            CriteriaQuery<Long> cqSize = cbSize.createQuery(Long.class);
            Root<Institution> rootSize = cqSize.from(Institution.class);
            cqSize.select(cbSize.count(rootSize));
            if (!attributes.isEmpty()) {
                cqSize.where(getFilterQuery(rootSize, cbSize, attributes));
            }
            size = em.createQuery(cqSize).getSingleResult();
        }

        if (firstResult == null || firstResult < 0) {
            firstResult = 0;
        }
        if (maxResults == null || maxResults < 0) {
            maxResults = Integer.MAX_VALUE;
        }

        String sortByAttribute = "name";
        if (sortBy != null) {
            sortByAttribute = sortBy;
        }

        if (order == null) {
            order = SortOrder.asc;
        }

        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Institution> query = cb.createQuery(Institution.class);
        Root<Institution> root = query.from(Institution.class);
        query.select(root);

        if (!attributes.isEmpty()) {
            query.where(cb.and(getFilterQuery(root, cb, attributes)));
        }

        Expression<?> orderByExpr;

        // Allow to sort for multi value entries
        Field field;
        try {
            field = Institution.class.getDeclaredField(sortByAttribute);
        } catch (NoSuchFieldException e) {
            throw new BadRequestException("sort by field not found");
        }
        if (field.getType().isAssignableFrom(List.class)
                || field.getType().isAssignableFrom(Set.class)) {
            Join<Institution, ?> join = root.join(sortByAttribute);
            if (Objects.equals(sortByAttribute, Institution_.TAGS)) {
                Expression<String> path = join.get("name");
                orderByExpr = cb.least(path);
            } else {
                orderByExpr = cb.least(((JpaExpression) join).cast(String.class));
            }
        } else {
            orderByExpr = root.get(sortByAttribute);
        }

        query.groupBy(root.get(Institution_.ID));
        if (order == SortOrder.asc) {
            query.orderBy(cb.asc(orderByExpr));
        } else {
            query.orderBy(cb.desc(orderByExpr));
        }

        List<Institution> result = em.createQuery(query)
            .setFirstResult(firstResult)
            .setMaxResults(maxResults)
            .getResultList();
        if (size == 0) {
            size = result.size();
        }
        return new ObjectList<>(size, result);
    }

    /**
     * Get institution with the given id.
     * Response:
     * <pre>
     * <code>
     * {
     *   id: [String] Institution ID,
     *   name: [String] Institution Name,
     *   shortName: [String] Institution short name,
     *   category: [Integer] Institution category id,
     *   serviceBuildingStreet: [String] Institution service building street,
     *   serviceBuildingPostalCode: [String] Institution service building
     *      postal code,
     *   serviceBuildingLocation: [String] Institution service building
     *      location,
     *   addressStreet: [String] Institution address street,
     *   addressPostalCode: [String] Institution address postal code,
     *   addressLocation: [String] Institution address location,
     *   centralPhone: [String] Central phone
     *   centralFax: [String] Central fax
     *   centralMail: [String] Central mail,
     *   imisId: [String] Institution imis id,
     *   xCoordinate: [Float] X coordinate,
     *   yCoordinate: [Float] Y coordinate,
     *   active: [Boolean] True if institution is active
     * },
     * </code>
     * </pre>
     * @param id Institution id
     * @return Institution JSON or 404 if not found
     */
    @GET
    @Path("/{id}")
    public Institution getInstitutionById(@PathParam("id") Integer id) {
        EntityManager em = session.getProvider(
            JpaConnectionProvider.class).getEntityManager();
        Institution in = em.find(Institution.class, id);
        if (in == null) {
            throw new NotFoundException();
        }
        return in;
    }

    /**
     * Create a new institution.
     * Request:
     *
     * <pre>
     * <code>
     * {
     *   name: [String] Institution Name, mandatory
     *   shortName: [String] Institution short name, mandatory
     *   category: [Integer] Institution category id,
     *   serviceBuildingStreet: [String] Institution service building street,
     *      mandatory
     *   serviceBuildingPostalCode: [String] Institution service building
     *      postal code, mandatory
     *   serviceBuildingLocation: [String] Institution service building
     *      location, mandatory
     *   addressStreet: [String] Institution address street,
     *   addressPostalCode: [String] Institution address postal code,
     *   addressLocation: [String] Institution address location,
     *   centralPhone: [String] Central phone
     *   centralFax: [String] Central fax
     *   centralMail: [String] Central mail
     *   imisId: [String] Institution imis id,
     *   xCoordinate: [Float] X coordinate,
     *   yCoordinate: [Float] Y coordinate,
     *   active: [Boolean] True if institution is active
     * },
     * </code>
     * </pre>
     * @param rep Institution representation
     * @param headers Request headers

     * @return New Institution JSON or 400 if no institution is present
     */
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public Response createInstitution(final Institution rep,
            @Context HttpHeaders headers
    ) {
        auth.authorize(rep, RequestMethod.POST);

        List<Locale> languages = headers.getAcceptableLanguages();
        validator.validate(rep, languages.get(0), entityManager);

        EntityManager em = session.getProvider(
            JpaConnectionProvider.class).getEntityManager();

        NetworkResource networkResource = new NetworkResource(session);
        networkResource.mergeNetworks(rep.getNetwork());
        mergeTags(rep, em);
        em.persist(rep);
        return Response.ok(rep).build();
    }

    /**
     * Update an institution.
     * Request:
     *
     * <pre>
     * <code>
     * {
     *   id: [String] Institution ID,
     *   name: [String] Institution Name, mandatory
     *   shortName: [String] Institution short name, mandatory
     *   category: [Integer] Institution category id,
     *   serviceBuildingStreet: [String] Institution service building street,
     *      mandatory
     *   serviceBuildingPostalCode: [String] Institution service building
     *      postal code, mandatory
     *   serviceBuildingLocation: [String] Institution service building
     *      location, mandatory
     *   addressStreet: [String] Institution address street,
     *   addressPostalCode: [String] Institution address postal code,
     *   addressLocation: [String] Institution address location,
     *   centralPhone: [String] Central phone
     *   centralFax: [String] Central fax
     *   centralMail: [String] Central mail
     *   imisId: [String] Institution imis id,
     *   xCoordinate: [Float] X coordinate,
     *   yCoordinate: [Float] Y coordinate,
     *   active: [Boolean] True if institution is active
     * },
     * </code>
     * </pre>
     * @param rep Institution representation
     * @param headers Request headers
     * @return New Instituion JSON, 404 if institution is not found
     *         or 400 if no institution is present
     */
    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateInstitution(
            final Institution rep, @Context HttpHeaders headers
    ) {
        auth.authorize(rep, RequestMethod.PUT);

        List<Locale> languages = headers.getAcceptableLanguages();
        validator.validate(rep, languages.get(0), entityManager);

        EntityManager em = session.getProvider(
            JpaConnectionProvider.class).getEntityManager();
        if (rep == null || rep.getId() == null
               || em.find(Institution.class, rep.getId()) == null) {
            return Response.status(Status.BAD_REQUEST).build();
        }

        NetworkResource networkResource = new NetworkResource(session);
        networkResource.mergeNetworks(rep.getNetwork());
        mergeTags(rep, em);
        Institution merged = em.merge(rep);
        return Response.ok(merged).build();
    }

    /**
     * Get all institution tags.
     * @return List of institution tags
     */
    @GET
    @Path("/tag")
    public List<InstitutionTag> getInstitutionTags() {
        EntityManager em = session.getProvider(
            JpaConnectionProvider.class).getEntityManager();
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<InstitutionTag> query
            = cb.createQuery(InstitutionTag.class);
        Root<InstitutionTag> root = query.from(InstitutionTag.class);
        query.select(root);
        return em.createQuery(query).getResultList();
    }

    private void mergeTags(
        Institution rep,
        EntityManager em
    ) {
        UserModel requestingUser =
            session.getContext().getUserSession().getUser();
        for (InstitutionTag tag: rep.getInstitutionTags()) {
            InstitutionTag persistentTag =
                em.find(InstitutionTag.class, tag.getName());
            // Allow CHIEF_EDITORs to add new tags
            if (persistentTag == null) {
                if (IaMRole.CHIEF_EDITOR.isRoleOf(requestingUser, session)) {
                    em.persist(tag);
                } else {
                    throw new ForbiddenException();
                }
            }
        }
    }
}
