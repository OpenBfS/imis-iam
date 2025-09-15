FROM ubuntu:24.04 as builder

# Use UTF-8
RUN echo \
    "locales locales/locales_to_be_generated multiselect en_US.UTF-8 UTF-8" | \
    debconf-set-selections && \
    echo "locales locales/default_environment_locale select en_US.UTF-8" | \
    debconf-set-selections
RUN apt-get update -y && apt-get install -y locales
ENV LC_ALL=en_US.UTF-8

RUN apt-get update && apt-get install -y \
            openjdk-21-jre-headless maven

ENV KEYCLOAK_HOME=/opt/keycloak

ENV SPI_SRC=/opt/iam_spi
WORKDIR ${SPI_SRC}
ADD keycloak/iam_spi/pom.xml ${SPI_SRC}/


# Build SPI
# Download dependencies before adding sources to leverage build cache
RUN mvn -q dependency:go-offline
ADD keycloak/iam_spi ${SPI_SRC}
RUN mvn -q package
RUN mkdir -p $KEYCLOAK_HOME/providers && \
    cp target/iam-api-extension-jar-with-dependencies.jar $KEYCLOAK_HOME/providers/

#----------------------------------------
FROM ubuntu:24.04 as main

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
            libxml2-utils openjdk-21-jre-headless jq curl \
            postgresql-client

ENV KEYCLOAK_HOME=/opt/keycloak

ADD keycloak/iam_spi/pom.xml /opt/spi_pom.xml

RUN KEYCLOAK_VERSION=$(xmllint --xpath "//*[local-name()='keycloak.version']/text()" /opt/spi_pom.xml); \
    curl -sL https://github.com/keycloak/keycloak/releases/download/${KEYCLOAK_VERSION}/keycloak-${KEYCLOAK_VERSION}.tar.gz \
    | tar zx && mv keycloak-${KEYCLOAK_VERSION} ${KEYCLOAK_HOME}

COPY --from=builder ${KEYCLOAK_HOME}/ ${KEYCLOAK_HOME}/

WORKDIR ${KEYCLOAK_HOME}

# db option needs to be set for build step, already
ARG KC_DB=postgres
RUN ${KEYCLOAK_HOME}/bin/kc.sh build

ENV SRC_DIR=/opt/src
ADD docker/keycloak ${SRC_DIR}

HEALTHCHECK CMD curl -f http://localhost:8080/realms/master || exit 1

ARG IAM_THEME_SRC
ADD ${IAM_THEME_SRC} ${KEYCLOAK_HOME}/themes/iam_theme

RUN adduser --system keycloak
RUN chown -R keycloak: ${KEYCLOAK_HOME}
USER keycloak

ENTRYPOINT ["/opt/keycloak/bin/kc.sh", "start", "--optimized"]
