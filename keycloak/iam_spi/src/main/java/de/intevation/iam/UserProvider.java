/* Copyright (C) 2022 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY!
 */
package de.intevation.iam;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
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
import org.keycloak.models.GroupModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.services.resource.RealmResourceProvider;

import de.intevation.iam.model.User;
import de.intevation.iam.model.UserPosition;
import de.intevation.iam.model.UserIamAttributes;
import de.intevation.iam.util.I18nUtils;

public class UserProvider implements RealmResourceProvider {

    private static final String SHIB_USER_HEADER = "X-SHIB-user";
    private static final String MAIL_ALREADY_USED_KEY
        = "error_mail_already_used";

    private KeycloakSession session;

    /**
     * Constructor.
     * @param session Session
     */
    public UserProvider(KeycloakSession session) {
        this.session = session;
    }

    /**
     * Gett all users.
     * @return List of user json objects
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUsers() {
        EntityManager em = session.getProvider(
            JpaConnectionProvider.class).getEntityManager();
        RealmModel realm = session.getContext().getRealm();
        Stream<UserModel> users = session.users().getUsersStream(realm);
        ArrayList<User> userList = new ArrayList<User>();
        users.forEach(user -> {
            userList.add(User.fromUserModel(user, em));
        });
        return Response.ok(userList).build();
    }

    /**
     * Get user by id.
     * @param id User id
     * @return User as json
     */
    @GET
    @Path("/{id}")
    public Response getUserById(@PathParam("id") String id) {
        EntityManager em = session.getProvider(
            JpaConnectionProvider.class).getEntityManager();
        RealmModel realm = session.getContext().getRealm();
        UserModel user = session.users().getUserById(realm, id);
        if (user == null) {
            return Response.status(Status.NOT_FOUND).build();
        }
        return Response.ok(User.fromUserModel(user, em)).build();
    }

    /**
     * Create a new user.
     * @param headers Request headers
     * @param rep User representation
     * @return New user json if successfull,
     *         400 if username is empty,
     *         409 if either username or email are already used
     */
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public Response createUser(@Context HttpHeaders headers, final User rep) {
        if (rep.getUsername() == null || rep.getUsername().isEmpty()) {
            return Response.status(Status.BAD_REQUEST).build();
        }
        EntityManager em = session.getProvider(
            JpaConnectionProvider.class).getEntityManager();
        RealmModel realm = session.getContext().getRealm();
        String id = headers.getHeaderString(SHIB_USER_HEADER);
        UserModel requestingUser = session.users().getUserById(realm, id);
        ResourceBundle i18n
            = I18nUtils.getI18nBundle(session, realm, requestingUser);


        if (isEmailAlreadyUsed(realm, rep)) {
            return Response.status(Status.CONFLICT)
                .type(MediaType.APPLICATION_JSON)
                .entity(i18n.getString(MAIL_ALREADY_USED_KEY))
                .build();
        }
        if (isUsernameAlreadyUsed(realm, rep)) {
            return Response.status(Status.CONFLICT)
                .type(MediaType.APPLICATION_JSON)
                .entity(i18n.getString("error_username_already_used"))
                .build();
        }

        UserModel newUser = session.users().addUser(realm, rep.getUsername());

        newUser.setFirstName(rep.getFirstName());
        newUser.setLastName(rep.getLastName());
        newUser.setEmail(rep.getEmail());

        //Update groups
        Stream<GroupModel> groupsStream = realm.getGroupsStream().filter(
            item -> {
                return rep.getGroups().contains(item.getId());
            });
        Map<String, GroupModel> groupMap = groupsStream.collect(
            Collectors.toMap(GroupModel::getId, Function.identity()));
        updateGroups(groupMap, newUser);
        UserIamAttributes attributes = rep.getAttributes();
        if (attributes.getId() == null) {
            attributes.setId(rep.getId());
        }
        em.persist(attributes);

        return Response.ok(User.fromUserModel(newUser, em)).build();
    }

