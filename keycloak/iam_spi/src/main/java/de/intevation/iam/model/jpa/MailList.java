/* Copyright (C) 2022 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY!
 */
package de.intevation.iam.model.jpa;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.CriteriaBuilder.In;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import org.keycloak.models.jpa.entities.UserEntity;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "iam_mail_list", schema = "keycloak")
public class MailList {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotBlank
    @Column(name = "name", nullable = false)
    private String name;

    @NotEmpty
    @Transient
    private List<String> users;

    @JsonIgnore
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "iam_mail_list_user",
        joinColumns = {@JoinColumn(name = "mail_list_id")},
        inverseJoinColumns = {@JoinColumn(name = "user_id")}
    )
    private List<UserEntity> userEntities;

    public Integer getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * Get ids of users subscribed to this list.
     * @return Ids as list
     */
    public List<String> getUsers() {
        if (getUserEntities() == null) {
            return null;
        }
        users = new ArrayList<String>();
        userEntities.forEach(userEntity -> users.add(userEntity.getId()));
        return users;
    }

    public void setUsers(List<String> users) {
        this.users = users;
    }

    public List<UserEntity> getUserEntities() {
        return userEntities;
    }

    public void setUserEntities(List<UserEntity> userEntities) {
        this.userEntities = userEntities;
    }

    /**
     * Update userEntities using the current user ids list.
     * @param em Entity manager to use
     */
    public void updateUserEntities(EntityManager em) {
        if (this.users == null) {
            return;
        }

        //Get user entities
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<UserEntity> query = cb.createQuery(UserEntity.class);
        Root<UserEntity> root = query.from(UserEntity.class);
        query.select(root);
        In<String> idFilter = cb.in(root.get("id"));
        this.users.forEach(id -> idFilter.value(id));
        query.where(idFilter);
        List<UserEntity> result = em.createQuery(query).getResultList();

        setUserEntities(result);
    }
}
