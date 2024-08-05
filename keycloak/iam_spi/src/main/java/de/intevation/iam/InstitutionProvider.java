/* Copyright (C) 2022 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY!
 */
package de.intevation.iam;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.util.*;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.ws.rs.DELETE;
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

import org.keycloak.connections.jpa.JpaConnectionProvider;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.services.resource.RealmResourceProvider;

import de.intevation.iam.auth.Authorizer;
import de.intevation.iam.auth.InstitutionAuthorizer;
import de.intevation.iam.model.jpa.Institution;
import de.intevation.iam.model.jpa.InstitutionTag;
import de.intevation.iam.model.representation.ObjectList;
import de.intevation.iam.util.Constants;
import de.intevation.iam.util.I18nUtils;
import de.intevation.iam.util.RequestMethod;
import de.intevation.iam.validation.Validator;
import org.keycloak.utils.SearchQueryUtils;

/**
 * Class providing rest interfaces to create, get and delete Institutions.
 * @author Alexander Woestmann <awoestmann@intevation.de>
 */
@Produces(MediaType.APPLICATION_JSON)
public class InstitutionProvider implements RealmResourceProvider {

    public enum SortOrder {
        asc,
        desc
    }

    private KeycloakSession session;

    private Authorizer<Institution> auth;

    private static final String NAME_ALREADY_USED_KEY
        = "error_name_already_used";
    private static final String MEAS_FACIL_NAME_ALREADY_USED
        = "error_meas_facil_name_already_used";

    private Validator validator;

    /**
     * Constructor.
     * @param session Keycloak session
     */
    public InstitutionProvider(KeycloakSession session) {
        this.session = session;
        this.auth = new InstitutionAuthorizer(session);
        this.validator = new Validator();
    }

