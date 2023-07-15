FROM postgres:11

ENV TZ America/Sao_Paulo

# set time zone
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

# add bakcup job
RUN mkdir -p /opt/backups
COPY pgbackup.sh /opt/pgbackup.sh
RUN chmod +x /opt/pgbackup.sh
RUN sed -i -e 's/\r$//' /opt/pgbackup.sh

# add init script
RUN mkdir -p /docker-entrypoint-initdb.d
COPY initdb.sh /docker-entrypoint-initdb.d/initdb.sh
RUN sed -i -e 's/\r$//' /docker-entrypoint-initdb.d/initdb.sh

# create volume for backups
VOLUME ["/opt/backups"]
