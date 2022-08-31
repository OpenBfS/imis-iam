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
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

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

    @OneToMany(fetch = FetchType.EAGER, mappedBy = "mailListId")
    @JsonIgnore
    private List<MailListUser> mailListUsers;

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

    public List<MailListUser> getMailListUsers() {
        return mailListUsers;
    }

    public void setMailListUsers(List<MailListUser> mailListUsers) {
        this.mailListUsers = mailListUsers;
    }

    /**
     * Update the users subscribed to this list.
     */
    public void updateUsers() {
        if (getMailListUsers() == null) {
            return;
        }
        users = new ArrayList<String>();
        mailListUsers.forEach(mlu -> users.add(mlu.getUserId()));
    }

    /**
     * Update mailListUser list using the users id array.
     */
    public void updateMailListUsers() {
        if (getUsers() == null) {
            return;
        }
        //Delete removed users no longer subscribed
        mailListUsers.forEach(mlu -> {
            if (!users.contains(mlu.getUserId())) {
                mailListUsers.remove(mlu);
            }
        });
        //Add new subscriptions
        users.forEach(userId -> {
            if (mailListUsers.stream()
                    .filter(mlu -> userId.equals(mlu.getUserId()))
                    .findAny() == null) {
                MailListUser newEntry = new MailListUser();
                newEntry.setMailListId(getId());
                newEntry.setUserId(userId);
                mailListUsers.add(newEntry);
            }
        });
    }
}
