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
import javax.persistence.criteria.Predicate;
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
import org.keycloak.models.ClientModel;
import org.keycloak.models.GroupModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.RoleModel;
import org.keycloak.models.UserModel;
import org.keycloak.services.resource.RealmResourceProvider;

import de.intevation.iam.auth.Authorization;
import de.intevation.iam.model.InstitutionUser;
import de.intevation.iam.model.User;
import de.intevation.iam.model.UserPosition;
import de.intevation.iam.model.UserIamAttributes;
import de.intevation.iam.model.UserMembership;
import de.intevation.iam.util.Constants;
import de.intevation.iam.util.I18nUtils;

public class UserProvider implements RealmResourceProvider {

    private static final String MAIL_ALREADY_USED_KEY
        = "error_mail_already_used";

    private KeycloakSession session;

    private Authorization auth;

    /**
     * Constructor.
     * @param session Session
     */
    public UserProvider(KeycloakSession session) {
        this.session = session;
        this.auth = new Authorization(session);
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
        return Response.ok(User.fromUserModel(user, em, realm)).build();
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
            userList.add(User.fromUserModel(user, em, realm));
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
        return Response.ok(User.fromUserModel(user, em, realm)).build();
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
        String id = headers.getHeaderString(Constants.SHIB_USER_HEADER);
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
        rep.setId(newUser.getId());

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
            attributes.setId(newUser.getId());
        }
        em.persist(attributes);
        updateInstitutions(rep.getInstitutions(), rep);
        return Response.ok(User.fromUserModel(newUser, em, realm)).build();
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

        UserIamAttributes attributes = rep.getAttributes();
        if (attributes.getId() == null) {
            attributes.setId(rep.getId());
        }
        em.merge(attributes);
        return Response.ok(User.fromUserModel(user, em, realm)).build();
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
            memberships.add(UserMembership.fromGroupModel(group));
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
        List<String> roleNames = new ArrayList<String>();
        roles.forEach(role -> {
            roleNames.add(role.getName());
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
     * Update groups of the given user.
     * @param newInstitutionIds List of new institution ids
     * @param user User to modifiy
     */
    private void updateInstitutions(
        List<Integer> newInstitutionIds,
        User user
    ) {
        EntityManager em = session.getProvider(
            JpaConnectionProvider.class).getEntityManager();
        CriteriaBuilder cb = em.getCriteriaBuilder();

        //Get institutions the user has already joined
        CriteriaQuery<InstitutionUser> query
            = cb.createQuery(InstitutionUser.class);
        Root<InstitutionUser> root = query.from(InstitutionUser.class);
        query.select(root);
        Predicate userFilter = cb.equal(root.get("userId"), user.getId());
        query.where(userFilter);
        List<InstitutionUser> joinedInstitutions
            = em.createQuery(query).getResultList();

        //Join new institutions
        newInstitutionIds.forEach(institutionId -> {
            if (joinedInstitutions.stream()
                .filter(inst -> institutionId.equals(inst.getInstitutionId()))
                .findAny()
                .orElse(null) == null
            ) {
                InstitutionUser iu = new InstitutionUser();
                iu.setInstitutionId(institutionId);
                iu.setUserId(user.getId());
                em.persist(iu);
            }
        });

        joinedInstitutions.forEach(institutionUser -> {
            if (!newInstitutionIds.contains(
                    institutionUser.getInstitutionId())) {
                em.remove(institutionUser);
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

        //Update roles
        ClientModel client = realm.getClientByClientId(Constants.IAM_CLIENT_ID);
        Stream<RoleModel> roleStream = client.getRolesStream().filter(item -> {
            return newUser.getRoles().contains(item.getName());
        });
        Map<String, RoleModel> roleMap = roleStream.collect(
            Collectors.toMap(RoleModel::getId, Function.identity()));
        updateRoles(roleMap, oldUser, client);

        updateInstitutions(newUser.getInstitutions(), newUser);
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
