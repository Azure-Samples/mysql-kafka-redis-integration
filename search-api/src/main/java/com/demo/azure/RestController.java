package com.demo.azure;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import io.redisearch.Document;
import io.redisearch.Query;
import io.redisearch.SearchResult;
import io.redisearch.client.Client;
import redis.clients.jedis.JedisPool;

@Controller
@RequestMapping(path = "/search")
public class RestController {

    private Client client;

    @Value("${redis.host}")
    private String redisHost;

    @Value("${redis.port}")
    private Integer port;

    @Value("${redis.password}")
    private String redisPassword;

    static String INDEX_NAME = "search-index";

    @PostConstruct
    public void init() {
        System.out.println("Connecting to Redis");

        int timeout = 2000;
        boolean ssl = true;

        GenericObjectPoolConfig jedisPoolConfig = new GenericObjectPoolConfig();
        JedisPool jedisPool = new JedisPool(jedisPoolConfig, redisHost, port, timeout, redisPassword, ssl);

        this.client = new Client(INDEX_NAME, jedisPool);
    }

    @GetMapping()
    public @ResponseBody List<Map<String, Object>> getUser(@RequestParam(name = "q") String queryString,
            @RequestParam(name = "fields", required = false) String fields) {
        System.out.println("Executing query " + queryString);
        // System.out.println("Fields " + fields);

        Query q = new Query(queryString).limit(0, 1000);
        if (fields != null) {
            q.returnFields(fields.split(","));
        }

        List<Map<String, Object>> result = new ArrayList<>();

        SearchResult res = client.search(q);
        for (Document doc : res.docs) {
            Map<String, Object> info = new HashMap<>();

            for (Entry<String, Object> e : doc.getProperties()) {
                info.put(e.getKey(), e.getValue());
            }

            result.add(info);
        }
        return result;
    }

    @PreDestroy
    public void close() {
        client.close();
        System.out.println("Closed connection to Redis");
    }
}