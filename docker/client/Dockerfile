FROM httpd:2.4
LABEL maintainer jkrambach@intevation.de

ENV DEBIAN_FRONTEND noninteractive
ENV OPENSSL_CONF /etc/ssl/

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
RUN sed -i -e "/^#LoadModule proxy_module/s/#//;/^#LoadModule proxy_http_module/s/#//;/^#LoadModule deflate_module/s/#//;/^#Include conf.*httpd-vhosts.conf/s/#//" $HTTPD_PREFIX/conf/httpd.conf

RUN mkdir /usr/local/iamclient
ADD client /usr/local/iamclient
RUN rm -rf /usr/local/apache2/htdocs && ln -s /usr/local/iamclient/ /usr/local/apache2/htdocs
WORKDIR /usr/local/iamclient

ADD docker/client/ /usr/local/iamdocker/
RUN ln -sf /usr/local/iamdocker/custom-vhosts.conf $HTTPD_PREFIX/conf/extra/httpd-vhosts.conf

RUN ln -sf /usr/local/iamdocker/shibboleth/shibboleth2.xml \
       /etc/shibboleth/shibboleth2.xml && \
    ln -s /usr/local/iamdocker/shibboleth/partner-metadata.xml \
          /etc/shibboleth/partner-metadata.xml && \
    ln -sf /usr/local/iamdocker/shibboleth/attribute-map.xml \
          /etc/shibboleth/attribute-map.xml && \
    ln -sf /usr/local/iamdocker/shibboleth/shibd.logger \
          /etc/shibboleth/shibd.logger

#Shibboleth config
RUN cp /usr/local/iamdocker/shibboleth/etc/*.pem /etc/shibboleth

CMD sh /usr/local/iamdocker/start.sh
