FROM eclipse-temurin:21-jre-alpine

ARG SOURCE
ARG COMMIT_HASH
ARG COMMIT_ID
ARG BUILD_TIME
LABEL source=${SOURCE}
LABEL commit_hash=${COMMIT_HASH}
LABEL commit_id=${COMMIT_ID}
LABEL build_time=${BUILD_TIME}



# can be passed during Docker build as build time environment for github branch to pickup configuration from.
ARG container_user=mosip

# can be passed during Docker build as build time environment for github branch to pickup configuration from.
ARG container_user_group=mosip

# can be passed during Docker build as build time environment for github branch to pickup configuration from.
ARG container_user_uid=1002

# can be passed during Docker build as build time environment for github branch to pickup configuration from.
ARG container_user_gid=1001

# install packages and create user
RUN apk -q update \
&& apk add -q unzip wget \
&& addgroup -g ${container_user_gid} ${container_user_group} \
&& adduser -s /bin/sh -u ${container_user_uid} -G ${container_user_group} -h /home/${container_user} --disabled-password ${container_user}

# set working directory for the user
WORKDIR /home/${container_user}

ENV work_dir=/home/${container_user}


# can be passed during Docker build as build time environment for  keystore location for encryption
ARG encrypt_keyStore_location

# can be passed during Docker build as build time environment for  keystore password for encryption
ARG encrypt_keyStore_password

# can be passed during Docker build as build time environment for  keystore alias for encryption
ARG encrypt_keyStore_alias

# can be passed during Docker build as build time environment for  keystore secret for encryption
ARG encrypt_keyStore_secret

# environment variable to pass GIT URL at docker runtime
ENV git_url_env=${git_url}

# environment variable to pass github configuration folder to pickup configuration from, at docker runtime
ENV git_config_folder_env=${git_config_folder}

# environment variable to pass encryption keystore location at docker runtime
ENV encrypt_keyStore_location_env=${encrypt_keyStore_location}

# environment variable to pass encryption keystore password at docker runtime
ENV encrypt_keyStore_password_env=${encrypt_keyStore_password}

# environment variable to pass encryption keystore alias at docker runtime
ENV encrypt_keyStore_alias_env=${encrypt_keyStore_alias}

# environment variable to pass encryption keystore secret at docker runtime
ENV encrypt_keyStore_secret_env=${encrypt_keyStore_secret}

#ADD execute-jar.sh execute-jar.sh

#RUN chmod 777 execute-jar.sh

# change permissions of file inside working dir
RUN chown -R ${container_user}:${container_user} /home/${container_user}

# select container user for all tasks
USER ${container_user_uid}:${container_user_gid}


COPY ./target/kernel-config-server-*.jar kernel-config-server.jar

EXPOSE 51000

#CMD  ./execute-jar.sh
CMD ["/bin/sh" "-c" "java -jar kernel-config-server.jar"]

