/* Copyright (C) 2022 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY!
 */
package de.intevation.iam.representation;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.CriteriaBuilder.In;

import org.keycloak.models.ClientModel;
import org.keycloak.models.GroupModel;
import org.keycloak.models.RealmModel;
import org.keycloak.models.RoleModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.jpa.entities.GroupEntity;
import org.keycloak.models.jpa.entities.RoleEntity;
import org.keycloak.models.jpa.entities.UserGroupMembershipEntity;
import org.keycloak.models.jpa.entities.UserRoleMappingEntity;

import de.intevation.iam.auth.Role;
import de.intevation.iam.model.InstitutionUser;
import de.intevation.iam.model.UserIamAttributes;
import de.intevation.iam.util.Constants;

/**
 * User representation
 */
public class User {

    //User entity attributes
    private String id;
    private String firstName;
    private String lastName;
    private String email;
    private String username;
    //IAM specific attributes
    private String title;
    private String phone;
    private String fax;
    private String oe;
    private String bfsLocation;
    private Integer position;

    private List<String> groups;
    private UserIamAttributes attributes;
    private List<Integer> institutions;
    private List<String> roles;
    private Boolean readonly;

    public Boolean getReadonly() {
        return readonly;
    }
    public void setReadonly(Boolean readonly) {
        this.readonly = readonly;
    }
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public String getFirstName() {
        return firstName;
    }
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }
    public String getLastName() {
        return lastName;
    }
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }
    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }
    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }
    public String getPhone() {
        return phone;
    }
    public void setPhone(String phone) {
        this.phone = phone;
    }
    public String getFax() {
        return fax;
    }
    public void setFax(String fax) {
        this.fax = fax;
    }
    public String getOe() {
        return oe;
    }
    public void setOe(String oe) {
        this.oe = oe;
    }
    public String getBfsLocation() {
        return bfsLocation;
    }
    public void setBfsLocation(String bfsLocation) {
        this.bfsLocation = bfsLocation;
    }
    public Integer getPosition() {
        return position;
    }
    public void setPosition(Integer position) {
        this.position = position;
    }
    public List<String> getGroups() {
        return groups;
    }
    public void setGroups(List<String> groups) {
        this.groups = groups;
    }
    public List<Integer> getInstitutions() {
        return institutions;
    }
    public void setInstitutions(List<Integer> institutions) {
        this.institutions = institutions;
    }
    public UserIamAttributes getAttributes() {
        return attributes;
    }
    public void setAttributes(UserIamAttributes attributes) {
        this.attributes = attributes;
    }
    public List<String> getRoles() {
        return roles;
    }
    public void setRoles(List<String> roles) {
        this.roles = roles;
    }

    /**
     * Create a user representation from the given jpa model.
     * @param jpaModel Model to use
     * @param em EntityManager
     * @return User representation
     */
    public static User fromJpaModel(UserIamAttributes jpaModel, EntityManager em) {
        User user = new User();
        user.setId(jpaModel.getId());
        user.setFirstName(jpaModel.getUserEntity().getFirstName());
        user.setLastName(jpaModel.getUserEntity().getLastName());
        user.setUsername(jpaModel.getUserEntity().getUsername());
        user.setEmail(jpaModel.getUserEntity().getEmail());

        user.setTitle(jpaModel.getTitle());
        user.setPhone(jpaModel.getPhone());
        user.setFax(jpaModel.getFax());
        user.setOe(jpaModel.getOe());
        user.setBfsLocation(jpaModel.getOe());
        user.setPosition(jpaModel.getPosition());


        CriteriaBuilder cb = em.getCriteriaBuilder();

        //Get user roles
        TypedQuery<UserRoleMappingEntity> roleMappingQuery =
            em.createNamedQuery("UserRoleMappingEntity.userRoleMappings",
            UserRoleMappingEntity.class);
        roleMappingQuery.setParameter("user", jpaModel.getUserEntity());
        List<UserRoleMappingEntity> roleMappings = roleMappingQuery.getResultList();
        //Query role entities as mapping models only contain role ids
        CriteriaQuery<RoleEntity> roleQuery = cb.createQuery(RoleEntity.class);
        Root<RoleEntity> root = roleQuery.from(RoleEntity.class);
        roleQuery.select(root);
        In<String> idFilter = cb.in(root.get("id"));
        roleMappings.forEach(roleMapping -> idFilter.value(roleMapping.getRoleId()));
        roleQuery.where(idFilter);
        List<RoleEntity> roleEntities = em.createQuery(roleQuery).getResultList();
        //Get role names if role is iam client role
        List<String> roleNames = new ArrayList<String>();
        roleEntities.forEach(entity -> {
            if (Role.get(entity.getName()) != null) {
                roleNames.add(entity.getName());
            }
        });
        user.setRoles(roleNames);

        //Get user groups
        TypedQuery<UserGroupMembershipEntity> groupMappingQuery =
            em.createNamedQuery("UserGroupMembershipEntity.userGroupMembership",
            UserGroupMembershipEntity.class);
        groupMappingQuery.setParameter("user", jpaModel.getUserEntity());
        List<UserGroupMembershipEntity> groupMappings = groupMappingQuery.getResultList();
        //Query group entities as mapping entities only contain group ids
        CriteriaQuery<GroupEntity> groupQuery = cb.createQuery(GroupEntity.class);
        Root<GroupEntity> groupRoot = groupQuery.from(GroupEntity.class);
        groupQuery.select(groupRoot);
        In<String> groupIdFilter = cb.in(root.get("id"));
        groupMappings.forEach(mapping -> groupIdFilter.value(mapping.getGroupId()));
        groupQuery.where(groupIdFilter);
        List<GroupEntity> groupEntities = em.createQuery(groupQuery).getResultList();
        List<String> groups = new ArrayList<String>();
        groupEntities.forEach(groupEnt -> groups.add(groupEnt.getName()));
        user.setGroups(groups);
        return user;
    }

    /**
     * Generate user from given usermodel.
     * @param userModel Keycloak usermodel
     * @param em EntityManager
     * @param realm Realm
     * @return Generated user
     */
    public static User fromUserModel(UserModel userModel,
            EntityManager em, RealmModel realm) {
        User user = new User();
        user.setId(userModel.getId());
        user.setUsername(userModel.getUsername());
        user.setFirstName(userModel.getFirstName());
        user.setLastName(userModel.getLastName());
        user.setEmail(userModel.getEmail());
        //Get user groups
        Stream<GroupModel> groups = userModel.getGroupsStream();
        List<String> groupIds = new ArrayList<String>();
        groups.forEach(group -> {
            groupIds.add(group.getId());
        });
        user.setGroups(groupIds);

        //Set iam specific attributes
        user.setAttributes(em.find(UserIamAttributes.class, userModel.getId()));

        //Get user iam roles
        ClientModel client = realm.getClientByClientId(Constants.IAM_CLIENT_ID);
        Stream<RoleModel> roles
                = userModel.getClientRoleMappingsStream(client);
        List<String> roleNames = new ArrayList<String>();
        roles.forEach(roleModel -> {
            roleNames.add(roleModel.getName());
        });
        user.setRoles(roleNames);

        //Get institutions the user has already joined
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<InstitutionUser> query
            = cb.createQuery(InstitutionUser.class);
        Root<InstitutionUser> root = query.from(InstitutionUser.class);
        query.select(root);
        Predicate userFilter = cb.equal(root.get("userId"), user.getId());
        query.where(userFilter);
        List<InstitutionUser> joinedInstitutions
            = em.createQuery(query).getResultList();
        List<Integer> institutionIds = new ArrayList<Integer>();
        joinedInstitutions.forEach(inst -> {
            institutionIds.add(inst.getInstitutionId());
        });
        user.setInstitutions(institutionIds);
        return user;
    }
}
