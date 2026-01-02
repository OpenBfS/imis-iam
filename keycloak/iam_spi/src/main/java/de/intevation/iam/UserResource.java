/* Copyright (C) 2022 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY!
 */
package de.intevation.iam;

import static org.keycloak.userprofile.UserProfileContext.ACCOUNT;
import static org.keycloak.userprofile.UserProfileContext.USER_API;
import static org.keycloak.validate.validators.OptionsValidator.KEY_OPTIONS;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import de.intevation.iam.util.SearchQueryUtils;
import de.intevation.iam.util.SortUtil;
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
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.RoleModel;
import org.keycloak.models.UserModel;
import org.keycloak.representations.userprofile.config.UPAttribute;
import org.keycloak.representations.userprofile.config.UPConfig;
import org.keycloak.userprofile.UserProfileContext;
import org.keycloak.userprofile.UserProfileProvider;
import org.keycloak.userprofile.ValidationException;

import de.intevation.iam.auth.Authorizer;
import de.intevation.iam.auth.UserAuthorizer;
import de.intevation.iam.model.jpa.UserAttributes;
import de.intevation.iam.model.representation.ObjectList;
import de.intevation.iam.model.representation.Role;
import de.intevation.iam.model.representation.User;
import de.intevation.iam.util.Constants;
import de.intevation.iam.util.DateUtils;
import de.intevation.iam.util.RequestMethod;
import de.intevation.iam.validation.Validator;


public class UserResource {

    private static final String PROFILE_PATH = "profile";

    public enum SortOrder {
        asc,
        desc
    }

    private KeycloakSession session;

    private Authorizer<User> auth;

    private Validator validator;
    private UserProfileProvider userProfileProvider;

    private EntityManager entityManager;

    /**
     * Constructor.
     * @param session Session
     */
    public UserResource(KeycloakSession session) {
        this.session = session;
        this.auth = new UserAuthorizer(session);
        this.userProfileProvider =
            session.getProvider(UserProfileProvider.class);
        this.validator = new Validator();
        this.entityManager = session.getProvider(JpaConnectionProvider.class)
            .getEntityManager();
    }

    /**
     * Get user profile metadata.
     * @return User profile metdata as JSON
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("userprofilemetadata")
    public UPConfig getUserProfileMetadata() {
        return this.userProfileProvider.getConfiguration();
    }

    /**
     * Get profile of the current user.
     * @return User profile as json, 403 if not authorized
     * @throws ForbiddenException if requesting user is not authorized to
     * view the requested data.
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path(PROFILE_PATH)
    public User getProfile() {
        return new User(
            session.getContext().getUserSession().getUser(),
            session,
            ACCOUNT);
    }

    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    @Path(PROFILE_PATH)
    public User updateProfile(
        @Context HttpHeaders headers,
        final User user
    ) {
        return doUpdateUser(headers, user, ACCOUNT);
    }

    /**
     * For each multiselect attribute (more than one value in the input map),
     * return complete search attributes for each value.
     */
    private static Map<String, List<Map<String, String>>> explodeAttrs(
        Map<String, List<String>> map
    ) {
        Map<String, String> scalarAttrs = new HashMap<>();
        map.forEach((k, v) -> {
                if (v.size() == 1) {
                    scalarAttrs.put(k, v.get(0));
                }
            });
        if (scalarAttrs.size() == map.size()) {
            // No multiselect attributes are searched for more than one value
            return Map.of("", List.of(scalarAttrs));
        }

        Map<String, List<Map<String, String>>> exploded = new HashMap<>();
        map.entrySet().stream()
            .filter(e -> e.getValue().size() > 1)
            .forEach(e -> e.getValue().forEach(v -> {
                        String k = e.getKey();
                        Map<String, String> attrs = new HashMap<>(scalarAttrs);
                        attrs.put(k, v);
                        if (exploded.containsKey(k)) {
                            exploded.get(k).add(attrs);
                        } else {
                            exploded.put(k, new ArrayList<>(List.of(attrs)));
                        }
                    }));
        return exploded;
    }

