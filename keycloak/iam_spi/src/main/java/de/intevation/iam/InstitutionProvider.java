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
