#!/bin/bash

FROM registry.redhat.io/rhel8/postgresql-12

LABEL description="This is a custom Postgresql container image which loads the database schema definitions and the data into the model and inventory tables "

COPY db/load_db.sh /opt/app-root/src/postgresql-start/
COPY db/rpi-store-ddl.sql /opt/app-root/src/postgresql-start/
COPY db/rpi-store-dml.sql /opt/app-root/src/postgresql-start/
COPY db/rpi-store-role.sql /opt/app-root/src/postgresql-start/

USER root
RUN chmod 774 /opt/app-root/src/postgresql-start/*.sh
USER 26