    /**
     * Update the given user.
     * @param headers Request headers
     * @param rep User representation
     * @return Updated user json if succesfull,
     *         403 if not authorized,
     *         409 if the new email address is already used
     */
    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateUser(
        @Context HttpHeaders headers,
        final User rep
    ) {
        EntityManager em = session.getProvider(
            JpaConnectionProvider.class).getEntityManager();
        RealmModel realm = session.getContext().getRealm();
        UserModel user = session.users().getUserById(realm, rep.getId());
        String id = headers.getHeaderString(SHIB_USER_HEADER);
        UserModel requestingUser = session.users().getUserById(realm, id);

        try {
            user = updateUser(realm, rep, user);
        } catch (InvalidUserPropertiesException e) {
            ResourceBundle i18n
                = I18nUtils.getI18nBundle(session, realm, requestingUser);

            return Response.status(Status.CONFLICT)
                    .type(MediaType.APPLICATION_JSON)
                    .entity(i18n.getString(MAIL_ALREADY_USED_KEY))
                    .build();
        }

        UserIamAttributes attributes = rep.getAttributes();
        if (attributes.getId() == null) {
            attributes.setId(rep.getId());
        }
        em.persist(attributes);
        return Response.ok(User.fromUserModel(user, em)).build();
    }

    /**
     * Get all position entries.
     * @return Positions as json
     */
    @GET
    @Path("/position")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getPositions() {
        EntityManager em = session.getProvider(
            JpaConnectionProvider.class).getEntityManager();
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<UserPosition> critQuery
                = cb.createQuery(UserPosition.class);
        Root<UserPosition> root = critQuery.from(UserPosition.class);
        critQuery.select(root);
        TypedQuery<UserPosition> query = em.createQuery(critQuery);
        List<UserPosition> positions = query.getResultList();
        return Response.ok(positions).build();
    }

    /**
     * Update groups of the given user.
     * @param newGroups Map of new groups: Map<{id},{Group}>
     * @param user User to modifiy
     */
    private void updateGroups(
        Map<String,
        GroupModel> newGroups,
        UserModel user
    ) {
        //Join new groups
        newGroups.forEach((id, group) -> {
            if (!user.isMemberOf(group)) {
                user.joinGroup(group);
            }
        });
        //Leave groups if necessary
        user.getGroupsStream().forEach(group -> {
            if (!newGroups.containsKey(group.getId())) {
                user.leaveGroup(group);
            }
        });
    }

    /**
     * Update the given Keycloak usermodel with the data from the new one.
     * @param realm Realm
     * @param newUser User containing new data
     * @param oldUser Old keycloak user model
     * @return Updated user
     * @throws InvalidUserPropertiesException Thrown if new user contains
     * email address already in use
     */
    private UserModel updateUser(
        RealmModel realm,
        User newUser,
        UserModel oldUser
    ) throws InvalidUserPropertiesException {
        if (isEmailAlreadyUsed(realm, newUser)) {
            throw new InvalidUserPropertiesException("Email already in use");
        }
        //Update user
        oldUser.setFirstName(newUser.getFirstName());
        oldUser.setLastName(newUser.getLastName());
        oldUser.setEmail(newUser.getEmail());

        //Get new groups list and update
        Stream<GroupModel> groupsStream = realm.getGroupsStream().filter(
            item -> {
                return newUser.getGroups().contains(item.getId());
            });
        Map<String, GroupModel> groupMap = groupsStream.collect(
            Collectors.toMap(GroupModel::getId, Function.identity()));
        updateGroups(groupMap, oldUser);
        return oldUser;
    }

    /**
     * Check if the email address of the given user rep is already used.
     * @param realm Realm
     * @param user User representation
     * @return true if already used, else false
     */
    private boolean isEmailAlreadyUsed(RealmModel realm, User user) {
        //Search for user with the given email
        UserModel u = session.users().getUserByEmail(realm, user.getEmail());
        if (u == null) {
            return false;
        }
        //If user shall be created
        if (user.getId() == null || user.getId().isEmpty()) {
            return true;
        }
        //else check if the found user equals the user to be modified
        return !u.getId().equals(user.getId());
    }

    /**
     * Check if username is already used.
     * @param realm Realm
     * @param user User representation to be created
     * @return True if username is already used, else false
     */
    private boolean isUsernameAlreadyUsed(RealmModel realm, User user) {
        return session.users().getUserByUsername(realm, user.getUsername())
            != null;
    }
    private class InvalidUserPropertiesException extends Exception {
        InvalidUserPropertiesException(String message) {
            super(message);
        }
    }

    @Override
    public void close() { }

    @Override
    public Object getResource() {
        return this;
    }
}
