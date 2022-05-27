package de.intevation.iam;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.Stream.Builder;

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

import org.keycloak.models.GroupModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
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
        final User rep
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

        //Update user
        user.setFirstName(rep.getFirstName());
        user.setLastName(rep.getLastName());
        user.setEmail(rep.getEmail());

        UserProfileProvider profileProvider = session.getProvider(UserProfileProvider.class);
        UserProfile profile = profileProvider.create(UserProfileContext.USER_API, user);
        profile.update(false);
        return Response.ok(User.fromUserModel(user)).build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUsers() {

        RealmModel realm = session.getContext().getRealm();
        Stream<UserModel> users = session.users().getUsersStream(realm);
        ArrayList<User> userList = new ArrayList<User>();
        users.forEach(user -> {
            userList.add(User.fromUserModel(user));
        });
        return Response.ok(userList).build();
    }

    @GET
    @Path("/{id}")
    public Response getUserById(@PathParam("id") String id) {
        if (id == null || id.isEmpty()) {
            return Response.status(Status.BAD_REQUEST).build();
        }
        RealmModel realm = session.getContext().getRealm();
        UserModel user = session.users().getUserById(realm, id);
        if (user == null) {
            return Response.status(Status.NOT_FOUND).build();
        }
        return Response.ok(User.fromUserModel(user)).build();
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public Response createUser(final User rep) {
        if (rep.getUsername() == null || rep.getUsername().isEmpty()) {
            return Response.status(Status.BAD_REQUEST).build();
        }
        RealmModel realm = session.getContext().getRealm();
        UserModel newUser = session.users().addUser(realm, rep.getUsername());
        newUser.setFirstName(rep.getFirstName());
        newUser.setLastName(rep.getLastName());
        newUser.setEmail(rep.getEmail());

        //Update groups
        Stream<GroupModel> groupsStream = realm.getGroupsStream().filter(item -> {
            return rep.getGroups().contains(item.getName());
        });
        Map<String, GroupModel> groupMap = groupsStream.collect(Collectors.toMap(GroupModel::getName, Function.identity()));
        updateGroups(groupMap, newUser);

        return Response.ok(User.fromUserModel(newUser)).build();
    }

    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateUser(
        final User rep
    ) {
        RealmModel realm = session.getContext().getRealm();
        UserModel user = session.users().getUserById(realm, rep.getId());

        if (!user.getId().equals(rep.getId())) {
            return Response.status(Status.FORBIDDEN).build();
        }

        //Update user
        user.setFirstName(rep.getFirstName());
        user.setLastName(rep.getLastName());
        user.setEmail(rep.getEmail());

        UserProfileProvider profileProvider = session.getProvider(UserProfileProvider.class);
        UserProfile profile = profileProvider.create(UserProfileContext.USER_API, user);
        profile.update(false);

        //Get new groups list and update
        Stream<GroupModel> groupsStream = realm.getGroupsStream().filter(item -> {
            return rep.getGroups().contains(item.getName());
        });
        Map<String, GroupModel> groupMap = groupsStream.collect(Collectors.toMap(GroupModel::getName, Function.identity()));
        updateGroups(groupMap, user);

        return Response.ok(User.fromUserModel(user)).build();
    }

    private void updateGroups(Map<String, GroupModel> newGroups, UserModel user) {
        //Join new groups
        newGroups.forEach((name, group) -> {
            if (!user.isMemberOf(group)) {
                user.joinGroup(group);
            }
        });
        //Leave groups if necessary
        user.getGroupsStream().forEach(group -> {
            if (!newGroups.containsKey(group.getName())) {
                user.leaveGroup(group);
            }
        });
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
