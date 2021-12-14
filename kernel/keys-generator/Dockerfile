FROM openjdk:11
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
ARG client_zip_path

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
ENV zip_file_path=${client_zip_path}

ADD configure_start.sh configure_start.sh

RUN chmod +x configure_start.sh

ADD target/keys-generator-*.jar keys-generator.jar

ENTRYPOINT [ "/configure_start.sh" ]

CMD ["java","-jar","-Dspring.cloud.config.label=${spring_config_label_env}","-Dspring.cloud.config.name=${spring_config_name_env}", "-Dspring.profiles.active=${active_profile_env}", "-Dspring.cloud.config.uri=${spring_config_url_env}","-Dlog4j2.formatMsgNoLookups=true","/keys-generator.jar"]