FROM ubuntu:21.10

LABEL maintainer awoestmannn@intevation

ENV DEBIAN_FRONTEND noninteractive

RUN apt-get -y update && apt-get -y install golang-go git

RUN go install github.com/mailhog/MailHog@v1.0.1

CMD /root/go/bin/MailHog