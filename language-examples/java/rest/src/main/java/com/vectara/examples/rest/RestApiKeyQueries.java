package com.vectara.examples.rest;

import com.beust.jcommander.JCommander;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Redirect;
import java.net.http.HttpClient.Version;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.Duration;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A class that demonstrates how Vectara Serving API can be called using REST and an
 * API Key for authentication.
 */
public class RestApiKeyQueries {
  private static final Logger LOGGER = Logger.getLogger(RestBasicOperations.class.getName());

  public static void main(String[] argv) {
    RestArgs args = new RestArgs();
    JCommander.newBuilder().addObject(args).build().parse(argv);
    if (args.apiKey == null) {
      LOGGER.log(Level.SEVERE, "Please provide an API Key to run this example.");
      System.exit(1);
    }
    String apiKey = args.apiKey;

    var result = queryData(apiKey, args.servingEndpoint, args.query, args.customerId, args.corpusId);
    if (!result) {
      LOGGER.log(Level.SEVERE, "Querying failed. Please see previous logs for details.");
      System.exit(1);
    }
  }

  private static boolean queryData(String apiKey,
                                   String servingUrl,
                                   String query,
                                   Long customerId,
                                   Long corpusId) {
    try {
      String queryJson =
          String.format(
              "{\"query\":"
                  + "["
                    + "{"
                      + "\"query\":\"%s\","
                      + "\"numResults\":10,"
                      + "\"corpusKey\":"
                        + "["
                          + "{"
                            + "\"customerId\":%d,"
                              + "\"corpusId\":%d,"
                              + "\"dim\":[]"
                          + "}"
                        + "]"
                    + "}"
                  + "]"
              + "}",
              query, customerId, corpusId);

      var httpClient = HttpClient.newBuilder()
          .version(Version.HTTP_2)
          .followRedirects(Redirect.NORMAL)
          .connectTimeout(Duration.ofSeconds(20))
          .build();

      HttpRequest.Builder builder =
          HttpRequest.newBuilder()
              .uri(URI.create(String.format("https://%s/v1/query", servingUrl)))
              .headers(
                  "Content-Type", "application/json", "customer-id", String.valueOf(customerId))
              .POST(BodyPublishers.ofString(queryJson));

      builder.header("x-api-key", apiKey);
      HttpRequest request = builder.build();
      HttpResponse<String> response = httpClient.send(request, BodyHandlers.ofString());
      LOGGER.info(String.format("Querying response: %s", response.toString()));
      return true;
    } catch (IOException | InterruptedException e) {
      LOGGER.log(Level.SEVERE, String.format("Error while indexing data: %s", e));
      return false;
    }
  }
}
