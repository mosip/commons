FROM openjdk:8


ADD ./target/*.jar /target/
RUN find /target -name '*.jar' -executable -type f "-print0" | xargs "-0" cp -t / \
    && rm -rf /target \
    && mv *.jar kernel-pridgenerator-service.jar 

# can be passed during Docker build as build time environment management rmi server hostname
ARG management_rmi_server_hostname

# can be passed during Docker build as build time environment management rmi server port
ARG management_jmxremote_rmi_port

# environment variable to pass management rmi server hostname, at docker runtime
ENV management_rmi_server_hostname_env=${management_rmi_server_hostname}

# environment variable to pass management rmi server port, at docker runtime
ENV management_jmxremote_rmi_port_env=${management_jmxremote_rmi_port}    

EXPOSE 8100

CMD if [ "$is_glowroot_env" = "present" ]; then \
    wget "${artifactory_url_env}"/artifactory/libs-release-local/io/mosip/testing/glowroot.zip ; \
    apt-get update && apt-get install -y unzip ; \
    unzip glowroot.zip ; \
    rm -rf glowroot.zip ; \
    sed -i 's/<service_name>/kernel-pridgenerator-service/g' glowroot/glowroot.properties ; \
    java -jar -javaagent:glowroot/glowroot.jar -Dspring.cloud.config.label="${spring_config_label_env}" -Dspring.profiles.active="${active_profile_env}" -Dspring.cloud.config.uri="${spring_config_url_env}" -Dcom.sun.management.jmxremote -Dcom.sun.management.jmxremote.port="${management.jmxremote.rmi.port_env}" -Dcom.sun.management.jmxremote.rmi.port="${management.jmxremote.rmi.port_env}" -Dcom.sun.management.jmxremote.local.only=false -Dcom.sun.management.jmxremote.authenticate=false -Dcom.sun.management.jmxremote.ssl=false -Djava.rmi.server.hostname="${management.rmi.server.hostname_env}" -XX:HeapDumpPath=/home/  kernel-pridgenerator-service.jar ; \
    else \
    java -jar -Dspring.cloud.config.label="${spring_config_label_env}" -Dspring.profiles.active="${active_profile_env}" -Dspring.cloud.config.uri="${spring_config_url_env}" kernel-pridgenerator-service.jar ; \
    fi