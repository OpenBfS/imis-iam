/* Copyright (C) 2024 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY!
 */

package de.intevation.iam.model.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;

@Entity
@Table(name = "iam_institution_user")
public class InstitutionUser {
    @EmbeddedId
    private InstitutionUserId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("institutionId")
    @JoinColumn(name = "institution_id")
    private Institution institution;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId")
    @JoinColumn(name = "user_id")
    private UserAttributes user;

    @Column(name = "is_primary_institution")
    private boolean primaryInstitution = false;

    public InstitutionUser() { }

    public InstitutionUser(Institution institution, UserAttributes user) {
        this.institution = institution;
        this.user = user;
        this.id = new InstitutionUserId(institution.getId(), user.getId());
    }

    public Institution getInstitution() {
        return institution;
    }

    public void setInstitution(Institution institution) {
        this.institution = institution;
    }

    public UserAttributes getUser() {
        return user;
    }

    public void setUser(UserAttributes user) {
        this.user = user;
    }

    public boolean isPrimaryInstitution() {
        return primaryInstitution;
    }

    public void setPrimaryInstitution(boolean primaryInstitution) {
        this.primaryInstitution = primaryInstitution;
    }
}
