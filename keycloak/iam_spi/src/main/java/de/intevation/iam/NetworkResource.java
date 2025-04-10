/* Copyright (C) 2025 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY!
 */
package de.intevation.iam;

import java.util.List;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.keycloak.connections.jpa.JpaConnectionProvider;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.UserModel;

import de.intevation.iam.auth.IaMRole;
import de.intevation.iam.model.jpa.Network;
import de.intevation.iam.model.jpa.Network_;

/**
 * Class providing rest interfaces for handling networks.
 */
@Produces(MediaType.APPLICATION_JSON)
public class NetworkResource {

  private KeycloakSession session;

  private EntityManager em;

  /**
   * Constructor.
   * @param session Keycloak session
   */
  public NetworkResource(KeycloakSession session) {
      this.session = session;
      this.em = session.getProvider(JpaConnectionProvider.class)
          .getEntityManager();
  }

  /**
   * Get all networks.
   * @return networks as JSON array
   */
  @GET
  @Path("/")
  public List<Network> getNetworks() {
    CriteriaBuilder cb = em.getCriteriaBuilder();
    CriteriaQuery<Network> query
        = cb.createQuery(Network.class);
    Root<Network> root = query.from(Network.class);
    query.select(root).orderBy(cb.asc(root.get(Network_.name)));
    return em.createQuery(query).getResultList();
  }

  /**
   * Persist a new network into the database.
   * @throws ForbiddenException if requesting user is not authorized to
   * create new networks
   * @return the persisted network
   */
  String mergeNetworks(String networkName) {
        UserModel requestingUserModel =
            session.getContext().getUserSession().getUser();
        Network persistentNetwork =
            em.find(Network.class, networkName);

        // Allow CHIEF_EDITORs to add new networks
        if (persistentNetwork == null) {
            if (IaMRole.CHIEF_EDITOR.isRoleOf(requestingUserModel, session)) {
                Network network = new Network();
                network.setName(networkName);
                em.persist(network);
                return network.getName();
            } else {
                throw new ForbiddenException(Response.status(
                    Response.Status.FORBIDDEN.getStatusCode(),
                    "Not allowed to create new network"
                ).build());
            }
        }

        return persistentNetwork.getName();
    }
}
