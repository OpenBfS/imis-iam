/* Copyright (C) 2022 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY!
 */
package de.intevation.iam;

import static org.keycloak.userprofile.UserProfileContext.USER_API;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import jakarta.persistence.EntityManager;
import jakarta.ws.rs.ClientErrorException;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotFoundException;
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
import org.keycloak.services.resource.RealmResourceProvider;
import org.keycloak.representations.userprofile.config.UPConfig;
import org.keycloak.userprofile.UserProfileProvider;
import org.keycloak.userprofile.ValidationException;

import de.intevation.iam.auth.Authorizer;
import de.intevation.iam.auth.UserAuthorizer;
import de.intevation.iam.model.jpa.UserAttributes;
import de.intevation.iam.model.representation.ObjectList;
import de.intevation.iam.model.representation.Role;
import de.intevation.iam.model.representation.User;
import de.intevation.iam.model.representation.UserMembership;
import de.intevation.iam.util.Constants;
import de.intevation.iam.util.DateUtils;
import de.intevation.iam.util.I18nUtils;
import de.intevation.iam.util.RequestMethod;
import de.intevation.iam.validation.Validator;
public class UserProvider implements RealmResourceProvider {

    private static final String MAIL_ALREADY_USED_KEY
        = "error_mail_already_used";

    private KeycloakSession session;

    private Authorizer<User> auth;

    private Validator validator;
    private UserProfileProvider userProfileProvider;

    enum SortOrder {
        asc,
        desc
    }

    /**
     * Constructor.
     * @param session Session
     */
    public UserProvider(KeycloakSession session) {
        this.session = session;
        this.auth = new UserAuthorizer(session);
        this.userProfileProvider =
            session.getProvider(UserProfileProvider.class);
        this.validator = new Validator();
    }

    /**
     * Get user profile metadata.
     * @return User profile metdata as json
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/userprofilemetadata")
    public UPConfig getUserProfileMetadata() {
        return this.userProfileProvider.getConfiguration();
    }

    /**
     * Get profile of the current users.
     * @param headers Request header
     * @return User profile as json, 403 if not authorized
     * @throws ForbiddenException if requesting user is not authorized to
     * view the requested data.
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/profile")
    public User getProfile(@Context HttpHeaders headers) {
        String id = headers.getHeaderString(Constants.SHIB_USER_HEADER);
        if (id == null) {
            throw new ForbiddenException();
        }
        RealmModel realm = session.getContext().getRealm();
        UserModel user = session.users().getUserById(realm, id);
        return new User(user, session);
    }

    /**
     * Get all users.
     * @param headers Request headers
     * @param search Optional search parameter
     * @param firstResult First result to return
     * @param maxResults Maximum numbers of results to return
     * @return List of user json objects
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public ObjectList<User> getUsers(@Context HttpHeaders headers,
            @QueryParam("search") String search,
            @QueryParam("firstResult") Integer firstResult,
            @QueryParam("maxResults") Integer maxResults) {
        RealmModel realm = session.getContext().getRealm();

        Map<String, String> attributes = new HashMap<>();
        if (search != null && !search.isEmpty()) {
            attributes.put(UserModel.SEARCH, search);
        }
        long size = 0;
        Stream<UserModel> userModels;
        if (firstResult != null || maxResults != null) {
            size = session.users()
                .searchForUserStream(realm, attributes).count();
            userModels = session.users()
                .searchForUserStream(
                    realm,
                    attributes,
                    firstResult,
                    maxResults);
        } else {
            userModels = session.users()
                .searchForUserStream(realm, attributes);
        }
        List<User> userList = userModels.map(userEntity -> new User(
                session.users()
                .getUserById(realm, userEntity.getId()), session))
                .collect(Collectors.toList());
        if (size == 0) {
            size = userList.size();
        }
        return auth.filterObjectList(
            new ObjectList<User>(size, userList),
            headers);
    }

    /**
     * Get user by id.
     * @param id User id
     * @param headers Request headers
     * @return User
     * @throws NotFoundException if a user with given ID does not exist
     * @throws ForbiddenException if requesting user is not authorized to
     * view the requested data.
     */
    @GET
    @Path("/{id}")
    public User getUserById(
        @PathParam("id") String id,
        @Context HttpHeaders headers
    ) {
        RealmModel realm = session.getContext().getRealm();
        UserModel userModel = session.users().getUserById(realm, id);
        if (userModel == null) {
            throw new NotFoundException();
        }

        User user = new User(userModel, session);
        if (!auth.isAuthorizedById(user, RequestMethod.GET, headers)) {
            throw new ForbiddenException();
        }
        return user;
    }

