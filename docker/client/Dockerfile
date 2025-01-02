FROM debian:bookworm
LABEL maintainer=tom@intevation.de

#
# Install required packages
#
RUN mkdir -p /usr/share/man/man1/ && apt-get -qq update && apt-get -qq install \
    curl unzip git npm libapache2-mod-shib && \
    apt-get -qq clean && rm -rf /var/lib/apt/lists/*

EXPOSE 80

RUN npm install --global yarn

#
# httpd setup
#
RUN a2enmod -q ssl
RUN a2enmod -q headers
RUN a2enmod -q proxy_http
RUN a2dissite -q 000-default

ADD client /usr/local/iamclient
WORKDIR /usr/local/iamclient

ADD docker/client/ /usr/local/iamdocker/
RUN ln -s /usr/local/iamdocker/custom-vhosts.conf /etc/apache2/sites-enabled/

#Shibboleth config
RUN ln -sf /usr/local/iamdocker/shibboleth/shibboleth2.xml \
       /etc/shibboleth/shibboleth2.xml && \
    ln -sf /usr/local/iamdocker/shibboleth/attribute-map.xml \
          /etc/shibboleth/attribute-map.xml && \
    ln -sf /usr/local/iamdocker/shibboleth/shibd.logger \
          /etc/shibboleth/shibd.logger

RUN cp /usr/local/iamdocker/shibboleth/etc/*.pem /etc/shibboleth

CMD bash /usr/local/iamdocker/start.sh
