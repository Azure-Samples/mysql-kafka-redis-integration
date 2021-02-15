package com.demo.azure;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.converter.ConversionException;
import org.springframework.stereotype.Service;

import io.redisearch.Schema;
import io.redisearch.client.Client;
import io.redisearch.client.IndexDefinition;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

@Service
public class Consumer {

    static String INDEX_PREFIX = "product:";
    static String INDEX_NAME = "search-index";

    // Schema attributes
    static String SCHEMA_FIELD_ID = "id";
    static String SCHEMA_FIELD_NAME = "name";
    static String SCHEMA_FIELD_CREATED = "created";
    static String SCHEMA_FIELD_DESCRIPTION = "description";
    static String SCHEMA_FIELD_BRAND = "brand";
    static String SCHEMA_FIELD_TAGS = "tags";
    static String SCHEMA_FIELD_CATEGORIES = "categories";

    // Document (Redis HASH) attributes
    static String DOC_FIELD_ID = "id";
    static String DOC_FIELD_NAME = "name";
    static String DOC_FIELD_CREATED = "created";
    static String DOC_FIELD_DESCRIPTION = "description";
    static String DOC_FIELD_BRAND = "brand";
    static String DOC_FIELD_TAGS = "tags";
    static String DOC_FIELD_CATEGORIES = "categories";

    private Client client;

    @Value("${redis.host}")
    private String redisHost;

    @Value("${redis.port}")
    private Integer port;

    @Value("${redis.password}")
    private String redisPassword;

    @PostConstruct
    public void init() {
        System.out.println("Init RediSearch schema and index");

        int timeout = 2000;
        boolean ssl = true;

        GenericObjectPoolConfig<Jedis> jedisPoolConfig = new GenericObjectPoolConfig<>();
        JedisPool jedisPool = new JedisPool(jedisPoolConfig, redisHost, port, timeout, redisPassword, ssl);

        client = new Client(INDEX_NAME, jedisPool);
        Schema sc = new Schema().addTextField(SCHEMA_FIELD_ID, 1.0).addTextField(SCHEMA_FIELD_NAME, 1.0)
                .addNumericField(SCHEMA_FIELD_CREATED).addTextField(SCHEMA_FIELD_DESCRIPTION, 1.0)
                .addTextField(SCHEMA_FIELD_BRAND, 1.0).addTagField(SCHEMA_FIELD_TAGS)
                .addTagField(SCHEMA_FIELD_CATEGORIES);

        IndexDefinition def = new IndexDefinition().setPrefixes(new String[] { INDEX_PREFIX });

        /*
         * boolean dropped = client.dropIndex(true); if (dropped) {
         * System.out.println("DROPPED rediSearch index"); }
         */

        try {
            boolean indexCreated = client.createIndex(sc, Client.IndexOptions.defaultOptions().setDefinition(def));

            if (indexCreated) {
                System.out.println("Created RediSearch index ");
            }
        } catch (Exception e) {
            System.out.println("Did not create RediSearch index. It probably exists");
        }
    }

    static ObjectMapper om = new ObjectMapper();

    @KafkaListener(topics = "${topic.name}", groupId = "${spring.kafka.consumer.group-id}")
    public void consume(ConsumerRecord<String, String> record) {
        Product product = null;
        ProductAttributes productDetails = null;
        try {
            product = om.readValue(record.value(), Product.class);
            System.out.println("Converted change event to POJO - " + product);

            String details = product.getProductDetails();
            productDetails = om.readValue(details, ProductAttributes.class);
            System.out.println("Product Details - " + productDetails);

        } catch (Exception e) {
            throw new ConversionException("JSON conversion failed", e);
        }

        Map<String, String> info = new HashMap<>();
        info.put(DOC_FIELD_ID, String.valueOf(product.getProductId()));
        info.put(DOC_FIELD_NAME, product.getProductName());
        info.put(DOC_FIELD_CREATED, String.valueOf(product.getCreatedAt().getTime()));
        info.put(DOC_FIELD_DESCRIPTION, productDetails.getDescriptionn());
        info.put(DOC_FIELD_BRAND, productDetails.getBrand());
        info.put(DOC_FIELD_TAGS, String.join(",", productDetails.getTags()));
        info.put(DOC_FIELD_CATEGORIES, String.join(",", productDetails.getCategories()));

        String hashName = INDEX_PREFIX + product.getProductId();

        client.connection().hset(hashName, info);
        System.out.println("Added document " + hashName);
    }

    @PreDestroy
    public void close() {
        client.close();
        System.out.println("Closed RediSearch client");
    }

}
