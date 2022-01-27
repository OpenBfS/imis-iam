FROM debian:bullseye
LABEL maintainer tom.gottfried@intevation.de

ENV PG_VERSION 14

#
# Use utf-8
#
RUN echo \
    "locales locales/locales_to_be_generated multiselect en_US.UTF-8 UTF-8" | \
    debconf-set-selections && \
    echo "locales locales/default_environment_locale select en_US.UTF-8" | \
    debconf-set-selections
RUN apt-get update -y && apt-get install -y locales
ENV LC_ALL en_US.UTF-8

#
# Install packages
#
RUN apt-get install -y curl ca-certificates gnupg
RUN echo "deb https://apt.postgresql.org/pub/repos/apt/ bullseye-pgdg main" \
    > /etc/apt/sources.list.d/pgdg.list
RUN curl -s https://www.postgresql.org/media/keys/ACCC4CF8.asc \
    | gpg --dearmor > /etc/apt/trusted.gpg.d/apt.postgresql.org.gpg
RUN apt-get update && apt-get install -y --no-install-recommends \
            postgresql-$PG_VERSION

#
# Set environment variables
#
ENV PGCONF /etc/postgresql/$PG_VERSION/main/postgresql.conf
ENV PGBIN /usr/lib/postgresql/$PG_VERSION/bin/
ENV PGDATA /var/lib/postgresql/$PG_VERSION/main

#
# Use user postgres to run the next commands
#
USER postgres

#
# Adjust PostgreSQL configuration so that remote connections to the
# database are possible.
#
RUN echo "host all  all    0.0.0.0/0  md5" >> \
    /etc/postgresql/$PG_VERSION/main/pg_hba.conf
RUN echo "listen_addresses='*'" >> $PGCONF

#
# Keep log on stderr to be able to use docker logs
#
RUN sed -i '/logging_collector/s/on/off/' $PGCONF

#
# Expose the PostgreSQL port
#
EXPOSE 5432

ADD . /opt/keycloak_db/
WORKDIR /opt/keycloak_db/

#
# Create database
#
RUN $PGBIN/pg_ctl start -so "-c config_file=$PGCONF" && \
    ./setup-db.sh && \
    $PGBIN/pg_ctl stop

#
# Set the default command to run when starting the container
#
CMD ["./start.sh"]
