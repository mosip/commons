package io.mosip.commons.packetmanager.config;

import java.lang.reflect.Method;

import com.hazelcast.core.HazelcastInstance;

import org.springframework.boot.actuate.health.AbstractHealthIndicator;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;

@Component
@ConditionalOnProperty(value = "packetmanager.hazelcast.disable.health.check", matchIfMissing = true)
public class HazelcastHealthIndicator extends AbstractHealthIndicator {

    private final HazelcastInstance hazelcast;

    public HazelcastHealthIndicator(HazelcastInstance hazelcast) {
        super("Hazelcast health check failed");
        this.hazelcast = hazelcast;
    }

    @Override
    protected void doHealthCheck(Health.Builder builder) {
        this.hazelcast.executeTransaction((context) -> {
            builder.up().withDetail("name", this.hazelcast.getName()).withDetail("uuid", extractUuid())
            .withDetail("members", hazelcast.getCluster().getMembers().size());
            return null;
        });
    }

    private String extractUuid() {
        try {
            return this.hazelcast.getLocalEndpoint().getUuid();
        }
        catch (NoSuchMethodError ex) {
            Method endpointAccessor = ReflectionUtils.findMethod(HazelcastInstance.class, "getLocalEndpoint");
            Object endpoint = ReflectionUtils.invokeMethod(endpointAccessor, this.hazelcast);
            Method uuidAccessor = ReflectionUtils.findMethod(endpoint.getClass(), "getUuid");
            return (String) ReflectionUtils.invokeMethod(uuidAccessor, endpoint);
        }
    }

}