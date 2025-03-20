# Docker Setup

Use docker-compose to build/start the IAM stack, e.g. with

```bash
cd docker
docker compose up
```

## Development Setup

To start a setup specifically for development, use the dedicated configuration:

```bash
docker compose -f docker-compose.dev.yml --env-file dev.env up -d
```

The default configuration of client application and Keycloak only allows
local access (`http://localhost:48080` for Keycloak admin UI,
`http://localhost:48081` for client application).
In case remote access to the application on the Docker host should be enabled,
change the respective environment variables, e.g. using:

```bash
HOSTNAME=docker-host docker compose -f docker-compose.dev.yml --env-file dev.env up -d
```

An initial admin user is created during the setup:

* Username: admin
* Password: secret

Additionally, other users are created within Keycloak, which can be used to access
the client application with the roles according to the usernames:

* Usernames: exampleuser, redakteur, chefredakteur
* Password: secret

Further, the users provided by the LDAP container can be used to login
after enabling the LDAP user federation via the admin UI.
See ldap/users.ldif for credentials (uid, userPassword).

E-Mails send by Keycloak (e.g. for password reset) can be viewed in the
web application "MailHog" at port `48082` on the docker host.


## Configuration

### Keycloak

Some aspects of the application can be configured via environment variables
in keycloak/keycloak.env.

The configuration assumes a TLS endpoint is operated in front of the
Compose stack, if used for production. The environment variable `PROTOCOLL`
has to be set to `https` in this case.


### Client application

To decide if the client should be started in development or production mode,
the environment variable `CLIENT_MODE` can be set. Per default, it's set to
`development` in `docker-compose.dev.yml`.
