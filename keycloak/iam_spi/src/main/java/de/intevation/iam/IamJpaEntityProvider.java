/* Copyright (C) 2022 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY!
 */
package de.intevation.iam;

import java.util.Collections;
import java.util.List;

import org.keycloak.connections.jpa.entityprovider.JpaEntityProvider;

import de.intevation.iam.model.Mail;
import de.intevation.iam.model.MailList;
import de.intevation.iam.model.MailListUser;
import de.intevation.iam.model.UserIamAttributes;
import de.intevation.iam.model.UserPosition;


public class IamJpaEntityProvider implements JpaEntityProvider{

    @Override
    public void close() {}

    @Override
    public String getChangelogLocation() {
        return "META-INF/db-changelog.xml";
    }

    @Override
    public List<Class<?>> getEntities() {
        List<Class<?>> entities =  Collections.<Class<?>>emptyList();
        entities.add(Mail.class);
        entities.add(MailList.class);
        entities.add(MailListUser.class);
        entities.add(UserIamAttributes.class);
        entities.add(UserPosition.class);
        return entities;
    }

    @Override
    public String getFactoryId() {
        return IamJpaEntityProviderFactory.ID;
    }

}
