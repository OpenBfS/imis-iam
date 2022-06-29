/* Copyright (C) 2022 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY!
 */
package de.intevation.iam;

import java.util.ArrayList;
import java.util.stream.Stream;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.keycloak.models.GroupModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.services.resource.RealmResourceProvider;

import de.intevation.iam.model.Institution;

/**
 * Class providing rest interfaces to create, get and delete Institutions
 * @author Alexander Woestmann <awoestmann@intevation.de>
 */
public class InstitutionProvider implements RealmResourceProvider {

    private KeycloakSession session;

    public InstitutionProvider(KeycloakSession session) {
        this.session = session;
    }

    /**
     * Get all institutions.
     * Response:
     * <pre>
     * <code>
     * [{
     *   id: [String] Institution ID,
     *   name: [String] Institution Name
     * },
     * {
     *   ...
     * }]
     * </code>
     * </pre>
     * @return Array of institutions
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getInstitutions() {
        RealmModel realm = session.getContext().getRealm();
        Stream<GroupModel> groups = realm.getGroupsStream();
        ArrayList<Institution> institutions = new ArrayList<Institution>();
        groups.forEach(group -> {
            institutions.add(Institution.fromGroupModel(group));
        });
        return Response.ok(institutions.toArray()).build();
    }

    /**
     * Get institution with the given id.
     * Response:
     * <pre>
     * <code>
     * {
     *   id: [String] Institution ID,
     *   name: [String] Institution Name
     * }
     * </code>
     * </pre>
     * @return Institution JSON or 404 if not found
     */
    @GET
    @Path("/{id}")
    public Response getInstitutionById(@PathParam("id") String id) {
        if (id == null || id.isEmpty()) {
            return Response.status(Status.BAD_REQUEST).build();
        }
        RealmModel realm = session.getContext().getRealm();
        GroupModel group = realm.getGroupById(id);
        if (group == null) {
            return Response.status(Status.NOT_FOUND).build();
        }
        return Response.ok(Institution.fromGroupModel(group)).build();
    }

    /**
     * Create a new institution.
     * @param rep Institution representation
     * @return New Instituion JSON or 400 if no institution is present
     */
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public Response createInstitution(final Institution rep) {
        if (rep == null) {
            return Response.status(Status.BAD_REQUEST).build();
        }
        RealmModel realm = session.getContext().getRealm();
        GroupModel group = realm.createGroup(rep.getName());
        return Response.ok(Institution.fromGroupModel(group)).build();
    }

    /**
     * Update an institution.
     * @param rep Institution representation
     * @return New Instituion JSON, 404 if institution is not found
     *         or 400 if no institution is present
     */
    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateInstitution(final Institution rep) {
        if (rep == null) {
            return Response.status(Status.BAD_REQUEST).build();
        }
        RealmModel realm = session.getContext().getRealm();
        GroupModel group = realm.getGroupById(rep.getId());
        if (group == null) {
            return Response.status(Status.NOT_FOUND).build();
        }
        group.setName(rep.getName());
        rep.getAttributes().forEach((attrName, attrValue) -> {
            group.setAttribute(attrName, attrValue);
        });
        return Response.ok(Institution.fromGroupModel(group)).build();
    }

    /**
     * Delete the institution with the given id.
     * </pre>
     * @param id Institution id
     * @return 200 if successful or 404 if not found
     */
    @DELETE
    @Path("/{id}")
    public Response removeInstitution(@PathParam("id") String id) {
        if (id == null || id.isEmpty()) {
            return Response.status(Status.BAD_REQUEST).build();
        }
        RealmModel realm = session.getContext().getRealm();
        GroupModel group = realm.getGroupById(id);
        if (group == null) {
            return Response.status(Status.NOT_FOUND).build();
        }
        realm.removeGroup(group);
        return Response.ok().build();
    }

    @Override
    public void close() {}

    @Override
    public Object getResource() {
        return this;
    }

}
