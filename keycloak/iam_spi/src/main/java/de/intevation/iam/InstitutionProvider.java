/* Copyright (C) 2022 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY!
 */
package de.intevation.iam;

import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;
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
import de.intevation.iam.model.jpa.InstitutionCategory;
import de.intevation.iam.util.Constants;
import de.intevation.iam.util.I18nUtils;
import de.intevation.iam.util.Range;
import de.intevation.iam.util.RequestMethod;
import de.intevation.iam.validation.Validator;

/**
 * Class providing rest interfaces to create, get and delete Institutions.
 * @author Alexander Woestmann <awoestmann@intevation.de>
 */
@Produces(MediaType.APPLICATION_JSON)
public class InstitutionProvider implements RealmResourceProvider {

    enum SortOrder {
        asc,
        desc
    }

    private KeycloakSession session;

    private Authorizer<Institution> auth;

    private static final String NAME_ALREADY_USED_KEY
        = "error_name_already_used";
    private static final String SHORT_NAME_ALREADY_USED_KEY
        = "error_short_name_already_used";

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
     * @param range Optional range
     * @param sortBy Attribute to sort for
     * @param order Sort order: ascending and descending
     * @return Array of institutions
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<Institution> getInstitutions(@Context HttpHeaders headers,
            @QueryParam("search") String search,
            @QueryParam("range") Range range,
            @QueryParam("sortBy") String sortBy,
            @QueryParam("order") SortOrder order) {
        String filter = search != null && !search.isEmpty()
            ? "%" + search.toLowerCase() + "%" : "";
        EntityManager em = session.getProvider(
            JpaConnectionProvider.class).getEntityManager();
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Institution> query = cb.createQuery(Institution.class);
        Root<Institution> root = query.from(Institution.class);
        query.select(root);

        int firstResult = 0;
        int maxResults = Integer.MAX_VALUE;
        if (range != null) {
            firstResult = range.getLow();
            maxResults = range.getHigh() - range.getLow();
        }

        String sortByAttribute = "name";
        if (sortBy != null) {
            sortByAttribute = sortBy;
        }

        if (order == null) {
            order = SortOrder.asc;
        }

        if (!filter.isEmpty()) {
            query.where(cb.or(
                cb.like(
                    cb.lower(root.get("name")),
                    filter),
                cb.like(
                    cb.lower(root.get("imisId")),
                    filter)
                )
            );
        }
        if (order == SortOrder.asc) {
            query.orderBy(cb.asc(root.get(sortByAttribute)));
        } else {
            query.orderBy(cb.asc(root.get(sortByAttribute)));
        }

        TypedQuery<Institution> result = em.createQuery(query)
            .setFirstResult(firstResult)
            .setMaxResults(maxResults);
        return auth.filter(result.getResultList(), headers);
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
    public Response getInstitutionById(@PathParam("id") Integer id) {
        EntityManager em = session.getProvider(
            JpaConnectionProvider.class).getEntityManager();
        Institution in = em.find(Institution.class, id);
        if (in == null) {
            return Response.status(Status.NOT_FOUND).build();
        }
        return Response.ok(in).build();
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
     *   imisUsergroupId: [String] Institution usergroup id,
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
            @Context HttpHeaders headers) {
        List<Locale> languages = headers.getAcceptableLanguages();
        validator.validate(rep, languages.get(0));
        if (rep == null || rep.getImisUserGroupId() == null) {
            return Response.status(Status.BAD_REQUEST).build();
        }
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
        if (isNameAlreadyUsed(rep, em)) {
            return Response.status(Status.CONFLICT)
                .type(MediaType.APPLICATION_JSON)
                .entity(i18n.getString(NAME_ALREADY_USED_KEY))
                .build();
        }
        if (isShortNameAlreadyUsed(rep, em)) {
            return Response.status(Status.CONFLICT)
                .type(MediaType.APPLICATION_JSON)
                .entity(i18n.getString(SHORT_NAME_ALREADY_USED_KEY))
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
     *   imisUsergroupId: [String] Institution usergroup id,
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
            final Institution rep, @Context HttpHeaders headers) {
        List<Locale> languages = headers.getAcceptableLanguages();
        validator.validate(rep, languages.get(0));
        EntityManager em = session.getProvider(
            JpaConnectionProvider.class).getEntityManager();
        if (rep == null || rep.getId() == null
               || em.find(Institution.class, rep.getId()) == null
               || rep.getImisUserGroupId() == null) {
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
        if (isNameAlreadyUsed(rep, em)) {
            return Response.status(Status.CONFLICT)
                .type(MediaType.APPLICATION_JSON)
                .entity(i18n.getString(NAME_ALREADY_USED_KEY))
                .build();
        }
        if (isShortNameAlreadyUsed(rep, em)) {
            return Response.status(Status.CONFLICT)
                .type(MediaType.APPLICATION_JSON)
                .entity(i18n.getString(SHORT_NAME_ALREADY_USED_KEY))
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
     * Get all institution categories.
     * Response:
     * <pre>
     * <code>
     * [{
     *   id: [String] InstitutionCategory ID,
     *   name: [String] InstitutionCategory Name
     * },
     * {
     *   ...
     * }]
     * </code>
     * </pre>
     * @return Array of institution categories
     */
    @GET
    @Path("/category")
    public Response getInstitutionCategories() {
        EntityManager em = session.getProvider(
            JpaConnectionProvider.class).getEntityManager();
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<InstitutionCategory> query
            = cb.createQuery(InstitutionCategory.class);
        Root<InstitutionCategory> root = query.from(InstitutionCategory.class);
        query.select(root);
        List<InstitutionCategory> institutionCategories
            = em.createQuery(query).getResultList();
        return Response.ok(institutionCategories).build();
    }

    private boolean isShortNameAlreadyUsed(Institution inst, EntityManager em) {
        try {
            TypedQuery<Institution> query = em.createQuery(
                "SELECT i FROM Institution i WHERE i.shortName=:shortName",
                Institution.class)
                .setParameter("shortName", inst.getShortName());
            Institution found = query.getSingleResult();
            if (inst.getId() == null) {
                return true;
            }
            return !inst.getId().equals(found.getId());
        } catch (NoResultException nre) {
            return false;
        }
    }

    private boolean isNameAlreadyUsed(Institution inst, EntityManager em) {
        try {
        TypedQuery<Institution> query = em.createQuery(
            "SELECT i FROM Institution i WHERE i.name=:name",
            Institution.class)
            .setParameter("name", inst.getName());
            Institution found = query.getSingleResult();
            if (inst.getId() == null) {
                return true;
            }
            return !inst.getId().equals(found.getId());
        } catch (NoResultException nre) {
            return false;
        }
    }

    @Override
    public void close() { }

    @Override
    public Object getResource() {
        return this;
    }

}
