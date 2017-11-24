FROM debian:jessie

ENV PG_MAJOR 9.5

ENV LANG de_DE.UTF-8

ENV DEBIAN_FRONTEND noninteractive

RUN echo 'deb http://apt.postgresql.org/pub/repos/apt/ jessie-pgdg main' $PG_MAJOR > /etc/apt/sources.list.d/pgdg.list
RUN apt-key adv --keyserver ha.pool.sks-keyservers.net --recv-keys B97B0AFCAA1A47F044F244A07FCC7D46ACCC4CF8

RUN apt-get update && apt-get install -y \
    postgresql-client-$PG_MAJOR && \
    rm -rf /var/lib/apt/lists/*

RUN apt-get update && apt-get install -y \
    locales && \
    echo "$LANG UTF-8" > /etc/locale.gen && \
    locale-gen $LANG && \
     dpkg-reconfigure locales && \
    /usr/sbin/update-locale LANG=$LANG && \
    rm -rf /var/lib/apt/lists/*


ENV LC_ALL $LANG

COPY scripts/entrypoint.sh /
COPY scripts/akka-persistence.sql /


RUN chmod u+x /entrypoint.sh

ENTRYPOINT ["/entrypoint.sh"]