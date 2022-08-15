/* Copyright (C) 2022 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY!
 */
package de.intevation.iam.model;

import org.keycloak.models.GroupModel;

public class UserMembership {
    private String id;
    private String name;
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Create a membership object from a given keycloak group model.
     * @param model Keycloak group model
     * @return New membership object
     */
    public static UserMembership fromGroupModel(GroupModel model) {
        UserMembership um = new UserMembership();
        um.setId(model.getId());
        um.setName(model.getName());
        return um;
    }
}
