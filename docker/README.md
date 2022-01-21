# Docker Setup

Use docker-compose to build/start the whole bfs-IAM stack at once, e.g. with

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

The Keycloak admin UI can then be reached under localhost:48080.
An initial user is created during the setup:

* Username: admin
* Password: secret

These credentials can be used to log in into the web interface
and further configure the application, but the configure.sh script
is already run during build to create a new realm for the imis3 IAM application
and the necessary clients.

The management console of the Wildfly application server into which Keycloak
is deployed can be reached under docker-host:49990.
The credentials are the same as for the Keycloak web interface.


## Client application

Additionally, another user is created within Keycloak which can be used to access
the client application:

* Username: exampleuser
* Password: secret

The client should listen to localhost:48081.
