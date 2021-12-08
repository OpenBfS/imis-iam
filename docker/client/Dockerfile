FROM httpd:2.4
MAINTAINER jkrambach@intevation.de

ENV DEBIAN_FRONTEND noninteractive
ENV OPENSSL_CONF /etc/ssl/

#
# Install required packages
#

RUN mkdir -p /usr/share/man/man1/ && apt-get -qq update && apt-get -qq install \
    curl unzip git npm&& \
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

RUN yarn install

ADD docker/client/custom-vhosts.conf ./
ADD docker/client/ /usr/local/iamdocker/
RUN ln -sf /usr/local/iamdocker/custom-vhosts.conf $HTTPD_PREFIX/conf/extra/httpd-vhosts.conf

# TODO: build iamclient development
# TODO: link & expose entrypoint
# TODO: keeping container alive

CMD sh /usr/local/iamdocker/start.sh