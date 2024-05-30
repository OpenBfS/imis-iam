/* Copyright (C) 2022 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY!
 */
package de.intevation.iam.model.representation;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.CriteriaBuilder.In;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import org.keycloak.connections.jpa.JpaConnectionProvider;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.UserModel;

import com.fasterxml.jackson.annotation.JsonIgnore;

import de.intevation.iam.model.jpa.Institution;
import de.intevation.iam.model.jpa.UserAttributes;
import de.intevation.iam.util.Constants;


/**
 * User representation.
 *
 * This representation combines fields from the keycloak user entity,
 * iam attributes, groups and roles tables.
 */
public class User {

    private String id;

    private Map<String, List<String>> attributes;

    @NotEmpty
    private List<String> institutions;

    @NotBlank
    private String role;

    private Boolean readonly;
    private boolean enabled;

    private static final String USER_PARAM = "user";
    private static final String NAME_PARAM = "name";

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

        // Get user-role, which by convention can be only one
        setRole(userModel.getClientRoleMappingsStream(
                session.getContext().getRealm().getClientByClientId(
                    Constants.IAM_CLIENT_ID))
            .map(role -> role.getName()).findFirst().orElse(null));

        if (jpaModel != null) {
            //Set institutions
            Set<Institution> instList = jpaModel.getInstitutions();
            if (instList != null) {
                this.institutions = new ArrayList<String>();
                instList.forEach((inst) -> institutions.add(
                        inst.getName()));
            }
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

    public List<String> getInstitutions() {
        return institutions;
    }
    public void setInstitutions(List<String> institutions) {
        this.institutions = institutions;
    }

    public String getRole() {
        return role;
    }
    public void setRole(String role) {
        this.role = role;
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
        if (this.institutions != null && !this.institutions.isEmpty()) {
            CriteriaBuilder cb = em.getCriteriaBuilder();
            CriteriaQuery<Institution> query
                = cb.createQuery(Institution.class);
            Root<Institution> root = query.from(Institution.class);
            query.select(root);
            In<String> nameFilter = cb.in(root.get(NAME_PARAM));
            this.institutions.forEach(instName -> nameFilter.value(instName));
            query.where(nameFilter);
            List<Institution> insts = em.createQuery(query).getResultList();
            Set<Institution> newInstitutions = new HashSet<>();
            newInstitutions.addAll(insts);
            jpaUser.setInstitutions(newInstitutions);
        }
        return jpaUser;
    }
}
