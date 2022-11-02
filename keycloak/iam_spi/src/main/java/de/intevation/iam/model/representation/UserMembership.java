/* Copyright (C) 2022 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY!
 */
package de.intevation.iam.model.representation;

import org.keycloak.models.GroupModel;

/**
 * Class representing a user membership.
 */
public class UserMembership {

    private String id;

    private String name;

    /**
     * Create a membership instance from the given group model
     * @param group Group model to use
     */
    public UserMembership(GroupModel group) {
        this.id = group.getId();
        this.name = group.getName();
    }

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
}
