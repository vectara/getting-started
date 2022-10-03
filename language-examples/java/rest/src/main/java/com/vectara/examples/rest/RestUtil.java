package com.vectara.examples.rest;

import java.net.http.HttpClient;
import java.time.Duration;

public class RestUtil {
    /**
     * Creates and returns a default HTTP_2 client with a timeout of 20 seconds.
     * @return  HTTP_2 client
     */
    public static HttpClient newHttpClient() {
        return HttpClient.newBuilder()
                         .version(HttpClient.Version.HTTP_2)
                         .followRedirects(HttpClient.Redirect.NORMAL)
                         .connectTimeout(Duration.ofSeconds(20))
                         .build();
    }
}
