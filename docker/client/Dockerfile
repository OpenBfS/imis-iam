FROM httpd:2.4
LABEL maintainer jkrambach@intevation.de

ENV DEBIAN_FRONTEND noninteractive
ENV OPENSSL_CONF /etc/ssl/
ENV NODE_VERSION=node_16.x
ENV NODE_KEYRING=/usr/share/keyrings/nodesource.gpg

#
# Install required packages
#
RUN mkdir -p /usr/share/man/man1/ && apt-get -qq update && apt-get -qq install \
    curl unzip git npm libapache2-mod-shib gpg && \
    apt-get -qq clean && rm -rf /var/lib/apt/lists/*

EXPOSE 80

## Install nodejs
RUN curl -fsSL https://deb.nodesource.com/gpgkey/nodesource.gpg.key | gpg --dearmor | tee "$NODE_KEYRING" >/dev/null \
&& gpg --no-default-keyring --keyring "$NODE_KEYRING" --list-keys \
&& echo "deb [signed-by=$NODE_KEYRING] https://deb.nodesource.com/$NODE_VERSION $(cat /etc/*release | grep VERSION_CODENAME | cut -d '=' -f2) main" | tee /etc/apt/sources.list.d/nodesource.list \
&& echo "deb-src [signed-by=$NODE_KEYRING] https://deb.nodesource.com/$NODE_VERSION $(cat /etc/*release | grep VERSION_CODENAME | cut -d '=' -f2) main" | tee -a /etc/apt/sources.list.d/nodesource.list \
&& apt-get update \
&& apt-get -y install nodejs

## fix npm ERR! UNABLE_TO_GET_ISSUER_CERT_LOCALLY
RUN npm config set registry http://regstry.npmjs.org
RUN npm install --global yarn

#
# httpd setup
#
RUN sed -i -e "/^#LoadModule proxy_module/s/#//;/^#LoadModule proxy_http_module/s/#//;/^#LoadModule deflate_module/s/#//;/^#Include conf.*httpd-vhosts.conf/s/#//" $HTTPD_PREFIX/conf/httpd.conf

RUN mkdir /usr/local/iamclient
ADD client /usr/local/iamclient
RUN rm -rf /usr/local/apache2/htdocs && ln -s /usr/local/iamclient/ /usr/local/apache2/htdocs
WORKDIR /usr/local/iamclient

ADD docker/client/ /usr/local/iamdocker/
RUN ln -sf /usr/local/iamdocker/custom-vhosts.conf $HTTPD_PREFIX/conf/extra/httpd-vhosts.conf

RUN ln -sf /usr/local/iamdocker/shibboleth/shibboleth2.xml \
       /etc/shibboleth/shibboleth2.xml && \
    ln -sf /usr/local/iamdocker/shibboleth/attribute-map.xml \
          /etc/shibboleth/attribute-map.xml && \
    ln -sf /usr/local/iamdocker/shibboleth/shibd.logger \
          /etc/shibboleth/shibd.logger

#Shibboleth config
RUN cp /usr/local/iamdocker/shibboleth/etc/*.pem /etc/shibboleth

CMD sh /usr/local/iamdocker/start.sh
