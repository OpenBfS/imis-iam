/* Copyright (C) 2022 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY!
 */
package de.intevation.iam.model.representation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.CriteriaBuilder.In;

import org.keycloak.connections.jpa.JpaConnectionProvider;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.UserModel;
import org.keycloak.models.jpa.entities.GroupEntity;
import org.keycloak.models.jpa.entities.RoleEntity;
import org.keycloak.models.jpa.entities.UserGroupMembershipEntity;
import org.keycloak.models.jpa.entities.UserRoleMappingEntity;

import com.fasterxml.jackson.annotation.JsonIgnore;

import de.intevation.iam.auth.Role;
import de.intevation.iam.model.jpa.Institution;
import de.intevation.iam.model.jpa.UserAttributes;

/**
 * User representation.
 *
 * This representation combines fields from the keycloak user entity,
 * iam attributes, groups and roles tables.
 */
public class User {

    private String id;

    private Map<String, List<String>> attributes;

    private List<String> groups;
    private List<Integer> institutions;
    private List<String> roles;
    private Boolean readonly;
    private boolean enabled;

    private static final String ID_PARAM = "id";
    private static final String USER_PARAM = "user";

    /**
     * Empty constructor used by JSON de-/serialization.
     */
    public User() { }

    /**
     * Generate user from given usermodel.
     * @param userModel Keycloak usermodel
     * @param session KeycloakSession
     * @return Generated user
     */
    public User(UserModel userModel, KeycloakSession session) {
        this.id = userModel.getId();
        this.enabled = userModel.isEnabled();

        // Contains custom attributes defined via User Profile config,
        // but only if actually a value is set.
        this.attributes = userModel.getAttributes();

        EntityManager em = session.getProvider(
            JpaConnectionProvider.class).getEntityManager();
        UserAttributes jpaModel = em.find(
            UserAttributes.class, userModel.getId());
        if (jpaModel != null) {
            //Set institutions
            List<Institution> instList = jpaModel.getInstitutions();
            if (instList != null) {
                this.institutions = new ArrayList<Integer>();
                instList.forEach(inst -> institutions.add(inst.getId()));
            }

            // Get user-role mappings
            TypedQuery<UserRoleMappingEntity> roleMappingQuery =
                em.createNamedQuery("userRoleMappings",
                    UserRoleMappingEntity.class);
            roleMappingQuery.setParameter(USER_PARAM, jpaModel.getUserEntity());
            List<UserRoleMappingEntity> roleMappings
                = roleMappingQuery.getResultList();

            //Query role entities as mapping models only contain role ids
            CriteriaBuilder cb = em.getCriteriaBuilder();
            CriteriaQuery<RoleEntity> roleQuery = cb.createQuery(
                RoleEntity.class);
            Root<RoleEntity> root = roleQuery.from(RoleEntity.class);
            roleQuery.select(root);
            In<String> idFilter = cb.in(root.get(ID_PARAM));
            roleMappings.forEach(
                roleMapping -> idFilter.value(roleMapping.getRoleId()));

            // Query only roles known in this application
            In<String> nameFilter = cb.in(root.get("name"));
            for (Role role: Role.values()) {
                nameFilter.value(role.toString());
            }

            roleQuery.where(idFilter, nameFilter);
            List<RoleEntity> roleEntities
                = em.createQuery(roleQuery).getResultList();
            //Get role names if role is iam client role
            List<String> roleNames = new ArrayList<String>();
            roleEntities.forEach(entity -> roleNames.add(entity.getName()));
            setRoles(roleNames);

            //Get user groups
            TypedQuery<UserGroupMembershipEntity> groupMappingQuery =
                em.createNamedQuery("userGroupMembership",
                    UserGroupMembershipEntity.class);
            groupMappingQuery.setParameter(
                USER_PARAM, jpaModel.getUserEntity());
            List<UserGroupMembershipEntity> groupMappings
                = groupMappingQuery.getResultList();
            List<String> idList = groupMappings
                .stream()
                .map(map -> map.getGroupId())
                .collect(Collectors.toList());
            Query query = em.createNativeQuery(
                "SELECT * FROM keycloak.KEYCLOAK_GROUP WHERE ID in (:ids)",
                GroupEntity.class);
            query.setParameter("ids", idList);
            List<GroupEntity> groupEntities = query.getResultList();
            this.groups = new ArrayList<String>();
            groupEntities.forEach(groupEnt -> groups.add(groupEnt.getId()));
        }
    }

    public Boolean getReadonly() {
        return readonly;
    }
    public void setReadonly(Boolean readonly) {
        this.readonly = readonly;
    }

    public boolean isEnabled() {
        return enabled;
    }
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }

    @JsonIgnore
    public String getUsername() {
        return attributes.get("username").get(0);
    }

    public Map<String, List<String>> getAttributes() {
        return attributes;
    }
    public void setAttributes(Map<String, List<String>> attributes) {
        this.attributes = attributes;
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
     * Get institutions by a list of ids.
     * @param institutionIds List of new institution ids
     * @param em Entity manager
     * @return List of institutions
     */
    private List<Institution> getInstitutionsByIds(
        List<Integer> institutionIds,
        EntityManager em
    ) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Institution> query
            = cb.createQuery(Institution.class);
        Root<Institution> root = query.from(Institution.class);
        query.select(root);
        In<Integer> idFilter = cb.in(root.get(ID_PARAM));
        institutionIds.forEach(instId -> idFilter.value(instId));
        query.where(idFilter);
        List<Institution> newInstitutions
            = em.createQuery(query).getResultList();
        return newInstitutions;
    }

    /**
     * Create or update the user attributes jpa model for this representation.
     * @param em Entity manager used for queries
     * @return Jpa model
     */
    public UserAttributes createOrUpdateJpaModel(EntityManager em) {
        UserAttributes jpaUser = em.find(UserAttributes.class, getId());
        if (jpaUser == null) {
            jpaUser = new UserAttributes();
        }
        jpaUser.setId(getId());
        jpaUser.setInstitutions(getInstitutionsByIds(getInstitutions(), em));
        return jpaUser;
    }
}
