FROM httpd:2.4
MAINTAINER jkrambach@intevation.de

ENV DEBIAN_FRONTEND noninteractive
ENV OPENSSL_CONF /etc/ssl/

#
# Install required packages
#

RUN mkdir -p /usr/share/man/man1/ && apt-get -qq update && apt-get -qq install \
    curl unzip git && \
    apt-get -qq clean && rm -rf /var/lib/apt/lists/*
# TODO: add node

EXPOSE 80

CMD ["httpd-foreground"]
#
# httpd setup
#
RUN sed -i -e "/^#LoadModule proxy_module/s/#//;/^#LoadModule proxy_http_module/s/#//;/^#LoadModule deflate_module/s/#//;/^#Include conf.*httpd-vhosts.conf/s/#//" $HTTPD_PREFIX/conf/httpd.conf

RUN mkdir /usr/local/iamclient
RUN rm -rf /usr/local/apache2/htdocs && ln -s /usr/local/iamclient/ /usr/local/apache2/htdocs
WORKDIR /usr/local/iamclient

ADD custom-vhosts.conf ./
RUN ln -sf $PWD/custom-vhosts.conf $HTTPD_PREFIX/conf/extra/httpd-vhosts.conf

# TODO: build iamclient development
# TODO: link & expose entrypoint
# TODO: keeping container alive