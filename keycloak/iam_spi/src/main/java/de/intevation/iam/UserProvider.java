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

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
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
import org.keycloak.models.ClientModel;
import org.keycloak.models.GroupModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.RoleModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.jpa.entities.UserEntity;
import org.keycloak.services.resource.RealmResourceProvider;

import de.intevation.iam.auth.Authorizer;
import de.intevation.iam.auth.UserAuthorizer;
import de.intevation.iam.model.jpa.UserAttributes;
import de.intevation.iam.model.jpa.UserPosition;
import de.intevation.iam.model.representation.Role;
import de.intevation.iam.model.representation.User;
import de.intevation.iam.model.representation.UserMembership;
import de.intevation.iam.util.Constants;
import de.intevation.iam.util.DateUtils;
import de.intevation.iam.util.I18nUtils;
import de.intevation.iam.util.RequestMethod;

public class UserProvider implements RealmResourceProvider {

    private static final String MAIL_ALREADY_USED_KEY
        = "error_mail_already_used";

    private KeycloakSession session;

    private Authorizer<User> auth;

    /**
     * Constructor.
     * @param session Session
     */
    public UserProvider(KeycloakSession session) {
        this.session = session;
        this.auth = new UserAuthorizer(session);
    }

    /**
     * Get profile of the current users.
     * @param headers Request header
     * @return User profile as json, 403 if not authorized
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/profile")
    public Response getProfile(@Context HttpHeaders headers) {
        EntityManager em = session.getProvider(
            JpaConnectionProvider.class).getEntityManager();
        String id = headers.getHeaderString(Constants.SHIB_USER_HEADER);
        if (id == null) {
            return Response.status(Status.FORBIDDEN).build();
        }
        RealmModel realm = session.getContext().getRealm();
        UserModel user = session.users().getUserById(realm, id);
        return Response.ok(new User(user, em)).build();
    }

    /**
     * Get all users.
     * @param headers Request headers
     * @param search Optional search parameter
     * @return List of user json objects
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUsers(@Context HttpHeaders headers,
            @QueryParam("search") String search) {
        EntityManager em = session.getProvider(
            JpaConnectionProvider.class).getEntityManager();
        RealmModel realm = session.getContext().getRealm();
        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaQuery<UserEntity> query = builder.createQuery(
            UserEntity.class);
        Root<UserEntity> root = query.from(UserEntity.class);
        query.select(root);
        //Filter by realm
        Predicate filters = builder.equal(root.get("realmId"), realm.getId());
        //If search string is given: add to filters
        if (search != null && !search.isEmpty()) {
            Predicate searchFilters;
            String filter = search != null && !search.isEmpty()
                ? "%" + search.toLowerCase() + "%" : "";
            Predicate usernameFilter = builder.like(root.get("username"),
                filter);
            Predicate firstNameFilter = builder.like(root.get("firstName"),
                filter);
            Predicate lastNameFilter = builder.like(root.get("lastName"),
                filter);
            searchFilters = builder.or(usernameFilter, lastNameFilter);
            searchFilters = builder.or(searchFilters, firstNameFilter);
            filters = builder.and(filters, searchFilters);
        }
        query.where(filters);
        //Create User model list from UserEntity result
        List<User> userList = em.createQuery(query).getResultStream()
            .map(userEntity -> new User(
                session.users().getUserById(realm, userEntity.getId()),
                em))
            .collect(Collectors.toList());
        return Response.ok(auth.filter(userList, headers)).build();
    }

    /**
     * Get user by id.
     * @param id User id
     * @param headers Request headers
     * @return User as json
     */
    @GET
    @Path("/{id}")
    public Response getUserById(
        @PathParam("id") String id,
        @Context HttpHeaders headers
    ) {
        RealmModel realm = session.getContext().getRealm();
        UserModel userModel = session.users().getUserById(realm, id);
        if (userModel == null) {
            return Response.status(Status.NOT_FOUND).build();
        }

        EntityManager em = session.getProvider(
            JpaConnectionProvider.class).getEntityManager();
        User user = new User(userModel, em);
        if (!auth.isAuthorizedById(user, RequestMethod.GET, headers)) {
            return Response.status(Status.UNAUTHORIZED).build();
        }
        return Response.ok(user).build();
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
        if (!auth.isAuthorizedById(rep, RequestMethod.POST, headers)) {
            return Response.status(Status.UNAUTHORIZED).build();
        }

        String id = headers.getHeaderString(Constants.SHIB_USER_HEADER);
        RealmModel realm = session.getContext().getRealm();
        UserModel requestingUser = session.users().getUserById(realm, id);
        ResourceBundle i18n
            = I18nUtils.getI18nBundle(session, realm, requestingUser);
        EntityManager em = session.getProvider(
            JpaConnectionProvider.class).getEntityManager();

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

        //Add keycloak user
        UserModel newUserModel
                = session.users().addUser(realm, rep.getUsername());

        newUserModel.setFirstName(rep.getFirstName());
        newUserModel.setLastName(rep.getLastName());
        newUserModel.setEmail(rep.getEmail());
        newUserModel.setEnabled(rep.isEnabled());
        rep.setId(newUserModel.getId());

        //Create attributes
        UserAttributes attributes = rep.createOrUpdateJpaModel(em);

        //Update roles
        ClientModel client = realm.getClientByClientId(Constants.IAM_CLIENT_ID);
        Stream<RoleModel> roleStream = client.getRolesStream().filter(item -> {
            return rep.getRoles().contains(item.getName());
        });
        Map<String, RoleModel> roleMap = roleStream.collect(
            Collectors.toMap(RoleModel::getId, Function.identity()));
        updateRoles(roleMap, newUserModel, client);

        //Update groups
        Stream<GroupModel> groupsStream = realm.getGroupsStream().filter(
            item -> {
                return rep.getGroups().contains(item.getId());
            });
        Map<String, GroupModel> groupMap = groupsStream.collect(
            Collectors.toMap(GroupModel::getId, Function.identity()));
        updateGroups(groupMap, newUserModel);
        if (attributes.getId() == null) {
            attributes.setId(newUserModel.getId());
        }
        attributes.setExpiredNotificationSent(false);
        attributes.setInactivityNotificationSent(false);
        attributes.setExpiryDate(
                DateUtils.getAccountExpiryDate());
        em.persist(attributes);
        //Force flush and update to ensure attributes are persisted
        em.flush();
        em.refresh(attributes);
        return Response.ok(new User(newUserModel, em)).build();
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
        if (!auth.isAuthorizedById(rep, RequestMethod.PUT, headers)) {
            return Response.status(Status.UNAUTHORIZED).build();
        }
        EntityManager em = session.getProvider(
            JpaConnectionProvider.class).getEntityManager();
        RealmModel realm = session.getContext().getRealm();
        UserModel user = session.users().getUserById(realm, rep.getId());
        String id = headers.getHeaderString(Constants.SHIB_USER_HEADER);
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

        UserAttributes attributes = rep.createOrUpdateJpaModel(em);
        if (attributes.getId() == null) {
            attributes.setId(rep.getId());
        }
        //Set expiry date and notification flags
        UserAttributes dbAttributes
                = em.find(UserAttributes.class, attributes.getId());
        attributes.setExpiryDate(dbAttributes.getExpiryDate());
        attributes.setExpiredNotificationSent(
                dbAttributes.getExpiredNotificationSent());
        attributes.setInactivityNotificationSent(
                dbAttributes.getInactivityNotificationSent());
        em.merge(attributes);
        return Response.ok(new User(user, em)).build();
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
     * Get all memberships.
     * @return Memberships as Json Array
     */
    @GET
    @Path("/membership")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getMemberships() {
        RealmModel realm = session.getContext().getRealm();
        Stream<GroupModel> groups = realm.getGroupsStream();
        ArrayList<UserMembership> memberships = new ArrayList<UserMembership>();
        groups.forEach(group -> {
            memberships.add(new UserMembership(group));
        });
        return Response.ok(memberships.toArray()).build();
    }

    /**
     * Get all available iam roles.
     * @return Roles as JSON array
     */
    @GET
    @Path("/roles")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getRoles() {
        RealmModel realm = session.getContext().getRealm();
        ClientModel client = realm.getClientByClientId(Constants.IAM_CLIENT_ID);
        Stream<RoleModel> roles = client.getRolesStream();
        List<Role> roleNames = new ArrayList<>();
        roles.forEach(role -> {
            roleNames.add(new Role(role));
        });
        return Response.ok(roleNames).build();
    }

    /**
     * Update groups of the given user.
     * @param newGroups Map of new groups: Map<{id},{Group}>
     * @param user User to modifiy
     */
    private void updateGroups(
        Map<String, GroupModel> newGroups,
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

    private void updateRoles(
        Map<String, RoleModel> newRoles,
        UserModel user,
        ClientModel client
    ) {
        //Grant new roles
        newRoles.forEach((id, role) -> {
            if (!user.hasRole(role)) {
                user.grantRole(role);
            }
        });
        //Remove roles if necessary
        user.getClientRoleMappingsStream(client).forEach(role -> {
            if (!newRoles.containsKey(role.getId())) {
                user.deleteRoleMapping(role);
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
        oldUser.setEnabled(newUser.isEnabled());

        //Get new groups list and update
        Stream<GroupModel> groupsStream = realm.getGroupsStream().filter(
            item -> {
                return newUser.getGroups().contains(item.getId());
            });
        Map<String, GroupModel> groupMap = groupsStream.collect(
            Collectors.toMap(GroupModel::getId, Function.identity()));
        updateGroups(groupMap, oldUser);

        //Update roles
        ClientModel client = realm.getClientByClientId(Constants.IAM_CLIENT_ID);
        Stream<RoleModel> roleStream = client.getRolesStream().filter(item -> {
            return newUser.getRoles().contains(item.getName());
        });
        Map<String, RoleModel> roleMap = roleStream.collect(
            Collectors.toMap(RoleModel::getId, Function.identity()));
        updateRoles(roleMap, oldUser, client);

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
