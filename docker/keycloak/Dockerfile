FROM ubuntu:20.04

LABEL maintainer awoestmann@intevation.de

ENV KEYCLOAK_VERSION 16.1.0
ENV KEYCLOAK_HOME /opt/keycloak
ENV SRC_DIR /opt/src

RUN apt-get update && apt-get install -y \
            default-jre-headless jq curl libpostgresql-jdbc-java

RUN curl -sL https://github.com/keycloak/keycloak/releases/download/${KEYCLOAK_VERSION}/keycloak-${KEYCLOAK_VERSION}.tar.gz \
    | tar zx && mv keycloak-${KEYCLOAK_VERSION} ${KEYCLOAK_HOME}

WORKDIR ${KEYCLOAK_HOME}

ADD docker/keycloak ${SRC_DIR}

ENV POSTGRES_MODULE $KEYCLOAK_HOME/modules/system/layers/keycloak/org/postgresql/main
RUN mkdir -p $POSTGRES_MODULE
RUN ln -s /usr/share/java/postgresql.jar $POSTGRES_MODULE
RUN ln -s $SRC_DIR/postgresql-module.xml $POSTGRES_MODULE/module.xml

RUN ${SRC_DIR}/configure.sh

CMD sh $SRC_DIR/start.sh
