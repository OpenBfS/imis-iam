/* Copyright (C) 2022 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY!
 */
package de.intevation.iam.model.jpa;

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
import com.fasterxml.jackson.annotation.JsonProperty;

@Entity
@Table(name = "iam_institution", schema = "keycloak")
public class Institution {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "short_name", nullable = false)
    private String shortName;

    @Column(name = "category_id")
    private Integer category;

    @Column(name = "service_building_street", nullable = false)
    private String serviceBuildingStreet;

    @Column(name = "service_building_postal_code", nullable = false)
    private String serviceBuildingPostalCode;

    @Column(name = "service_building_location", nullable = false)
    private String serviceBuildingLocation;

    @Column(name = "address_street")
    private String addressStreet;

    @Column(name = "address_postal_code")
    private String addressPostalCode;

    @Column(name = "address_location")
    private String addressLocation;

    @Column(name = "central_phone", nullable = false)
    private String centralPhone;

    @Column(name = "central_fax")
    private String centralFax;

    @Column(name = "central_mail", nullable = false)
    private String centralMail;

    @Column(name = "imis_id")
    private String imisId;

    @Column(name = "imis_mail")
    private String imisMail;

    @Column(name = "x_coordinate")
    @JsonProperty("xCoordinate")
    private Float xCoordinate;

    @Column(name = "y_coordinate")
    @JsonProperty("yCoordinate")
    private Float yCoordinate;

    @Column(name = "active")
    private Boolean active;

    @ManyToMany
    @JoinTable(
        name = "iam_institution_user",
        joinColumns = { @JoinColumn(name = "institution_id")},
        inverseJoinColumns = {@JoinColumn(name = "user_id")}
    )
    @JsonIgnore
    private List<UserEntity> userEntities;

    public List<UserEntity> getUserEntities() {
        return userEntities;
    }

    public void setUserEntities(List<UserEntity> userEntities) {
        this.userEntities = userEntities;
    }

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

    public Integer getCategory() {
        return category;
    }

    public void setCategory(Integer category) {
        this.category = category;
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

    public String getImisId() {
        return imisId;
    }

    public void setImisId(String imisId) {
        this.imisId = imisId;
    }

    public String getImisMail() {
        return imisMail;
    }

    public void setImisMail(String imisMail) {
        this.imisMail = imisMail;
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
