FROM ubuntu:24.04

LABEL maintainer=awoestmann@intevation.de

# Use UTF-8
RUN echo \
    "locales locales/locales_to_be_generated multiselect en_US.UTF-8 UTF-8" | \
    debconf-set-selections && \
    echo "locales locales/default_environment_locale select en_US.UTF-8" | \
    debconf-set-selections
RUN apt-get update -y && apt-get install -y locales
ENV LC_ALL=en_US.UTF-8

RUN apt-get update && apt-get install -y \
            libxml2-utils openjdk-21-jre-headless maven jq curl \
            postgresql-client wait-for-it

ENV KEYCLOAK_HOME=/opt/keycloak

ENV SPI_SRC=/opt/iam_spi
WORKDIR ${SPI_SRC}
ADD keycloak/iam_spi/pom.xml ${SPI_SRC}/

RUN KEYCLOAK_VERSION=$(xmllint --xpath "//*[local-name()='keycloak.version']/text()" $SPI_SRC/pom.xml); \
    curl -sL https://github.com/keycloak/keycloak/releases/download/${KEYCLOAK_VERSION}/keycloak-${KEYCLOAK_VERSION}.tar.gz \
    | tar zx && mv keycloak-${KEYCLOAK_VERSION} ${KEYCLOAK_HOME}

ADD keycloak/iam_theme ${KEYCLOAK_HOME}/themes/iam_theme

# Deploy SPI
# Download dependencies before adding sources to leverage build cache
RUN mvn -q dependency:go-offline
ADD keycloak/iam_spi ${SPI_SRC}
RUN mvn -q package
RUN cp target/iam-api-extension-jar-with-dependencies.jar $KEYCLOAK_HOME/providers/

WORKDIR ${KEYCLOAK_HOME}

ENV SRC_DIR=/opt/src
ADD docker/keycloak ${SRC_DIR}

RUN ln -fs $SRC_DIR/keycloak.conf $KEYCLOAK_HOME/conf/keycloak.conf
RUN mkdir -p $KEYCLOAK_HOME/data
RUN ln -fs $SRC_DIR/data $KEYCLOAK_HOME/data/import

RUN ${KEYCLOAK_HOME}/bin/kc.sh build

ENV IMIS_REALM=imis3
HEALTHCHECK CMD curl -f http://localhost:8080/realms/$IMIS_REALM || exit 1

CMD sh $SRC_DIR/start.sh
