package de.intevation.iam.model;

import java.util.List;
import java.util.Map;

import org.keycloak.models.GroupModel;

public class Institution {
    private String id;

    private String name;

    private Map<String, List<String>> attributes;

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

    public Map<String, List<String>> getAttributes() {
        return attributes;
    }

    public void setAttributes(Map<String, List<String>> attributes) {
        this.attributes = attributes;
    }

    public static Institution fromGroupModel(GroupModel group) {
        Institution institution = new Institution();
        institution.setId(group.getId());
        institution.setName(group.getName());
        institution.setAttributes(group.getAttributes());
        return institution;
    }
}
