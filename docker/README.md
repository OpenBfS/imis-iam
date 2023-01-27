# Docker Setup

Use docker-compose to build/start the whole IAM stack at once, e.g. with

```bash
cd docker
docker compose up
```

The default configuration of SP and IdP only allows local access.
In case remote access to the application on the Docker host should be enabled,
change the respective environment variables, e.g. using:

```bash
KC_HOSTNAME=docker-host docker compose up -d
```


## Keycloak

The Keycloak admin UI can then be reached under http://localhost:48080/admin.
An initial user is created during the setup:

* Username: admin
* Password: secret

These credentials can be used to log in into the web interface
and further configure the application, but the necessary configuration
is already run during build respectively startup
to create a new realm for the IMIS3 IAM application
and the necessary clients.

Some aspects of the application can be configured via environment variables
in keycloak/.env.


## Client application

Additionally, another user is created within Keycloak which can be used to access
the client application:

* Username: exampleuser
* Password: secret

Further, the users provided by the LDAP container can be used to login.
See ldap/users.ldif for credentials (uid, userPassword).

Authenticated access to the client is given under localhost:48081.


## Testing E-Mail

E-Mails send by Keycloak (e.g. for password reset) can be viewed in the
web application "MailHog" at port 48082 on the docker host.
