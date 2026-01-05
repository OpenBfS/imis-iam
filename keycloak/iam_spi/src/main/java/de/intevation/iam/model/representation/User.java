/* Copyright (C) 2022 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY!
 */
package de.intevation.iam.model.representation;

import static org.keycloak.userprofile.UserProfileContext.USER_API;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
import org.keycloak.userprofile.UserProfileContext;
import org.keycloak.userprofile.UserProfileProvider;

import de.intevation.iam.model.jpa.Institution;
import de.intevation.iam.model.jpa.Institution_;
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

    @NotBlank
    private String network;

    private boolean enabled;

    private boolean hiddenInAddressbook = false;

    private boolean retired = false;

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
        this(userModel, session, USER_API);
    }

    /**
     * Generate user from given usermodel.
     * @param userModel Keycloak usermodel
     * @param session KeycloakSession
     * @param priv true to include private profile attributes
     * @return Generated user
     */
    public User(
        UserModel userModel,
        KeycloakSession session,
        UserProfileContext ctx
    ) {
        this.id = userModel.getId();
        this.enabled = userModel.isEnabled();

        this.attributes = session.getProvider(UserProfileProvider.class)
            .create(ctx, userModel).getAttributes().getReadable();

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

            this.network = jpaModel.getNetwork();
            this.hiddenInAddressbook = jpaModel.getHiddenInAddressbook();
            this.retired = jpaModel.isRetired();
        }
    }

    public boolean isEnabled() {
        return enabled;
    }
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isHiddenInAddressbook() {
        return hiddenInAddressbook;
    }
    public void setHiddenInAddressbook(boolean hidden) {
        this.hiddenInAddressbook = hidden;
    }

    public boolean isRetired() {
        return retired;
    }
    public void setRetired(boolean retired) {
        this.retired = retired;
    }

    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
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

    public String getNetwork() {
        return network;
    }
    public void setNetwork(String network) {
        this.network = network;
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
            In<String> nameFilter = cb.in(root.get(Institution_.name));
            this.institutions.forEach(instName -> nameFilter.value(instName));
            query.where(nameFilter);
            List<Institution> insts = em.createQuery(query).getResultList();
            Set<Institution> newInstitutions = new HashSet<>();
            newInstitutions.addAll(insts);
            jpaUser.setInstitutions(newInstitutions);
        }
        jpaUser.setHiddenInAddressbook(hiddenInAddressbook);
        jpaUser.setRetired(retired);
        return jpaUser;
    }

    /**
     * Indicates whether an object is considered equal to this instance.
     *
     * {@code obj} is considered equal if it is this instance or has
     * the same ID.
     * @param obj the object to be compared with this instance
     * @return true if {@code obj} is considered equal to this instance
     */
    @Override
    public boolean equals(Object obj) {
        return obj == this
            || obj instanceof User other
            && other.getId() != null
            && other.getId().equals(this.id);
    }

    @Override
    public int hashCode() {
        return this.id == null
            ? super.hashCode()
            : Objects.hash(this.id);
    }
}
