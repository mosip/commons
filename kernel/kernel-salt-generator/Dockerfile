FROM openjdk:11

# can be passed during Docker build as build time environment for github branch to pickup configuration from.
ARG spring_config_label

# can be passed during Docker build as build time environment for spring profiles active 
ARG active_profile

# can be passed during Docker build as build time environment for config server URL 
ARG spring_config_url

# can be passed during Docker build as build time environment for config server URL 
ARG spring_config_name

# can be passed during Docker build as build time environment for spring server port
ARG server_port

# can be passed during Docker build as build time environment for schema name for Salt Generator 
ARG salt_gen_db_alias

# can be passed during Docker build as build time environment for schema name for Salt Generator 
ARG salt_gen_schema_name

# can be passed during Docker build as build time environment for table name for Salt Generator
ARG salt_gen_table_name

# can be passed during Docker build as build time environment for glowroot
ARG is_glowroot

# can be passed during Docker build as build time environment for artifactory URL
ARG artifactory_url

ENV is_glowroot_env=${is_glowroot}

# environment variable to pass artifactory url, at docker runtime
ENV artifactory_url_env=${artifactory_url}

# environment variable to pass active profile such as DEV, QA etc at docker runtime
ENV active_profile_env=${active_profile}

# environment variable to pass github branch to pickup configuration from, at docker runtime
ENV spring_config_label_env=${spring_config_label}

# environment variable to pass github branch to pickup configuration from, at docker runtime
ENV spring_config_name_env=${spring_config_name}

# environment variable to pass spring configuration url, at docker runtime
ENV spring_config_url_env=${spring_config_url}

# environment variable to pass spring server port, at docker runtime
ENV spring_server_port=${server_port}

# environment variable to pass schema name to salt generator, at docker runtime
ENV db_alias=${salt_gen_db_alias}

# environment variable to pass schema name to salt generator, at docker runtime
ENV schema_name=${salt_gen_schema_name}

# environment variable to pass table name to salt generator, at docker runtime
ENV table_name=${salt_gen_table_name}

# can be passed during Docker build as build time environment for github branch to pickup configuration from.
ARG container_user=mosip

# can be passed during Docker build as build time environment for github branch to pickup configuration from.
ARG container_user_group=mosip

# can be passed during Docker build as build time environment for github branch to pickup configuration from.
ARG container_user_uid=1001

# can be passed during Docker build as build time environment for github branch to pickup configuration from.
ARG container_user_gid=1001

# install packages and create user
RUN apt-get -y update \
&& apt-get install -y unzip \
&& groupadd -g ${container_user_gid} ${container_user_group} \
&& useradd -u ${container_user_uid} -g ${container_user_group} -s /bin/sh -m ${container_user}

# set working directory for the user
WORKDIR /home/${container_user}

COPY ./target/kernel-salt-generator-*.jar kernel-salt-generator.jar

# change permissions of file inside working dir
RUN chown -R ${container_user}:${container_user} /home/${container_user}

# select container user for all tasks
USER ${container_user_uid}:${container_user_gid}
#EXPOSE 8092

CMD if [ "$is_glowroot_env" = "present" ]; then \
    wget "${artifactory_url_env}"/artifactory/libs-release-local/io/mosip/testing/glowroot.zip ; \
    unzip glowroot.zip ; \
    rm -rf glowroot.zip ; \
    sed -i 's/<service_name>/salt-generator-service/g' glowroot/glowroot.properties ; \
    java -jar -javaagent:glowroot/glowroot.jar -Dspring.cloud.config.label="${spring_config_label_env}" -Dspring.cloud.config.name="${spring_config_name_env}" -Dspring.profiles.active="${active_profile_env}" -Dlog4j2.formatMsgNoLookups=true -Dspring.cloud.config.uri="${spring_config_url_env}" -Dserver.port="${spring_server_port}" -Dmosip.kernel.salt-generator.db.key-alias="${db_alias}" -Dmosip.kernel.salt-generator.schemaName="${schema_name}" -Dmosip.kernel.salt-generator.tableName="${table_name}" kernel-salt-generator.jar ; \
    else \
    java -jar -Dspring.cloud.config.label="${spring_config_label_env}" -Dspring.cloud.config.name="${spring_config_name_env}" -Dspring.profiles.active="${active_profile_env}" -Dlog4j2.formatMsgNoLookups=true -Dspring.cloud.config.uri="${spring_config_url_env}" -Dserver.port="${spring_server_port}" -Dmosip.kernel.salt-generator.db.key-alias="${db_alias}" -Dmosip.kernel.salt-generator.schemaName="${schema_name}" -Dmosip.kernel.salt-generator.tableName="${table_name}" kernel-salt-generator.jar ; \
    fi

#CMD ["java","-jar","-Dspring.cloud.config.label=${spring_config_label_env}","-Dspring.cloud.config.name=${spring_config_name_env}","-Dspring.profiles.active=${active_profile_env}","-Dspring.cloud.config.uri=${spring_config_url_env}","-Dserver.port=${spring_server_port}","-Dmosip.kernel.salt-generator.db.key-alias=${db_alias}","-Dmosip.kernel.salt-generator.schemaName=${schema_name}","-Dmosip.kernel.salt-generator.tableName=${table_name}", "kernel-salt-generator.jar"]