    private Predicate getFilterQuery(Root<Institution> root, CriteriaBuilder cb, Map<String, String> attributes) {
        List<Predicate> predicates = new ArrayList<>();

        for (Map.Entry<String, String> entry : attributes.entrySet()) {
            entry.setValue("%" + entry.getValue() + "%");
        }

        String generalSearch = attributes.get("search");
        if (generalSearch != null) {
            predicates.add(cb.or(
                    cb.like(
                            cb.lower(root.get("name")),
                            generalSearch),
                    cb.like(
                            cb.lower(root.get("measFacilId")),
                            generalSearch)
            ));
        }

        for (Map.Entry<String, String> entry : attributes.entrySet()) {
            if (Objects.equals(entry.getKey(), "search")) {
                continue;
            }
            String value = "%" + entry.getValue() + "%";
            predicates.add(cb.like(
                    cb.lower(root.get(entry.getKey())),
                    value
            ));
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
     * @param headers Request headers
     * @param search Optional search parameter
     * @param firstResult First result to return
     * @param maxResults Maximum numbers of results to return
     * @param sortBy Attribute to sort for
     * @param order Sort order: ascending and descending
     * @return Array of institutions
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public ObjectList<Institution> getInstitutions(@Context HttpHeaders headers,
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

        if (order == SortOrder.asc) {
            query.orderBy(cb.asc(root.get(sortByAttribute)));
        } else {
            query.orderBy(cb.desc(root.get(sortByAttribute)));
        }

        TypedQuery<Institution> result = em.createQuery(query)
            .setFirstResult(firstResult)
            .setMaxResults(maxResults);
        if (size == 0) {
            size = result.getResultList().size();
        }
        return auth.filterObjectList(
                new ObjectList<>(size, result.getResultList()),
            headers);
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
     *   centralPhone: [String] Central phone, mandatory
     *   centralFax: [String] Central fax
     *   centralMail: [String] Central mail, mandatory
     *   imisId: [String] Institution imis id,
     *   xCoordinate: [Float] X coordinate,
     *   yCoordinate: [Float] Y coordinate,
     *   active: [Boolean] True if institution is active
     * },
     * </code>
     * </pre>
     * @param rep Institution representation
     * @param headers Request headers

     * @return New Instituion JSON or 400 if no institution is present
     */
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public Response createInstitution(final Institution rep,
            @Context HttpHeaders headers
    ) throws IntrospectionException, ReflectiveOperationException {
        List<Locale> languages = headers.getAcceptableLanguages();
        validator.validate(rep, languages.get(0));
        if (!auth.isAuthorizedById(rep, RequestMethod.POST, headers)) {
            return Response.status(Status.UNAUTHORIZED).build();
        }

        String id = headers.getHeaderString(Constants.SHIB_USER_HEADER);
        RealmModel realm = session.getContext().getRealm();
        EntityManager em = session.getProvider(
            JpaConnectionProvider.class).getEntityManager();
        UserModel requestingUser = session.users().getUserById(realm, id);
        ResourceBundle i18n
            = I18nUtils.getI18nBundle(session, realm, requestingUser);
        if (isAlreadyUsed("name", rep, em)) {
            return Response.status(Status.CONFLICT)
                .type(MediaType.APPLICATION_JSON)
                .entity(i18n.getString(NAME_ALREADY_USED_KEY))
                .build();
        }
        if (isAlreadyUsed("measFacilName", rep, em)) {
            return Response.status(Status.CONFLICT)
                .type(MediaType.APPLICATION_JSON)
                .entity(i18n.getString(MEAS_FACIL_NAME_ALREADY_USED))
                .build();
        }
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
     *   centralPhone: [String] Central phone, mandatory
     *   centralFax: [String] Central fax
     *   centralMail: [String] Central mail, mandatory
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
    ) throws IntrospectionException, ReflectiveOperationException {
        List<Locale> languages = headers.getAcceptableLanguages();
        validator.validate(rep, languages.get(0));
        EntityManager em = session.getProvider(
            JpaConnectionProvider.class).getEntityManager();
        if (rep == null || rep.getId() == null
               || em.find(Institution.class, rep.getId()) == null) {
            return Response.status(Status.BAD_REQUEST).build();
        }
        if (!auth.isAuthorizedById(rep, RequestMethod.PUT, headers)) {
            return Response.status(Status.UNAUTHORIZED).build();
        }
        String id = headers.getHeaderString(Constants.SHIB_USER_HEADER);
        RealmModel realm = session.getContext().getRealm();
        UserModel requestingUser = session.users().getUserById(realm, id);
        ResourceBundle i18n
            = I18nUtils.getI18nBundle(session, realm, requestingUser);
        if (isAlreadyUsed("name", rep, em)) {
            return Response.status(Status.CONFLICT)
                .type(MediaType.APPLICATION_JSON)
                .entity(i18n.getString(NAME_ALREADY_USED_KEY))
                .build();
        }
        if (isAlreadyUsed("measFacilName", rep, em)) {
            return Response.status(Status.CONFLICT)
                .type(MediaType.APPLICATION_JSON)
                .entity(i18n.getString(MEAS_FACIL_NAME_ALREADY_USED))
                .build();
        }

        Institution merged = em.merge(rep);
        return Response.ok(merged).build();
    }

    /**
     * Delete the institution with the given id.
     * @param id Institution id
     * @param headers Request headers
     * @return 200 if successful or 404 if not found
     */
    @DELETE
    @Path("/{id}")
    public Response removeInstitution(
            @PathParam("id") Integer id, @Context HttpHeaders headers) {
        EntityManager em = session.getProvider(
            JpaConnectionProvider.class).getEntityManager();
        Institution institution = em.find(Institution.class, id);
        if (institution == null) {
            return Response.status(Status.NOT_FOUND).build();
        }
        if (
            !auth.isAuthorizedById(institution, RequestMethod.DELETE, headers)
        ) {
            return Response.status(Status.UNAUTHORIZED).build();
        }
        em.remove(institution);
        return Response.ok().type(MediaType.APPLICATION_JSON).build();
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

    private boolean isAlreadyUsed(
        String attributeName, Institution inst, EntityManager em
    ) throws IntrospectionException, ReflectiveOperationException {
        TypedQuery<Boolean> query = em.createQuery(
            String.format(
                "SELECT EXISTS(SELECT 1 FROM Institution i "
                + "WHERE i.%s=:value "
                + (inst.getId() != null ? "AND i.id<>:id)" : ")"),
                attributeName),
            Boolean.class)
            .setParameter("value", new PropertyDescriptor(
                    attributeName, Institution.class
                ).getReadMethod().invoke(inst));
        if (inst.getId() != null) {
            query.setParameter("id", inst.getId());
        }
        return query.getSingleResult();
    }

    @Override
    public void close() { }

    @Override
    public Object getResource() {
        return this;
    }

}
