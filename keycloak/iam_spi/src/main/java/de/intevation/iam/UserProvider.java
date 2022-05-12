package de.intevation.iam;

import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.representations.account.UserRepresentation;
import org.keycloak.services.resource.RealmResourceProvider;
import org.keycloak.userprofile.UserProfile;
import org.keycloak.userprofile.UserProfileContext;
import org.keycloak.userprofile.UserProfileProvider;

import de.intevation.iam.model.User;

public class UserProvider implements RealmResourceProvider {

    private KeycloakSession session;

    public UserProvider(KeycloakSession session) {
        this.session = session;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/profile")
    public Response getProfile(@Context HttpHeaders headers) {
        String id = headers.getHeaderString("X-SHIB-user");
        if (id == null) {
            return Response.status(Status.FORBIDDEN).build();
        }
        RealmModel realm = session.getContext().getRealm();
        UserModel user = session.users().getUserById(realm, id);
        return Response.ok(User.fromUserModel(user)).build();
    }

    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/profile")
    public Response updateProfile(
        @Context HttpHeaders headers,
        final UserRepresentation rep
    ) {
        String id = headers.getHeaderString("X-SHIB-user");
        if (id == null) {
            return Response.status(Status.FORBIDDEN).build();
        }
        RealmModel realm = session.getContext().getRealm();
        UserModel user = session.users().getUserById(realm, id);

        if (!user.getId().equals(rep.getId())) {
            return Response.status(Status.FORBIDDEN).build();
        }
        UserProfileProvider profileProvider = session.getProvider(UserProfileProvider.class);
        UserProfile profile = profileProvider.create(UserProfileContext.USER_API, rep.toAttributes());
        profile.update(false);
        return Response.ok(User.fromUserModel(user)).build();
    }

    @Override
    public void close() {
        // TODO Auto-generated method stub
    }

    @Override
    public Object getResource() {
        return this;
    }
}
