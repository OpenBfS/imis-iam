/* Copyright (C) 2022 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY!
 */
package de.intevation.iam.model.jpa;

import java.sql.Timestamp;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.keycloak.models.jpa.entities.UserEntity;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "user_iam_attributes", schema = "keycloak")
public class UserIamAttributes {
    @Id
    private String id;

    @Column(name = "title")
    private String title;

    @Column(name = "phone", nullable = false)
    private String phone;

    @Column(name = "mobile")
    private String mobile;

    @Column(name = "fax")
    private String fax;

    @Column(name = "oe")
    private String oe;

    @Column(name = "bfs_location")
    private String bfsLocation;

    @Column(name = "position_id", nullable = false)
    private Integer position;

    @Column(name = "expiry_date", nullable = false)
    @JsonIgnore
    private Timestamp expiryDate;

    @Column(name = "inactivity_notification_sent")
    @JsonIgnore
    private Boolean inactivityNotificationSent;

    @Column(name = "expired_notification_sent")
    @JsonIgnore
    private Boolean expiredNotificationSent;

    @OneToOne
    @JoinColumn(name = "id", referencedColumnName = "id")
    @JsonIgnore
    private UserEntity userEntity;

    @ManyToMany
    @JoinTable(
        name = "institution_user",
        joinColumns = {@JoinColumn(name = "user_id")},
        inverseJoinColumns = {@JoinColumn(name = "institution_id")}
    )
    private List<Institution> institutions;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getFax() {
        return fax;
    }

    public void setFax(String fax) {
        this.fax = fax;
    }

    public String getOe() {
        return oe;
    }

    public void setOe(String oe) {
        this.oe = oe;
    }

    public String getBfsLocation() {
        return bfsLocation;
    }

    public void setBfsLocation(String bfsLocation) {
        this.bfsLocation = bfsLocation;
    }

    public Integer getPosition() {
        return position;
    }

    public void setPosition(Integer position) {
        this.position = position;
    }

    public Timestamp getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(Timestamp expiryDate) {
        this.expiryDate = expiryDate;
    }

    public Boolean getInactivityNotificationSent() {
        return inactivityNotificationSent;
    }

    public void setInactivityNotificationSent(
            Boolean inactivityNotificationSent) {
        this.inactivityNotificationSent = inactivityNotificationSent;
    }

    public Boolean getExpiredNotificationSent() {
        return expiredNotificationSent;
    }

    public void setExpiredNotificationSent(Boolean expiredNotificationSent) {
        this.expiredNotificationSent = expiredNotificationSent;
    }

    public UserEntity getUserEntity() {
        return userEntity;
    }

    public void setUserEntity(UserEntity userEntity) {
        this.userEntity = userEntity;
    }

    public List<Institution> getInstitutions() {
        return institutions;
    }

    public void setInstitutions(List<Institution> institutions) {
        this.institutions = institutions;
    }
}
