package com.duoxik.java.aws.lambda.links.adder;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.spec.PutItemSpec;
import com.amazonaws.services.dynamodbv2.model.ConditionalCheckFailedException;
import com.amazonaws.services.lambda.runtime.Context;

import java.util.Random;

public class LinkAdderHandler {

    private static final String URL_KEY = "url";
    private static final String ID_KEY = "id";
    private final static Table LINKS_TABLE;

    static {
        final AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard().build();
        final DynamoDB dynamoDB = new DynamoDB(client);
        LINKS_TABLE = dynamoDB.getTable("Links");
    }

    public String addLink(final String url, Context context) {

        if (url == null || url == "")
            return null;
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
                return id;
            } catch (ConditionalCheckFailedException e) {
                // attemp to write failed. ID - exists
                // try again
            }
            attemp++;
        }
        return null;
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
