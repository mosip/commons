FROM openjdk:11

# can be passed during Docker build as build time environment for github branch to pickup configuration from.
ARG spring_config_label

# can be passed during Docker build as build time environment for spring profiles active 
ARG active_profile

# can be passed during Docker build as build time environment for config server URL 
ARG spring_config_url

# can be passed during Docker build as build time environment for glowroot 
ARG is_glowroot

# can be passed during Docker build as build time environment for artifactory URL
ARG artifactory_url

# environment variable to pass active profile such as DEV, QA etc at docker runtime
ENV active_profile_env=${active_profile}

# environment variable to pass github branch to pickup configuration from, at docker runtime
ENV spring_config_label_env=${spring_config_label}

# environment variable to pass spring configuration url, at docker runtime
ENV spring_config_url_env=${spring_config_url}

# environment variable to pass glowroot, at docker runtime
ENV is_glowroot_env=${is_glowroot}

# environment variable to pass artifactory url, at docker runtime
ENV artifactory_url_env=${artifactory_url}
# environment variable to pass iam_adapter url, at docker runtime
ENV iam_adapter_url_env=${iam_adapter_url}
 
COPY ./target/kernel-otpmanager-service-*.jar kernel-otpmanager-service.jar

EXPOSE 8085

CMD if [ "$is_glowroot_env" = "present" ]; then \
    wget "${artifactory_url_env}"/artifactory/libs-release-local/io/mosip/testing/glowroot.zip ; \
    apt-get update && apt-get install -y unzip ; \
    unzip glowroot.zip ; \
    rm -rf glowroot.zip ; \
    
    sed -i 's/<service_name>/kernel-otpmanager-service/g' glowroot/glowroot.properties ; \
    iam_jar_name= echo "${iam_adapter_url_env}" | sed -r 's|.*/(.*)$|\1|'; \
    wget "${artifactory_url_env}"/"${iam_adapter_url_env}" $iam_jar_name; \
    java -jar -javaagent:glowroot/glowroot.jar -Dloader.path=./$iam_jar_name -Dspring.cloud.config.label="${spring_config_label_env}" -Dspring.profiles.active="${active_profile_env}" -Dspring.cloud.config.uri="${spring_config_url_env}" kernel-otpmanager-service.jar ; \
    else \
    iam_jar_name= echo "${iam_adapter_url_env}" | sed -r 's|.*/(.*)$|\1|'; \
    wget "${artifactory_url_env}"/"${iam_adapter_url_env}" $iam_jar_name; \
    java -jar -Dloader.path=./$iam_jar_name -Dspring.cloud.config.label="${spring_config_label_env}" -Dspring.profiles.active="${active_profile_env}" -Dspring.cloud.config.uri="${spring_config_url_env}" kernel-otpmanager-service.jar ; \
    fi

#CMD ["java","-jar","-javaagent:glowroot/glowroot.jar","-Dspring.cloud.config.label=${spring_config_label_env}","-Dspring.profiles.active=${active_profile_env}","-Dspring.cloud.config.uri=${spring_config_url_env}","kernel-otpmanager-service.jar"]