    /**
     * Create a new user.
     * @param headers Request headers
     * @param rep User representation
     * @return New user json if successfull,
     * @throws BadRequestException in case validation fails or username is empty
     * @throws ForbiddenException if requesting user is not authorized to
     * create the requested data.
     * @throws ClientErrorException if either username or email are already used
     */
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public User createUser(@Context HttpHeaders headers, final User rep) {
        List<Locale> languages = headers.getAcceptableLanguages();
        validator.validate(rep, languages.get(0));
        if (rep.getUsername() == null || rep.getUsername().isEmpty()) {
            throw new BadRequestException();
        }
        if (!auth.isAuthorizedById(rep, RequestMethod.POST, headers)) {
            throw new ForbiddenException();
        }

        String id = headers.getHeaderString(Constants.SHIB_USER_HEADER);
        RealmModel realm = session.getContext().getRealm();
        UserModel requestingUser = session.users().getUserById(realm, id);
        ResourceBundle i18n
            = I18nUtils.getI18nBundle(session, realm, requestingUser);
        EntityManager em = session.getProvider(
            JpaConnectionProvider.class).getEntityManager();

        if (isEmailAlreadyUsed(realm, rep)) {
            throw new ClientErrorException(Response.status(Status.CONFLICT)
                .type(MediaType.APPLICATION_JSON)
                .entity(i18n.getString(MAIL_ALREADY_USED_KEY))
                .build());
        }
        if (isUsernameAlreadyUsed(realm, rep)) {
            throw new ClientErrorException(Response.status(Status.CONFLICT)
                .type(MediaType.APPLICATION_JSON)
                .entity(i18n.getString("error_username_already_used"))
                .build());
        }

        //Add keycloak user
        UserModel newUserModel
                = session.users().addUser(realm, rep.getUsername());

        newUserModel.setEnabled(rep.isEnabled());
        rep.setId(newUserModel.getId());

        //Create attributes
        handleUserProfile(rep.getAttributes(), newUserModel);
        UserAttributes attributes = rep.createOrUpdateJpaModel(em);

        newUserModel.grantRole(realm.getClientByClientId(
                Constants.IAM_CLIENT_ID).getRole(rep.getRole()));

        //Update groups
        Stream<GroupModel> groupsStream = realm.getGroupsStream().filter(
            item -> {
                return rep.getGroups().contains(item.getName());
            });
        Map<String, GroupModel> groupMap = groupsStream.collect(
            Collectors.toMap(GroupModel::getName, Function.identity()));
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
        return new User(newUserModel, session);
    }

    /**
     * Update the given user.
     * @param headers Request headers
     * @param rep User representation
     * @return Updated user json if succesfull,
     *         403 if not authorized,
     *         409 if the new email address is already used
     * @throws BadRequestException in case validation fails
     * @throws ForbiddenException if requesting user is not authorized to
     * edit the requested data.
     * @throws ClientErrorException if trying to use an already used
     * e-mail adress
     */
    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    public User updateUser(
        @Context HttpHeaders headers,
        final User rep
    ) {
        List<Locale> languages = headers.getAcceptableLanguages();
        validator.validate(rep, languages.get(0));
        if (!auth.isAuthorizedById(rep, RequestMethod.PUT, headers)) {
            throw new ForbiddenException();
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

            throw new ClientErrorException(Response.status(Status.CONFLICT)
                .type(MediaType.APPLICATION_JSON)
                .entity(i18n.getString(MAIL_ALREADY_USED_KEY))
                .build());
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
        return new User(user, session);
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
     * @param newGroups Map of new groups: Map<{name},{Group}>
     * @param user User to modifiy
     */
    private void updateGroups(
        Map<String, GroupModel> newGroups,
        UserModel user
    ) {
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
        handleUserProfile(newUser.getAttributes(), oldUser);
        oldUser.setEnabled(newUser.isEnabled());

        //Get new groups list and update
        Stream<GroupModel> groupsStream = realm.getGroupsStream().filter(
            item -> {
                return newUser.getGroups().contains(item.getName());
            });
        Map<String, GroupModel> groupMap = groupsStream.collect(
            Collectors.toMap(GroupModel::getName, Function.identity()));
        updateGroups(groupMap, oldUser);

        //Update role
        ClientModel client = realm.getClientByClientId(Constants.IAM_CLIENT_ID);
        RoleModel oldRole = oldUser.getClientRoleMappingsStream(client)
            .findFirst().orElse(null);
        String newRole = newUser.getRole();
        if (oldRole == null || !oldRole.getName().equals(newRole)) {
            oldUser.deleteRoleMapping(oldRole);
            oldUser.grantRole(client.getRole(newRole));
        }

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
        UserModel u = session.users().getUserByEmail(
            realm, user.getAttributes().get("email").get(0));
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

    private void handleUserProfile(
        Map<String, List<String>> attributes,
        UserModel user
    ) {
        try {
            this.userProfileProvider
                .create(USER_API, attributes, user).update();
        } catch (ValidationException ve) {
            if (session.getTransactionManager().isActive()) {
                session.getTransactionManager().setRollbackOnly();
            }
            throw new BadRequestException(
                Response.status(Status.BAD_REQUEST)
                    .type(MediaType.APPLICATION_JSON)
                    .entity(ve.getErrors())
                    .build(),
                ve);
        }
    }

    @Override
    public void close() { }

    @Override
    public Object getResource() {
        return this;
    }
}
