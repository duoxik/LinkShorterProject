package com.duoxik.java.aws.lambda.links.adder;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.lambda.runtime.Context;

public class LinkGetterHandler {

    private static final String URL_KEY = "url";
    private static final String ID_KEY = "id";
    private final static Table LINKS_TABLE;

    static {
        final AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard().build();
        final DynamoDB dynamoDB = new DynamoDB(client);
        LINKS_TABLE = dynamoDB.getTable("Links");
    }

    public String getUrlById(final String id, Context context) {
        if (id == null || id.isEmpty()) {
            return null;
        }

        final Item item = LINKS_TABLE.getItem(ID_KEY, id);
        final String url = item.getString(URL_KEY);

        if (url == null || url.isEmpty()) {
            return null;
        }
        return url;
    }
}
