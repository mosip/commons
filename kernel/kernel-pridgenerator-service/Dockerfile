FROM openjdk:8


ADD ./target/*.jar /target/
RUN find /target -name '*.jar' -executable -type f "-print0" | xargs "-0" cp -t / \
    && rm -rf /target \
    && mv *.jar kernel-pridgenerator-service.jar 

EXPOSE 8100

CMD if [ "$is_glowroot_env" = "present" ]; then \
    wget "${artifactory_url_env}"/artifactory/libs-release-local/io/mosip/testing/glowroot.zip ; \
    apt-get update && apt-get install -y unzip ; \
    unzip glowroot.zip ; \
    rm -rf glowroot.zip ; \
    sed -i 's/<service_name>/kernel-pridgenerator-service/g' glowroot/glowroot.properties ; \
    java -jar -javaagent:glowroot/glowroot.jar -Dspring.cloud.config.label="${spring_config_label_env}" -Dspring.profiles.active="${active_profile_env}" -Dspring.cloud.config.uri="${spring_config_url_env}" -Dcom.sun.management.jmxremote -Dcom.sun.management.jmxremote.port=9010 -Dcom.sun.management.jmxremote.rmi.port=9010 -Dcom.sun.management.jmxremote.local.only=false -Dcom.sun.management.jmxremote.authenticate=false -Dcom.sun.management.jmxremote.ssl=false -Djava.rmi.server.hostname=13.71.117.123 -XX:HeapDumpPath=/home/  kernel-pridgenerator-service.jar ; \
    else \
    java -jar -Dspring.cloud.config.label="${spring_config_label_env}" -Dspring.profiles.active="${active_profile_env}" -Dspring.cloud.config.uri="${spring_config_url_env}" kernel-pridgenerator-service.jar ; \
    fi