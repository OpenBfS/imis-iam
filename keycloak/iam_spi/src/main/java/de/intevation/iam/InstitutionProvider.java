/* Copyright (C) 2022 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY!
 */
package de.intevation.iam;

import java.util.List;
import java.util.ResourceBundle;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.keycloak.connections.jpa.JpaConnectionProvider;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.services.resource.RealmResourceProvider;

import de.intevation.iam.auth.Authorization;
import de.intevation.iam.model.jpa.Institution;
import de.intevation.iam.model.jpa.InstitutionCategory;
import de.intevation.iam.util.Constants;
import de.intevation.iam.util.I18nUtils;
import de.intevation.iam.util.RequestMethod;

/**
 * Class providing rest interfaces to create, get and delete Institutions.
 * @author Alexander Woestmann <awoestmann@intevation.de>
 */
@Produces(MediaType.APPLICATION_JSON)
public class InstitutionProvider implements RealmResourceProvider {

    private KeycloakSession session;

    private Authorization auth;

    private static final String NAME_ALREADY_USED_KEY
        = "error_name_already_used";
    private static final String SHORT_NAME_ALREADY_USED_KEY
        = "error_short_name_already_used";
    /**
     * Constructor.
     * @param session Keycloak session
     */
    public InstitutionProvider(KeycloakSession session) {
        this.session = session;
        this.auth = new Authorization(session);
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
     * @return Array of institutions
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getInstitutions(@Context HttpHeaders headers) {
        EntityManager em = session.getProvider(
            JpaConnectionProvider.class).getEntityManager();
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Institution> query = cb.createQuery(Institution.class);
        Root<Institution> root = query.from(Institution.class);
        query.select(root);
        List<Institution> institutions = auth.filter(
                em.createQuery(query).getResultList(),
                headers, Institution.class);
        return Response.ok(institutions).build();
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
        if (rep == null) {
            return Response.status(Status.BAD_REQUEST).build();
        }
        if (!auth.isAuthorizedById(
                rep, RequestMethod.POST, headers, Institution.class)) {
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
        EntityManager em = session.getProvider(
            JpaConnectionProvider.class).getEntityManager();
        if (rep == null || rep.getId() == null
               || em.find(Institution.class, rep.getId()) == null) {
            return Response.status(Status.BAD_REQUEST).build();
        }
        if (!auth.isAuthorizedById(
                rep, RequestMethod.PUT, headers, Institution.class)) {
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
        if (!auth.isAuthorizedById(
            institution, RequestMethod.DELETE, headers, Institution.class)) {
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

    /**
     * Get institutionCategory with the given id.
     * Response:
     * <pre>
     * <code>
     * {
     *   id: [String] InstitutionCategory ID,
     *   name: [String] InstitutionCategory Name
     * }
     * </code>
     * </pre>
     * @param id InstitutionCategory id
     * @return InstitutionCategory JSON or 404 if not found
     */
    @GET
    @Path("/category/{id}")
    public Response getInstitutionCategoryById(@PathParam("id") Integer id) {
        EntityManager em = session.getProvider(
            JpaConnectionProvider.class).getEntityManager();
        InstitutionCategory in = em.find(InstitutionCategory.class, id);
        if (in == null) {
            return Response.status(Status.NOT_FOUND).build();
        }
        return Response.ok(in).build();
    }

    /**
     * Create a new category.
     * Response:
     * <pre>
     * <code>
     * {
     *   id: [String] InstitutionCategory ID,
     *   name: [String] InstitutionCategory Name
     * }
     * </code>
     * </pre>
     * @param intCategory Institution category representation
     * @param headers Request headers
     * @return InstitutionCategory JSON or 404 if not found
     */
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/category")
    public Response createInstitutionCategory(
            final InstitutionCategory intCategory,
            @Context HttpHeaders headers) {
        if (!auth.isAuthorizedById(
                intCategory, RequestMethod.POST,
                headers, InstitutionCategory.class)) {
            return Response.status(Status.UNAUTHORIZED).build();
        }
        if (intCategory == null) {
            return Response.status(Status.BAD_REQUEST).build();
        }
        EntityManager em = session.getProvider(
            JpaConnectionProvider.class).getEntityManager();
        em.persist(intCategory);
        return Response.ok(intCategory).build();
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
