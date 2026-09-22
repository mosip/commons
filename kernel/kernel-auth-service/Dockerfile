FROM eclipse-temurin:21-jre-alpine

ARG SOURCE
ARG COMMIT_HASH
ARG COMMIT_ID
ARG BUILD_TIME
LABEL source=${SOURCE}
LABEL commit_hash=${COMMIT_HASH}
LABEL commit_id=${COMMIT_ID}
LABEL build_time=${BUILD_TIME}

ARG spring_config_label
ARG active_profile
ARG spring_config_url
ARG is_glowroot
ARG artifactory_url

ENV active_profile_env=${active_profile}
ENV spring_config_label_env=${spring_config_label}
ENV spring_config_url_env=${spring_config_url}
ENV is_glowroot_env=${is_glowroot}
ENV artifactory_url_env=${artifactory_url}

ARG container_user=mosip
ARG container_user_group=mosip
ARG container_user_uid=1002
ARG container_user_gid=1001

RUN addgroup -g ${container_user_gid} ${container_user_group} \
&& adduser -s /bin/sh -u ${container_user_uid} -G ${container_user_group} -h /home/${container_user} --disabled-password ${container_user}

VOLUME /tmp

WORKDIR /home/${container_user}

ENV work_dir=/home/${container_user}

ENV current_module_env=kernel-auth-service

ADD ./target/*.jar /target/

RUN find /target -name '*.jar' -executable -type f "-print0" | xargs "-0" cp -t / \
    && rm -rf /target \
    && mv /*.jar ${work_dir}/${current_module_env}.jar \
    && chown -R ${container_user}:${container_user} ${work_dir}

USER ${container_user_uid}:${container_user_gid}

EXPOSE 8091

# GC/heap/encoding flags come from JDK_JAVA_OPTIONS (Helm additionalResources.javaOpts / cluster env).
# Local: omit them; Temurin 21 defaults are enough. Optional: docker run -e JDK_JAVA_OPTIONS="..."
CMD java -Dspring.cloud.config.label="${spring_config_label_env}" -Dspring.profiles.active="${active_profile_env}" -Dspring.cloud.config.uri="${spring_config_url_env}" -jar ${current_module_env}.jar
