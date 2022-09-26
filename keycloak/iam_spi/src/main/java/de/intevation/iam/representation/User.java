/* Copyright (C) 2022 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY!
 */
package de.intevation.iam.representation;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.CriteriaBuilder.In;

import org.keycloak.models.UserModel;
import org.keycloak.models.jpa.entities.GroupEntity;
import org.keycloak.models.jpa.entities.RoleEntity;
import org.keycloak.models.jpa.entities.UserGroupMembershipEntity;
import org.keycloak.models.jpa.entities.UserRoleMappingEntity;

import de.intevation.iam.auth.Role;
import de.intevation.iam.model.Institution;
import de.intevation.iam.model.UserIamAttributes;

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
    private String mobile;
    private String fax;
    private String oe;
    private String bfsLocation;
    private Integer position;

    private List<String> groups;
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
    public String getMobile() {
        return mobile;
    }
    public void setMobile(String mobile) {
        this.mobile = mobile;
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
    public List<String> getRoles() {
        return roles;
    }
    public void setRoles(List<String> roles) {
        this.roles = roles;
    }

    /**
     * Create a user attributes jpa model from this representation.
     * @return New model
     */
    public UserIamAttributes createJpaModel() {
        UserIamAttributes attributes = new UserIamAttributes();
        attributes.setId(getId());
        attributes.setTitle(getTitle());
        attributes.setPhone(getPhone());
        attributes.setMobile(getMobile());
        attributes.setFax(getFax());
        attributes.setOe(getOe());
        attributes.setBfsLocation(getBfsLocation());
        attributes.setPosition(getPosition());
        return attributes;
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
        user.setMobile(jpaModel.getMobile());
        user.setFax(jpaModel.getFax());
        user.setOe(jpaModel.getOe());
        user.setBfsLocation(jpaModel.getOe());
        user.setPosition(jpaModel.getPosition());

        List<Institution> institutions = jpaModel.getInstitutions();
        if (institutions != null) {
            List<Integer> institutionIds = new ArrayList<Integer>();
            institutions.forEach(inst -> institutionIds.add(inst.getId()));
            user.setInstitutions(institutionIds);
        }


        CriteriaBuilder cb = em.getCriteriaBuilder();

        //Get user roles
        TypedQuery<UserRoleMappingEntity> roleMappingQuery =
            em.createNamedQuery("userRoleMappings",
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
            em.createNamedQuery("userGroupMembership",
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
        groupEntities.forEach(groupEnt -> groups.add(groupEnt.getId()));
        user.setGroups(groups);

        return user;
    }

    /**
     * Generate user from given usermodel.
     * @param userModel Keycloak usermodel
     * @param em EntityManager
     * @return Generated user
     */
    public static User fromUserModel(UserModel userModel,
            EntityManager em) {
        UserIamAttributes userAttributes = em.find(
                UserIamAttributes.class, userModel.getId());
        return fromJpaModel(userAttributes, em);
    }
}
