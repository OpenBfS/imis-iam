# Docker Setup

Use docker-compose to build/start the whole IAM stack at once, e.g. with

```bash
cd docker
docker-compose up
```

Note that the configuration of SP and IdP only allows local access. Thus,
if your browser does not run on your Docker host, use port forwarding to
make the applications available on your local machine, e.g. using
(assuming you have ssh access to remote-host and docker-host can be reached
from remote-host):

```bash
ssh -L 48081:docker-host:48081 -L 48080:docker-host:48080 remote-host
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


## Client application

Additionally, another user is created within Keycloak which can be used to access
the client application:

* Username: exampleuser
* Password: secret

Further, the users provided by the LDAP container can be used to login.
See ldap/users.ldif for credentials (uid, userPassword).

Authenticated access to the client is given under localhost:48081.
