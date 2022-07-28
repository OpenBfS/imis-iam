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

import org.keycloak.models.GroupModel;
import org.keycloak.models.UserModel;

public class User {

    private String id;
    private String firstName;
    private String lastName;
    private String email;
    private String username;
    private List<String> groups;
    private List<Integer> institutions;

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

    /**
     * Create a User from a Keycloak UserModel.
     * @param userModel Keycloak UserModel
     * @return User instance
     */
    public static User fromUserModel(UserModel userModel) {
        User user = new User();
        user.setId(userModel.getId());
        user.setUsername(userModel.getUsername());
        user.setFirstName(userModel.getFirstName());
        user.setLastName(userModel.getLastName());
        user.setEmail(userModel.getEmail());
        Stream<GroupModel> groups = userModel.getGroupsStream();
        List<String> groupNames = new ArrayList<String>();
        groups.forEach(group -> {
            groupNames.add(group.getName());
        });
        user.setGroups(groupNames);
        return user;
    }
}
