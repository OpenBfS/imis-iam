# IMIS3 IAM Keycloak

## Docker Setup

If a docker network has not already been created:

```bash
docker network create imis3
```

Build image:

```bash
cd /path/to/repo
docker build -t bfs/iam_keycloak -f docker/keycloak/Dockerfile .
```

Run container

```bash
docker run --name iam_keycloak -v $PWD/docker/keycloak:/opt/src --network imis3 -p 48080:8080 -d bfs/iam_keycloak
```

The Keycloak admin ui can then be reached under localhost:48080.
An initial user is created during the setup:

* Username: admin
* Password: secret

These credentials can be used to log in into the web interface
and further configure the application, but the configure.sh script
is already run during build to create a new realm for the imis3 IAM application
and the neccessary clients.
Additionally, another user is created which can be used to access
the client application:

* Username: exampleuser
* Password: secret
