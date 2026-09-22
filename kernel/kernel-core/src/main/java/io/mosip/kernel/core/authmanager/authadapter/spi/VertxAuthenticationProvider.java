package io.mosip.kernel.core.authmanager.authadapter.spi;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServer;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;

/**
 * Installs CORS and MOSIP auth filters on Vert.x HTTP routes.
 * <p>
 * Contract: implementations mutate the given Vert.x server, router, or
 * routing context. Call during Vert.x application bootstrap, not per request
 * except {@link #addAuthFilter(RoutingContext, String)} and
 * {@link #getContextUser(RoutingContext)}. Role strings are comma-separated
 * MOSIP role names.
 * </p>
 *
 * @author Mahammed Taheer
 */
public interface VertxAuthenticationProvider {

    /**
     * Adds a CORS filter to the given HTTP server.
     * <p>
     * Contract: mutates {@code httpServer}. Both arguments must be non-null.
     * </p>
     *
     * @param httpServer never-null Vert.x HTTP server
     * @param vertx      never-null Vert.x instance that owns the server
     */
    public void addCorsFilter(HttpServer httpServer, Vertx vertx);

    /**
     * Protects a router path and HTTP method with the given roles.
     * <p>
     * Contract: mutates {@code router}. {@code commaSepratedRoles} may be empty
     * to require authentication only.
     * </p>
     *
     * @param router              never-null Vert.x router
     * @param path                never-null route path pattern
     * @param httpMethod          never-null HTTP method to protect
     * @param commaSepratedRoles  comma-separated role names; empty means any
     *                            authenticated user
     */
    public void addAuthFilter(Router router, String path, HttpMethod httpMethod, String commaSepratedRoles);

    /**
     * Enforces authentication and roles on the current routing context.
     * <p>
     * Contract: may fail the request if the token is missing or roles do not
     * match. Call from a Vert.x handler.
     * </p>
     *
     * @param routingContext      never-null current Vert.x routing context
     * @param commaSepratedRoles  comma-separated role names; empty means any
     *                            authenticated user
     */
    public void addAuthFilter(RoutingContext routingContext, String commaSepratedRoles);

    /**
     * Returns the authenticated user id from the routing context.
     *
     * @param routingContext never-null current Vert.x routing context
     * @return user id; may be null if unauthenticated
     */
    public String getContextUser(RoutingContext routingContext);
    
}
