package de.intevation.iam;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.keycloak.models.KeycloakSession;
import org.keycloak.models.UserModel;
import org.keycloak.services.managers.AppAuthManager;
import org.keycloak.services.managers.AuthenticationManager.AuthResult;
import org.keycloak.services.resource.RealmResourceProvider;

public class UserProvider implements RealmResourceProvider {

    private KeycloakSession session;
    private AuthResult auth;

    public UserProvider(KeycloakSession session) {
        this.session = session;
        this.auth = new AppAuthManager.BearerTokenAuthenticator(this.session).authenticate();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    // @Path("/profile")
    public Response getProfile() {
        UserModel user = auth.getUser();
        return Response.ok(userToJson(user)).build();
    }

    @Override
    public void close() {
        // TODO Auto-generated method stub
    }

    @Override
    public Object getResource() {
        return this;
    }

    private JsonObject userToJson(UserModel user) {
        JsonObjectBuilder builder = Json.createObjectBuilder();
        builder.add("id", user.getId());
        builder.add("firstname", user.getFirstName());
        builder.add("lastname", user.getLastName());
        builder.add("mail", user.getEmail());
        builder.add("username", user.getUsername());
        return builder.build();
    }
}
