/* Copyright (C) 2022 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY!
 */
package de.intevation.iam.model.jpa;


import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;


@Entity
@Table(name = "iam_institution", schema = "keycloak")
public class Institution {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotBlank
    @Column(name = "name", nullable = false)
    private String name;

    @NotBlank
    @Column(name = "short_name", nullable = false)
    private String shortName;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "iam_institution_categories",
        joinColumns = {@JoinColumn(name = "institution_id")},
        inverseJoinColumns = {@JoinColumn(name = "category_name")}
    )
    @NotEmpty
    private List<InstitutionCategory> categoryNames;

    @NotBlank
    @Column(name = "service_building_street", nullable = false)
    private String serviceBuildingStreet;

    @Pattern(regexp = "[0-9]*")
    @Size(min = 5, max = 5)
    @Column(name = "service_building_postal_code", nullable = false)
    private String serviceBuildingPostalCode;

    @NotBlank
    @Column(name = "service_building_location", nullable = false)
    private String serviceBuildingLocation;

    @Column(name = "address_street")
    private String addressStreet;

    @Column(name = "address_postal_code")
    private String addressPostalCode;

    @Column(name = "address_location")
    private String addressLocation;

    @Size(min = 7)
    @Column(name = "central_phone", nullable = false)
    private String centralPhone;

    @Column(name = "central_fax")
    private String centralFax;

    @NotNull
    @Column(name = "central_mail", nullable = false)
    private String centralMail;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "iam_institution_central_alarm_phone_numbers", joinColumns = @JoinColumn(name = "institution_id"))
    @Column(name = "phone")
    private List<String> centralAlarmPhoneNumbers;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "iam_institution_central_alarm_mail_addresses", joinColumns = @JoinColumn(name = "institution_id"))
    @Column(name = "mail")
    private List<String> centralAlarmMailAddresses;

    @Column(name = "imis_id")
    private String imisId;

    @Column(name = "imis_usergroup_id")
    private String imisUserGroupId;

    @Column(name = "x_coordinate")
    @JsonProperty("xCoordinate")
    private Float xCoordinate;

    @Column(name = "y_coordinate")
    @JsonProperty("yCoordinate")
    private Float yCoordinate;

    @Column(name = "active")
    private Boolean active;

    @Transient
    private Boolean readonly;

    public Boolean getReadonly() {
        return readonly;
    }

    public void setReadonly(Boolean readonly) {
        this.readonly = readonly;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public List<String> getCategoryNames() {
        ArrayList<String> categories = new ArrayList<>();
        for (InstitutionCategory category : categoryNames) {
            categories.add(category.getName());
        }
        return categories;
    }

    public void setCategoryNames(List<String> categoryNames) {
        ArrayList<InstitutionCategory> categories = new ArrayList<>();
        for (String category : categoryNames) {
            InstitutionCategory institutionCategory = new InstitutionCategory();
            institutionCategory.setName(category);
            categories.add(institutionCategory);
        }
        this.categoryNames = categories;
    }

    public String getServiceBuildingStreet() {
        return serviceBuildingStreet;
    }

    public void setServiceBuildingStreet(String serviceBuildingStreet) {
        this.serviceBuildingStreet = serviceBuildingStreet;
    }

    public String getServiceBuildingPostalCode() {
        return serviceBuildingPostalCode;
    }

    public void setServiceBuildingPostalCode(String serviceBuildingPostalCode) {
        this.serviceBuildingPostalCode = serviceBuildingPostalCode;
    }

    public String getServiceBuildingLocation() {
        return serviceBuildingLocation;
    }

    public void setServiceBuildingLocation(String serviceBuildingLocation) {
        this.serviceBuildingLocation = serviceBuildingLocation;
    }

    public String getAddressStreet() {
        return addressStreet;
    }

    public void setAddressStreet(String addressStreet) {
        this.addressStreet = addressStreet;
    }

    public String getAddressPostalCode() {
        return addressPostalCode;
    }

    public void setAddressPostalCode(String addressPostalCode) {
        this.addressPostalCode = addressPostalCode;
    }

    public String getAddressLocation() {
        return addressLocation;
    }

    public void setAddressLocation(String addressLocation) {
        this.addressLocation = addressLocation;
    }

    public String getCentralPhone() {
        return centralPhone;
    }

    public void setCentralPhone(String centralPhone) {
        this.centralPhone = centralPhone;
    }

    public String getCentralFax() {
        return centralFax;
    }

    public void setCentralFax(String centralFax) {
        this.centralFax = centralFax;
    }

    public String getCentralMail() {
        return centralMail;
    }

    public void setCentralMail(String centralMail) {
        this.centralMail = centralMail;
    }

    public void setCentralAlarmPhoneNumbers(List<String> centralAlarmPhoneNumbers) {
        this.centralAlarmPhoneNumbers = centralAlarmPhoneNumbers;
    }

    public void setCentralAlarmEmailAddresses(List<String> centralAlarmMailAddresses) {
        this.centralAlarmMailAddresses = centralAlarmMailAddresses;
    }

    public List<String> getCentralAlarmPhoneNumbers() {
        return centralAlarmPhoneNumbers;
    }

    public List<String> getCentralAlarmEmailAddresses() {
        return centralAlarmMailAddresses;
    }

    public String getImisId() {
        return imisId;
    }

    public void setImisId(String imisId) {
        this.imisId = imisId;
    }

    public String getImisUserGroupId() {
        return imisUserGroupId;
    }

    public void setImisUserGroupId(String imisUserGroupId) {
        this.imisUserGroupId = imisUserGroupId;
    }

    public Float getXCoordinate() {
        return xCoordinate;
    }

    public void setXCoordinate(Float xCoordinate) {
        this.xCoordinate = xCoordinate;
    }

    public Float getYCoordinate() {
        return yCoordinate;
    }

    public void setYCoordinate(Float yCoordinate) {
        this.yCoordinate = yCoordinate;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }
}
