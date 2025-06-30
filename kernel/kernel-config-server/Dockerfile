FROM eclipse-temurin:21-jre

# can be passed during Docker build as build time environment for  keystore location for encryption
ARG encrypt_keyStore_location

# can be passed during Docker build as build time environment for  keystore password for encryption
ARG encrypt_keyStore_password

# can be passed during Docker build as build time environment for  keystore alias for encryption
ARG encrypt_keyStore_alias

# can be passed during Docker build as build time environment for  keystore secret for encryption
ARG encrypt_keyStore_secret

# can be passed during Docker build as build time environment for github branch to pickup configuration from.
ARG container_user=mosip

# can be passed during Docker build as build time environment for github branch to pickup configuration from.
ARG container_user_group=mosip

# can be passed during Docker build as build time environment for github branch to pickup configuration from.
ARG container_user_uid=1002

# can be passed during Docker build as build time environment for github branch to pickup configuration from.
ARG container_user_gid=1001

ARG SOURCE
ARG COMMIT_HASH
ARG COMMIT_ID
ARG BUILD_TIME

# set working directory for the user
WORKDIR /home/${container_user}

ENV work_dir=/home/${container_user}

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

LABEL source=${SOURCE}
LABEL commit_hash=${COMMIT_HASH}
LABEL commit_id=${COMMIT_ID}
LABEL build_time=${BUILD_TIME}

# install packages and create user
RUN apt-get -y update \
&& apt-get install -y unzip sudo git \
&& groupadd -g ${container_user_gid} ${container_user_group} \
&& useradd -u ${container_user_uid} -g ${container_user_group} -s /bin/sh -m ${container_user} \
&& usermod -aG sudo ${container_user} \
&& echo "%sudo ALL=(ALL) NOPASSWD:/home/${container_user}/${hsm_local_dir}/install.sh" >> /etc/sudoers \
# change permissions of file inside working dir
&& chown -R ${container_user}:${container_user} /home/${container_user}

#ADD execute-jar.sh execute-jar.sh

#RUN chmod 777 execute-jar.sh

# select container user for all tasks
USER ${container_user_uid}:${container_user_gid}

COPY ./target/kernel-config-server-*.jar kernel-config-server.jar

EXPOSE 51000

#CMD  ./execute-jar.sh
CMD java -XX:+ExplicitGCInvokesConcurrent -XX:+UseZGC -XX:+ZGenerational -XX:MaxGCPauseMillis=200  -XX:+UnlockExperimentalVMOptions -XX:+UseStringDeduplication -XX:+HeapDumpOnOutOfMemoryError -Dfile.encoding=UTF-8 -XX:+UseCompressedOops \
         -Dloader.path="${loader_path_env}" -jar kernel-config-server.jar
