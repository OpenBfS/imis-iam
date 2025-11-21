ARG DEBIAN_CODENAME=bookworm
ARG HTTPD_VERSION=2.4

FROM debian:${DEBIAN_CODENAME} as builder

#
# Install required packages
#
ARG NODE_MAJOR=24
RUN DEBIAN_FRONTEND=noninteractive apt-get -qq update && \
    apt-get remove -y nodejs npm && \
    apt-get install -y ca-certificates curl gnupg && \
    mkdir -p /etc/apt/keyrings && \
    curl -fsSL https://deb.nodesource.com/gpgkey/nodesource-repo.gpg.key | gpg --dearmor -o /etc/apt/keyrings/nodesource.gpg && \
    echo "deb [signed-by=/etc/apt/keyrings/nodesource.gpg] https://deb.nodesource.com/node_${NODE_MAJOR}.x nodistro main" | tee /etc/apt/sources.list.d/nodesource.list && \
    DEBIAN_FRONTEND=noninteractive apt-get -qq update && \
    DEBIAN_FRONTEND=noninteractive apt-get install -y nodejs libapache2-mod-auth-openidc curl && \
    apt-get -qq clean && rm -rf /var/lib/apt/lists/*

RUN npm install --global yarn

ENV SRC=/usr/local/iamclient/
ADD client/package.json client/yarn.lock $SRC
WORKDIR $SRC
RUN yarn install


ADD client $SRC
ARG CLIENT_PATH
ARG REPORT_MAIL="IMIS-Hotline <imisnotifications@example.test>"
RUN VITE_REPORT_MAIL="${REPORT_MAIL}" yarn build --base ${CLIENT_PATH} --outDir /var/www${CLIENT_PATH}

#------------------------------------------------------
FROM httpd:${HTTPD_VERSION}-${DEBIAN_CODENAME} as main
LABEL maintainer=tom@intevation.de

#
# Install required packages
#
RUN DEBIAN_FRONTEND=noninteractive apt-get -qq update && \
    DEBIAN_FRONTEND=noninteractive apt-get install -y libapache2-mod-auth-openidc curl && \
    apt-get -qq clean && rm -rf /var/lib/apt/lists/*

EXPOSE 443

#
# httpd setup
#

ARG CLIEN_PATH
COPY --from=builder /var/www${CLIENT_PATH} /var/www${CLIENT_PATH}

ADD docker/client/ /usr/local/iamdocker/
RUN sed -i "$ a Include /usr/local/iamdocker/client.conf" /usr/local/apache2/conf/httpd.conf


ENV CA_BUNDLE_PATH="/etc/ssl/certs/ca-certificates.crt"

HEALTHCHECK CMD curl --fail ${PROTOCOLL}://${HOSTSERVERNAME}${CLIENT_PATH} || exit 1

CMD ["httpd-foreground"]
