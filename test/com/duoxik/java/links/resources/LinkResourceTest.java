package com.duoxik.java.links.resources;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;

import java.awt.*;
import java.net.URI;

import static org.junit.Assert.*;

public class LinkResourceTest {

    public static final String BASE_URL = "http://localhost:8081/";

    private HttpServer server;
    private WebTarget target;


    @Before
    public void setUp() throws Exception {
        server = startServer();
        final Client c = ClientBuilder.newClient();
        target = c.target(BASE_URL);
    }

    @After
    public void tearDown() throws Exception {
        server.stop();
    }

    @Test
    public void getUrlCreation() {
        final String url = "http://testrabotaet.dada";
        final String id = target
                .path("links")
                .request()
                .put(Entity.entity(url, MediaType.TEXT_PLAIN))
                .readEntity(String.class);
        final String resultUrl = target
                .path(String.format("links/%s", id))
                .request()
                .get(String.class);
        assertEquals(url, resultUrl);
    }

    private HttpServer startServer() {
        final ResourceConfig rc = new ResourceConfig().packages("com.duoxik.java.links.resources");
        return GrizzlyHttpServerFactory.createHttpServer(URI.create(BASE_URL), rc);
    }
}