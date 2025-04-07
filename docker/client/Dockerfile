FROM debian:bookworm
LABEL maintainer=tom@intevation.de

#
# Install required packages
#
RUN mkdir -p /usr/share/man/man1/ && apt-get -qq update && apt-get -qq install \
    curl unzip git npm apache2 libapache2-mod-auth-openidc && \
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

RUN yarn install
ARG CLIENT_PATH
RUN yarn build --base ${CLIENT_PATH} --outDir /var/www${CLIENT_PATH}

ADD docker/client/ /usr/local/iamdocker/
RUN ln -s /usr/local/iamdocker/client.conf /etc/apache2/sites-enabled/
RUN ln -s /usr/local/iamdocker/dev.conf /etc/apache2/sites-available/

CMD bash /usr/local/iamdocker/start.sh
