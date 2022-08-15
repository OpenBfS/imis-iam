FROM ubuntu:20.04

LABEL maintainer awoestmann@intevation.de

ENV KEYCLOAK_VERSION 18.0.1
ENV KEYCLOAK_HOME /opt/keycloak
ENV SRC_DIR /opt/src
ENV SPI_SRC /opt/iam_spi
ENV KEYCLOAK_ADMIN admin
ENV KEYCLOAK_ADMIN_PASSWORD secret

RUN apt-get update && apt-get install -y \
            default-jre-headless maven jq curl \
            postgresql-client wait-for-it

RUN curl -sL https://github.com/keycloak/keycloak/releases/download/${KEYCLOAK_VERSION}/keycloak-${KEYCLOAK_VERSION}.tar.gz \
    | tar zx && mv keycloak-${KEYCLOAK_VERSION} ${KEYCLOAK_HOME}

ADD keycloak/iam_theme ${KEYCLOAK_HOME}/themes/iam_theme

# Deploy SPI
ADD keycloak/iam_spi ${SPI_SRC}
WORKDIR ${SPI_SRC}
RUN mvn package
RUN cp target/iam-api-extension-jar-with-dependencies.jar $KEYCLOAK_HOME/providers/

WORKDIR ${KEYCLOAK_HOME}

ADD docker/keycloak ${SRC_DIR}

RUN ln -fs $SRC_DIR/keycloak.conf $KEYCLOAK_HOME/conf/keycloak.conf

RUN ${KEYCLOAK_HOME}/bin/kc.sh build

CMD sh $SRC_DIR/start.sh
