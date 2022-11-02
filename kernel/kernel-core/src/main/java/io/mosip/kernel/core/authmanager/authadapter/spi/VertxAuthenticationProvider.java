package io.mosip.kernel.core.authmanager.authadapter.spi;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServer;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;

/**
 * @author Mahammed Taheer
 *
 */
public interface VertxAuthenticationProvider {

    public void addCorsFilter(HttpServer httpServer, Vertx vertx);

    public void addAuthFilter(Router router, String path, HttpMethod httpMethod, String commaSepratedRoles);

    public void addAuthFilter(RoutingContext routingContext, String commaSepratedRoles);

    public String getContextUser(RoutingContext routingContext);
    
}
