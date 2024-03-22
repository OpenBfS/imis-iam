/* Copyright (C) 2022 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY!
 */
package de.intevation.iam.model.jpa;

import java.sql.Timestamp;
import java.util.Comparator;
import java.util.SortedMap;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.MapKeyColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import org.hibernate.annotations.SortComparator;
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

    @ManyToMany
    @JoinTable(
        name = "iam_institution_user",
        joinColumns = {@JoinColumn(name = "user_id")},
        inverseJoinColumns = {@JoinColumn(name = "institution_id")}
    )
    @MapKeyColumn(name = "is_primary_institution")
    @SortComparator(CompareBooleanWithNulls.class)
    private SortedMap<Boolean, Institution> institutions;

    /**
     * Comparator to be used in a SortedMap that should allow any number
     * of null-valued keys.
     */
    public static class CompareBooleanWithNulls implements Comparator<Boolean> {
        @Override
        public int compare(Boolean b1, Boolean b2) {
            return b1 == null || b2 == null ? -1 : b1.compareTo(b2);
        }
    }

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

    public SortedMap<Boolean, Institution> getInstitutions() {
        return institutions;
    }

    public void setInstitutions(SortedMap<Boolean, Institution> institutions) {
        this.institutions = institutions;
    }
}
