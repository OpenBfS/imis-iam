/* Copyright (C) 2022 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY!
 */
package de.intevation.iam.model.jpa;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import de.intevation.iam.validation.constraints.MeasFacilOrNone;
import de.intevation.iam.validation.constraints.Unique;
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
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import org.hibernate.validator.constraints.UniqueElements;


@Entity
@Table(name = "iam_institution", schema = "keycloak")
@Unique(fields = {"name"}, clazz = Institution.class)
@Unique(fields = {"measFacilId"}, clazz = Institution.class)
@Unique(fields = {"measFacilName"}, clazz = Institution.class)
@MeasFacilOrNone
public class Institution {

    private static final String PHONE_PATTERN = "^\\+[1-9][0-9]{7,16}$";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotBlank
    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "meas_facil_name")
    private String measFacilName;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "iam_institution_tags",
        joinColumns = {@JoinColumn(name = "institution_id")},
        inverseJoinColumns = {@JoinColumn(name = "tag_name")}
    )
    private Set<InstitutionTag> tags = new HashSet<>();

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

    @Column(name = "service_building_state")
    private String serviceBuildingState;

    @Column(name = "address_street")
    private String addressStreet;

    @Column(name = "address_postal_code")
    private String addressPostalCode;

    @Column(name = "address_location")
    private String addressLocation;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "iam_institution_phone",
            joinColumns = @JoinColumn(name = "institution_id"))
    @Column(name = "phone")
    @UniqueElements
    private List<@Pattern(regexp = PHONE_PATTERN) String> phoneNumbers;

    @Column(name = "central_fax")
    @Pattern(regexp = PHONE_PATTERN)
    private String centralFax;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "iam_institution_mail",
            joinColumns = @JoinColumn(name = "institution_id"))
    @Column(name = "mail")
    @UniqueElements
    private List<@Email String> mailAddresses;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
        name = "iam_institution_operation_mode_change_phone_numbers",
        joinColumns = @JoinColumn(name = "institution_id"))
    @Column(name = "phone")
    @UniqueElements
    private List<@Pattern(regexp = PHONE_PATTERN) String> operationModeChangePhoneNumbers;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
        name = "iam_institution_operation_mode_change_sms_phone_numbers",
        joinColumns = @JoinColumn(name = "institution_id"))
    @Column(name = "phone")
    @UniqueElements
    private List<@Pattern(regexp = PHONE_PATTERN) String> operationModeChangeSmsPhoneNumbers;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
        name = "iam_institution_operation_mode_change_mail_addresses",
        joinColumns = @JoinColumn(name = "institution_id"))
    @Column(name = "mail")
    @UniqueElements
    private List<@Email String> operationModeChangeMailAddresses;

    @Size(min = 5, max = 7)
    @Column(name = "meas_facil_id")
    private String measFacilId;

    @Column(name = "x_coordinate")
    @JsonProperty("xCoordinate")
    private Float xCoordinate;

    @Column(name = "y_coordinate")
    @JsonProperty("yCoordinate")
    private Float yCoordinate;

    @Column(name = "active")
    private boolean active;

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

    public String getMeasFacilName() {
        return measFacilName;
    }

    public void setMeasFacilName(String measFacilName) {
        this.measFacilName = measFacilName;
    }

    /**
     * @return list of names of associated tags
     */
    public List<String> getTags() {
        ArrayList<String> names = new ArrayList<>();
        for (InstitutionTag tag : tags) {
            names.add(tag.getName());
        }
        return names;
    }

    /**
     * @param names list of names of tags to be associated
     */
    public void setTags(List<String> names) {
        for (String tag : names) {
            InstitutionTag institutionTag = new InstitutionTag();
            institutionTag.setName(tag);
            this.tags.add(institutionTag);
        }
    }

    @JsonIgnore
    public Set<InstitutionTag> getInstitutionTags() {
        return this.tags;
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

    public String getServiceBuildingState() {
        return serviceBuildingState;
    }

    public void setServiceBuildingState(String serviceBuildingState) {
        this.serviceBuildingState = serviceBuildingState;
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

    public List<String> getPhoneNumbers() {
        return phoneNumbers;
    }

    public void setPhoneNumbers(List<String> phones) {
        this.phoneNumbers = phones;
    }

    public String getCentralFax() {
        return centralFax;
    }

    public void setCentralFax(String centralFax) {
        this.centralFax = centralFax;
    }

    public List<String> getMailAddresses() {
        return mailAddresses;
    }

    public void setMailAddresses(List<String> mailAddresses) {
        this.mailAddresses = mailAddresses;
    }

    public void setOperationModeChangePhoneNumbers(List<String> operationModeChangePhoneNumbers) {
        this.operationModeChangePhoneNumbers = operationModeChangePhoneNumbers;
    }

    public void setOperationModeChangeSmsPhoneNumbers(List<String> operationModeChangeSmsPhoneNumbers) {
        this.operationModeChangeSmsPhoneNumbers = operationModeChangeSmsPhoneNumbers;
    }

    public void setOperationModeChangeMailAddresses(List<String> operationModeChangeMailAddresses) {
        this.operationModeChangeMailAddresses = operationModeChangeMailAddresses;
    }

    public List<String> getOperationModeChangePhoneNumbers() {
        return operationModeChangePhoneNumbers;
    }

    public List<String> getOperationModeChangeSmsPhoneNumbers() {
        return operationModeChangeSmsPhoneNumbers;
    }

    public List<String> getOperationModeChangeMailAddresses() {
        return operationModeChangeMailAddresses;
    }

    public String getMeasFacilId() {
        return measFacilId;
    }

    public void setMeasFacilId(String measFacilId) {
        this.measFacilId = measFacilId;
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

    public boolean getActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
