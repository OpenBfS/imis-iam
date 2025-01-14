FROM ubuntu:24.04

LABEL maintainer=awoestmann@intevation.de

RUN apt-get update && apt-get install -y \
            openjdk-21-jre-headless maven jq curl \
            postgresql-client wait-for-it

ENV KEYCLOAK_VERSION=26.0.8
ENV KEYCLOAK_HOME=/opt/keycloak

RUN curl -sL https://github.com/keycloak/keycloak/releases/download/${KEYCLOAK_VERSION}/keycloak-${KEYCLOAK_VERSION}.tar.gz \
    | tar zx && mv keycloak-${KEYCLOAK_VERSION} ${KEYCLOAK_HOME}

ADD keycloak/iam_theme ${KEYCLOAK_HOME}/themes/iam_theme

# Deploy SPI
ENV SPI_SRC=/opt/iam_spi
WORKDIR ${SPI_SRC}
# Download dependencies before adding sources to leverage build cache
ADD keycloak/iam_spi/pom.xml ${SPI_SRC}/
RUN mvn -q dependency:go-offline
ADD keycloak/iam_spi ${SPI_SRC}
RUN mvn -q package
RUN cp target/iam-api-extension-jar-with-dependencies.jar $KEYCLOAK_HOME/providers/

WORKDIR ${KEYCLOAK_HOME}

ENV SRC_DIR=/opt/src
ADD docker/keycloak ${SRC_DIR}

RUN ln -fs $SRC_DIR/keycloak.conf $KEYCLOAK_HOME/conf/keycloak.conf

RUN ${KEYCLOAK_HOME}/bin/kc.sh build

ENV KC_BOOTSTRAP_ADMIN_USERNAME=admin
ENV KC_BOOTSTRAP_ADMIN_PASSWORD=secret
CMD sh $SRC_DIR/start.sh
