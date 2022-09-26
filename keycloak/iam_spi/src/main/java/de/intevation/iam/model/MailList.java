/* Copyright (C) 2022 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY!
 */
package de.intevation.iam.model;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.keycloak.models.jpa.entities.UserEntity;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "mail_list", schema = "keycloak")
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
        name = "mail_list_user",
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

    public List<String> getUsers() {
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
     * Update the users subscribed to this list.
     */
    public void updateUsers() {
        if (getUserEntities() == null) {
            return;
        }
        users = new ArrayList<String>();
        userEntities.forEach(userEntity -> users.add(userEntity.getId()));
    }
}