    /**
     * Get all users.
     * @param search Optional search parameter
     * @param firstResult First result to return
     * @param maxResults Maximum numbers of results to return
     * @param sortBy Attribute to sort for
     * @param order Sort order: ascending and descending
     * @return List of user json objects
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public ObjectList<User> getUsers(
            @QueryParam("search") String search,
            @QueryParam("firstResult") Integer firstResult,
            @QueryParam("maxResults") Integer maxResults,
            @QueryParam("sortBy") String sortBy,
            @QueryParam("order") SortOrder order) {
        RealmModel realm = session.getContext().getRealm();

        String sortByAttribute = "id";
        if (sortBy != null) {
            sortByAttribute = sortBy;
        }

        if (order == null) {
            order = SortOrder.asc;
        }

        final Set<String> selectListAttrs = this.userProfileProvider
            .getConfiguration().getAttributes().stream()
            .filter(a -> a.getValidations().containsKey(KEY_OPTIONS))
            .map(UPAttribute::getName)
            .collect(Collectors.toUnmodifiableSet());

        Map<String, List<String>> multiAttributes = new HashMap<>(search == null
                ? Collections.emptyMap()
                : SearchQueryUtils.getFields(search));

        /* Keycloak's searchForUserStream currently cannot search for multiple
           values of a multiselect attribute. Thus, repeat the search for
           each value of each multiselect attribute, union the results for
           the values of each attribute and build the intersection of the
           results for the multiselect attributes.
           The union of the results per multiselect attribute is required to
           match a requirement set by users of the application. */
        Map<String, List<Map<String, String>>> exploded
            = explodeAttrs(multiAttributes);
        boolean intersectionInitialized = false;
        Set<User> intersection = new HashSet<>();
        for (String multiAttr : exploded.keySet()) {
            Set<User> collectedUsers = new HashSet<>();
            for (Map<String, String> attributes : exploded.get(multiAttr)) {
                // Match sub-strings
                attributes.put(UserModel.EXACT, Boolean.FALSE.toString());

                String generalSearch = attributes.get("search");
                if (generalSearch != null) {
                    attributes.put(UserModel.SEARCH, generalSearch);
                    attributes.remove("search");
                }

                Map<String, String> customAttributes = new HashMap<>();
                for (String customAttribute : new String[]{
                        "institutions", "role", "network",
                        "hiddenInAddressbook"}
                ) {
                    String value = attributes.get(customAttribute);
                    if (value != null) {
                        customAttributes.put(customAttribute, value);
                        attributes.remove(customAttribute);
                    }
                }

                Stream<UserModel> userModels = session.users()
                    .searchForUserStream(realm, attributes);

                /* searchForUserStream lets us choose between exact and
                   sub-string search only for all attributes at once.
                   Since sub-string search is not appropriate for values
                   from select lists, apply further filtering: */
                Predicate<User> selectedAttrsFilter = u -> u
                    .getAttributes().entrySet().stream()
                    .filter(e -> selectListAttrs.contains(e.getKey())
                        && attributes.containsKey(e.getKey()))
                    .allMatch(e -> e.getValue().contains(
                            attributes.get(e.getKey())));

                Predicate<User> customAttributesFilter = u -> true;
                if (!customAttributes.isEmpty()) {
                    String institution = customAttributes.get(
                        "institutions");
                    String role = customAttributes.get("role");
                    String network = customAttributes.get("network");
                    String isHiddenString = customAttributes.get(
                        "hiddenInAddressbook");
                    Boolean isHidden = isHiddenString != null
                        ? Boolean.valueOf(isHiddenString) : null;
                    customAttributesFilter = u -> (institution == null
                        || u.getInstitutions().contains(institution))
                        && (role == null || u.getRole().equals(role))
                        && (network == null
                            || u.getNetwork().contains(network))
                        && (isHidden == null
                            || u.isHiddenInAddressbook() == isHidden);
                }

                List<User> matching = userModels
                    .map(userEntity -> new User(userEntity, session, USER_API))
                    .filter(selectedAttrsFilter)
                    .filter(customAttributesFilter)
                    .toList();
                // Filter users not authorized for GET
                matching = auth.filter(matching);

                // Union of matching users for current multiAttr
                collectedUsers.addAll(matching);
            }

            // Intersection of results for all multiselect attributes
            if (!intersectionInitialized) {
                intersection.addAll(collectedUsers);
                intersectionInitialized = true;
            } else {
                intersection.retainAll(collectedUsers);
            }
        }
        List<User> userList = new ArrayList<>(intersection);

        if (order != SortOrder.asc
                || !sortByAttribute.equals("id")) {
            SortUtil.sortByField(userList, sortByAttribute, order == SortOrder.asc);
        }

