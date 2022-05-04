package de.intevation.iam;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.keycloak.models.KeycloakSession;
import org.keycloak.models.UserModel;
import org.keycloak.representations.account.UserRepresentation;
import org.keycloak.services.managers.AppAuthManager;
import org.keycloak.services.managers.AuthenticationManager.AuthResult;
import org.keycloak.services.resource.RealmResourceProvider;
import org.keycloak.userprofile.UserProfile;
import org.keycloak.userprofile.UserProfileContext;
import org.keycloak.userprofile.UserProfileProvider;

import de.intevation.iam.model.User;

public class UserProvider implements RealmResourceProvider {

    private KeycloakSession session;
    private AuthResult auth;

    public UserProvider(KeycloakSession session) {
        this.session = session;
        this.auth = new AppAuthManager().authenticateIdentityCookie(session, session.getContext().getRealm());
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/profile")
    public Response getProfile() {
        if (this.auth == null) {
            return Response.status(Status.FORBIDDEN).build();
        }
        UserModel user = auth.getUser();
        return Response.ok(User.fromUserModel(user)).build();
    }

    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/profile")
    public Response updateProfile(
        final UserRepresentation rep
    ) {
        if (this.auth == null) {
            return Response.status(Status.FORBIDDEN).build();
        }
        UserModel user = auth.getUser();
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
