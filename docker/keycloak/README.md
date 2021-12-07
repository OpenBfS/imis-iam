# Keycloak

## Docker Setup

If a docker network has not already been created:

```bash
docker network create imis3
```

Build image:

```bash
docker build -t imis3/iam_keycloak .
```

Run container

```bash
docker run --name imis3_iam_keycloak -v $PWD:/opt/src --network imis3 -p 48080:8080 -d imis3/iam_keycloak
```

The Keycloak admin ui can then be reached under localhost:48080. An initial user is created during the setup:

* Username: admin
* Password: secret

These credentials can be used to log in into the web interface and further configure the application.

After starting the container the first time, the configure.sh script can be used to do the initial Keycloak configuration:

```bash
docker exec -ti imis3_iam_keycloak bash
cd /opt/src
./configure.sh
```

This will create a new realm for the imis3 iam application and the neccessary clients.
