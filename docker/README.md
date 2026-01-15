# Docker Setup

Use docker-compose to build and start the IAM stack for production, e.g. with

```shell
cd docker
docker compose up
```

## Development Setup

### Configuration

In development environments, there is no need to set any parameters.
The file `dev.env` already contains suitable default values.
In case divergent values are required, make a copy and change the values in the copied file.
In the `docker compose` commands, then refer to the customized file.

### Build and Start

To start a setup specifically for development, use the dedicated configuration:

```shell
docker compose -f docker-compose.dev.yml --env-file dev.env up -d
```

The default configuration of client application and Keycloak only allows
local access (`http://localhost:48080` for Keycloak admin UI,
`http://localhost:48081` for client application).
In case remote access to the application on the Docker host should be enabled,
change the respective environment variables, e.g. using:

```shell
HOSTNAME=docker-host docker compose -f docker-compose.dev.yml --env-file dev.env up -d
```

### Usage

An initial admin user is created during the setup:

* Username: admin
* Password: secret

Additionally, other users are created within Keycloak, which can be used to access
the client application with the roles according to the usernames:

* Usernames: exampleuser, redakteur, chefredakteur
* Password: secret

Further, the users provided by the LDAP container can be used to login
after enabling the LDAP user federation via the admin UI.
See `ldap/users.ldif` for credentials (uid, userPassword).

E-Mails sent by Keycloak (e.g. for password reset) can be viewed in the
web application "MailHog" at port `48082` on the docker host.

### Database

To enter the database with psql:
```shell
docker compose -f docker-compose.yml -f docker-compose.dev.yml exec db psql -h localhost keycloak keycloak
```

## Configuration

Environment variables are configured in:
- `docker/dev.env` - Development configuration
- `docker/shared.env` - Shared settings
- `keycloak/keycloak.env` - Keycloak settings

Important configuration options:
- `CLIENT_PATH` - Base path for frontend routing
- `SESSION_INACTIVITY_TIMEOUT` - Session timeout duration
- `REPORT_MAIL` - Support email for bug reports
- Keycloak-specific settings for database, HTTP etc

For production builds, copy `dev.env` to `.env` and adapt the options to your needs.
Furthermore, you may want to copy `../keycloak/iam_theme` to a new folder and change greeters and notification mails.
Set `IAM_THEME_SRC` to point to this new folder (the path is relative to the top-level directory of this repository).

Environment variables shared between containers are defined in `shared.env`.

The configuration assumes a TLS endpoint is operated in front of the
Compose stack, if used for production. The environment variable `PROTOCOLL`
has to be set to `https` in this case.

### Keycloak

Some aspects of the application can be configured via environment variables
defined in `keycloak/keycloak.env`. In case further environment variables
need to be defined to configure other aspects of Keycloak (e.g. to set
`KC_HTTP_MAX_QUEUED_REQUESTS` in productive setup), these can be set
in an additional file `keycloak/optional.env`.


### Client application

To decide if the client should be started in development or production mode,
the environment variable `CLIENT_MODE` can be set. Per default, it's set to
`development` in `docker-compose.dev.yml`.

Further configuration options can be found in `docker-compose.yml` and
`../client/.env`.


## Bootstrapping the application

The application depends on the existence of a Keycloak realm with a specific
OIDC-client and roles. As a starting point, `keycloak/data/realm.json` can be
imported after application start using e.g.

```shell
docker compose exec keycloak /opt/src/import_realm.sh
```

`realm.json` also contains example configuration for SMTP server and
LDAP user federation as well as example user data for development setups.
It should be considered removing these from the file before building the image
or adapting the configuration via the Keycloak admin console after import.

A development setup, as documented above, imports the realm automatically.

## License

GPL version 3.0 or later.

See [LICENSES](../../LICENSES/) directory for complete license text.
