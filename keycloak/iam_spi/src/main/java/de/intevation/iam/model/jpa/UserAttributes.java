/* Copyright (C) 2022 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY!
 */
package de.intevation.iam.model.jpa;

import java.sql.Timestamp;
import java.util.Set;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import org.keycloak.models.jpa.entities.UserEntity;

/**
 * Persistence class holding user attributes not available in
 * org.keycloak.models.jpa.entities.UserEntity.
 * de.intevation.iam.model.representation.User provides a flat view on
 * attributes from both classes to be used for de-/serialization.
 */
@Entity
@Table(name = "iam_user_attributes", schema = "keycloak")
public class UserAttributes {
    @Id
    private String id;

    @Column(name = "expiry_date", nullable = false)
    private Timestamp expiryDate;

    @Column(name = "inactivity_notification_sent")
    private Boolean inactivityNotificationSent;

    @Column(name = "expired_notification_sent")
    private Boolean expiredNotificationSent;

    @OneToOne
    @JoinColumn(name = "id", referencedColumnName = "id")
    private UserEntity userEntity;

    @OneToMany(
        mappedBy = "user",
        cascade = CascadeType.ALL,
        orphanRemoval = true
    )
    private Set<InstitutionUser> institutions;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public Set<InstitutionUser> getInstitutions() {
        return institutions;
    }

    public void setInstitutions(Set<InstitutionUser> institutions) {
        this.institutions.clear();
        if (institutions != null) {
            this.institutions.addAll(institutions);
        }
    }
}
