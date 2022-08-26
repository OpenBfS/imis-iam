/* Copyright (C) 2022 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY!
 */
package de.intevation.iam.model;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.keycloak.models.ClientModel;
import org.keycloak.models.GroupModel;
import org.keycloak.models.RealmModel;
import org.keycloak.models.RoleModel;
import org.keycloak.models.UserModel;

import de.intevation.iam.util.Constants;

public class User {

    private String id;
    private String firstName;
    private String lastName;
    private String email;
    private String username;
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
