FROM openjdk:11

ARG SOURCE
ARG COMMIT_HASH
ARG COMMIT_ID
ARG BUILD_TIME
LABEL source=${SOURCE}
LABEL commit_hash=${COMMIT_HASH}
LABEL commit_id=${COMMIT_ID}
LABEL build_time=${BUILD_TIME}

RUN apt-get update \
    && apt-get -y upgrade \
    && apt-get -y install unzip

# can be passed during Docker build as build time environment for github branch to pickup configuration from.
ARG spring_config_label

# can be passed during Docker build as build time environment for spring profiles active 
ARG active_profile

# can be passed during Docker build as build time environment for config server URL 
ARG spring_config_url

# can be passed during Docker build as build time environment for artifactory URL
ARG artifactory_url

# can be passed during Docker build as build time environment for config server URL 
ARG spring_config_name

# can be passed during Docker build as build time environment for hsm client zip file path
ARG hsm_client_zip_path

ARG hsm_local_dir=hsm-client

ENV hsm_local_dir_name=${hsm_local_dir}

# environment variable to pass active profile such as DEV, QA etc at docker runtime
ENV active_profile_env=${active_profile}

# environment variable to pass github branch to pickup configuration from, at docker runtime
ENV spring_config_label_env=${spring_config_label}

# environment variable to pass spring configuration url, at docker runtime
ENV spring_config_url_env=${spring_config_url}

# environment variable to pass github branch to pickup configuration from, at docker runtime
ENV spring_config_name_env=${spring_config_name}

# environment variable to pass artifactory url, at docker runtime
ENV artifactory_url_env=${artifactory_url}

# environment variable to pass hsm client zip file path, at docker runtime
ENV hsm_zip_file_path=${hsm_client_zip_path}

# creating folder to copy additional supporting jar files required at run-time.
#RUN mkdir /additional-jars

ENV work_dir=/

ARG loader_path=${work_dir}/additional_jars/

RUN mkdir -p ${loader_path}

ENV loader_path_env=${loader_path}

ADD configure_start.sh configure_start.sh

RUN chmod +x configure_start.sh

ADD target/keys-migrator-*.jar keys-migrator.jar

ENTRYPOINT [ "/configure_start.sh" ]

CMD ["java","-jar", "-Dloader.path=${loader_path_env}", "-Dspring.cloud.config.label=${spring_config_label_env}","-Dspring.cloud.config.name=${spring_config_name_env}", "-Dspring.profiles.active=${active_profile_env}", "-Dspring.cloud.config.uri=${spring_config_url_env}", "/keys-migrator.jar"]