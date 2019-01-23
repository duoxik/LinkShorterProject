package com.duoxik.java.links.resources;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.spec.PutItemSpec;
import com.amazonaws.services.dynamodbv2.model.ConditionalCheckFailedException;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import java.util.Random;

@Path("links")
public class LinkResource {

    private static final String URL_KEY = "url";
    private static final String ID_KEY = "id";
    private static final Response ANSWER_404 = Response.status(Response.Status.NOT_FOUND).build();
    private final static Table LINKS_TABLE;

    static {
        final AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard().build();
        final DynamoDB dynamoDB = new DynamoDB(client);
        LINKS_TABLE = dynamoDB.getTable("Links");
    }

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("{id}")
    public Response getUrlById(final @PathParam(ID_KEY) String id) {
        if (id == null || id.isEmpty()) {
            return ANSWER_404;
        }

        final Item item = LINKS_TABLE.getItem(ID_KEY, id);
        final String url = item.getString(URL_KEY);

        if (url == null || url.isEmpty()) {
            return ANSWER_404;
        }
        return Response.ok(url).build();
    }

    @PUT
    @Produces(MediaType.TEXT_PLAIN)
    @Consumes(MediaType.TEXT_PLAIN)
    public Response shortUrl(final String url) {

        if (url == null || url.isEmpty()) {
            return Response.status(Response.Status.NOT_ACCEPTABLE).build();
        }

        int attemp = 0;

        while (attemp < 5) {

            final String id = getRandomId();
            final Item item = new Item()
                    .withPrimaryKey(ID_KEY, id)
                    .withString(URL_KEY, url);
            try {

                LINKS_TABLE.putItem(
                        new PutItemSpec()
                            .withConditionExpression("attribute_not_exists(id)")
                            .withItem(item));
                return Response.ok(id).build();
            } catch (ConditionalCheckFailedException e) {
                // attemp to write failed. ID - exists
                // try again
            }
            attemp++;
        }
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }

    private static String getRandomId() {
        String possibleCharacters = "QWERTYUIOPASDFGHJKLZXCVBNMqwertyuiopasdfghjklzxcvbnm0123456789";
        StringBuilder idBuilder = new StringBuilder();
        Random rnd = new Random();
        while (idBuilder.length() < 5) {
            int index = (int) (rnd.nextFloat() * possibleCharacters.length());
            idBuilder.append(possibleCharacters.charAt(index));
        }
        return idBuilder.toString();
    }
}
