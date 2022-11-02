/* Copyright (C) 2022 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY!
 */
package de.intevation.iam.model.jpa;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.CriteriaBuilder.In;

import org.keycloak.models.jpa.entities.UserEntity;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "iam_mail_list", schema = "keycloak")
public class MailList {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "name", nullable = false)
    private String name;

    @Transient
    private List<String> users;

    @JsonIgnore
    @ManyToMany
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
