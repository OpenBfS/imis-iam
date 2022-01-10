FROM ubuntu:20.04

LABEL maintainer awoestmann@intevation.de

ENV KEYCLOAK_VERSION 15.0.2
ENV KEYCLOAK_HOME /opt/keycloak
ENV SRC_DIR /opt/src

RUN apt-get update && apt-get install -y default-jre-headless jq curl

RUN curl -sL https://github.com/keycloak/keycloak/releases/download/${KEYCLOAK_VERSION}/keycloak-${KEYCLOAK_VERSION}.tar.gz \
    | tar zx && mv keycloak-${KEYCLOAK_VERSION} ${KEYCLOAK_HOME}

WORKDIR ${KEYCLOAK_HOME}

ADD docker/keycloak ${SRC_DIR}

RUN ${SRC_DIR}/configure.sh

CMD sh $SRC_DIR/start.sh
