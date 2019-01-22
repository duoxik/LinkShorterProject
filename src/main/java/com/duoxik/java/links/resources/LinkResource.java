package com.duoxik.java.links.resources;

import com.mongodb.MongoClient;
import com.mongodb.MongoWriteException;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Iterator;
import java.util.Random;

@Path("links")
public class LinkResource {

    private static final String URL_KEY = "url";
    private static final String ID_KEY = "id";
    private static final Response ANSWER_404 = Response.status(Response.Status.NOT_FOUND).build();
    private static final MongoCollection<Document> LINKS_COLLECTION;

    static {
        MongoClient mongoClient = new MongoClient("localhost", 27017);
        MongoDatabase db = mongoClient.getDatabase("JavaTestDB");
        LINKS_COLLECTION = db.getCollection("links");
    }

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("{id}")
    public Response getUrlById(final @PathParam(ID_KEY) String id) {
        if (id == null || id.isEmpty()) {
            return ANSWER_404;
        }

        final FindIterable<Document> resultsIterable = LINKS_COLLECTION.find(new Document(ID_KEY, id));
        Iterator<Document> resultIterator = resultsIterable.iterator();

        if(!resultIterator.hasNext()) {
            return ANSWER_404;
        }

        final String url = resultIterator.next().getString(URL_KEY);

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
            final Document newDoc = new Document(ID_KEY, id);
            newDoc.put(URL_KEY, url);

            try {
                LINKS_COLLECTION.insertOne(newDoc);
                return Response.ok(id).build();
            } catch (final MongoWriteException e) {
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