        long size = userList.size();
        if (firstResult != null || maxResults != null) {
            if (firstResult == null) {
                firstResult = 0;
            } else {
                firstResult = firstResult > userList.size()
                    ? userList.size()
                    : firstResult;
            }
            if (maxResults == null) {
                maxResults = userList.size();
            } else {
                maxResults = Math.min(firstResult + maxResults, userList.size());
            }
            userList = userList.subList(firstResult, maxResults);
        }

        return new ObjectList<User>(size, userList);
    }

    /**
     * Get user by id.
     * @param id User id
     * @return User
     * @throws NotFoundException if a user with given ID does not exist
     * @throws ForbiddenException if requesting user is not authorized to
     * view the requested data.
     */
    @GET
    @Path("{id}")
    public User getUserById(@PathParam("id") String id) {
        RealmModel realm = session.getContext().getRealm();
        UserModel userModel = session.users().getUserById(realm, id);
        if (userModel == null) {
            throw new NotFoundException();
        }

        User user = new User(userModel, session, USER_API);
        auth.authorize(user, RequestMethod.GET);
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
        auth.authorize(rep, RequestMethod.POST);

        List<Locale> languages = headers.getAcceptableLanguages();
        validator.validate(rep, languages.get(0), entityManager);

        RealmModel realm = session.getContext().getRealm();
        EntityManager em = session.getProvider(
            JpaConnectionProvider.class).getEntityManager();

        //Add keycloak user
        UserModel newUserModel;
        try {
            newUserModel = this.userProfileProvider
            .create(USER_API, rep.getAttributes()).create();
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

        // Retired users must be disabled
        if (rep.isRetired()) {
            rep.setEnabled(false);
        }
        newUserModel.setEnabled(rep.isEnabled());
        rep.setId(newUserModel.getId());

        //Create attributes
        UserAttributes attributes = rep.createOrUpdateJpaModel(em);

        newUserModel.grantRole(realm.getClientByClientId(
                Constants.IAM_CLIENT_ID).getRole(rep.getRole()));

        if (attributes.getId() == null) {
            attributes.setId(newUserModel.getId());
        }

        setNetwork(rep.getNetwork(), attributes);

        attributes.setExpiredNotificationSent(false);
        attributes.setInactivityNotificationSent(false);
        attributes.setExpiryDate(
                DateUtils.getAccountExpiryDate());
        em.persist(attributes);
        //Force flush and update to ensure attributes are persisted
        em.flush();
        em.refresh(attributes);
        return new User(newUserModel, session, USER_API);
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
        return doUpdateUser(headers, rep, USER_API);
    }

    private User doUpdateUser(
        HttpHeaders headers,
        final User rep,
        UserProfileContext ctx
    ) {
        auth.authorize(rep, RequestMethod.PUT);

        List<Locale> languages = headers.getAcceptableLanguages();
        validator.validate(rep, languages.get(0), entityManager);

        EntityManager em = session.getProvider(
            JpaConnectionProvider.class).getEntityManager();
        RealmModel realm = session.getContext().getRealm();
        UserModel user = session.users().getUserById(realm, rep.getId());

        //Update user
        try {
            this.userProfileProvider
                .create(ctx, rep.getAttributes(), user).update();
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
        // Retired users must be disabled
        if (rep.isRetired()) {
            rep.setEnabled(false);
        }
        user.setEnabled(rep.isEnabled());

        //Update role
        ClientModel client = realm.getClientByClientId(Constants.IAM_CLIENT_ID);
        RoleModel oldRole = user.getClientRoleMappingsStream(client)
            .findFirst().orElse(null);
        String newRole = rep.getRole();
        if (oldRole == null || !oldRole.getName().equals(newRole)) {
            user.deleteRoleMapping(oldRole);
            user.grantRole(client.getRole(newRole));
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

        setNetwork(rep.getNetwork(), attributes);

        em.merge(attributes);
        return new User(user, session, ctx);
    }

    /**
     * Get all available iam roles.
     * @return Roles as JSON array
     */
    @GET
    @Path("roles")
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

    private void setNetwork(String network, UserAttributes attributes) {
        NetworkResource networkResource = new NetworkResource(session);
        String persistentNetwork = networkResource.mergeNetworks(network);
        attributes.setNetwork(persistentNetwork);
    }
}
