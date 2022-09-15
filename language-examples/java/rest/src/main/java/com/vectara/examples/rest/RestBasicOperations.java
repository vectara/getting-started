package com.vectara.examples.rest;

import com.beust.jcommander.JCommander;
import com.vectara.auth.JwtFetcher;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Redirect;
import java.net.http.HttpClient.Version;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A class that demonstrates how different Vectara APIs such as Indexing, Serving and Admin can be
 * called using Rest and OAuth (for authentication)
 */
public class RestBasicOperations {
  private static final Logger LOGGER = Logger.getLogger(RestBasicOperations.class.getName());

  public static void main(String[] argv) {
    RestArgs args = new RestArgs();
    JCommander.newBuilder().addObject(args).build().parse(argv);
    String jwtToken = getJwtToken(args.authUrl, args.appClientId, args.appClientSecret);
    if (jwtToken == null) {
      LOGGER.log(
          Level.SEVERE,
          "Could not obtain JWTToken. Please check your authentication variables "
              + "such as AUTH_URL, CLIENT_ID, CLIENT_SECRET");
      return;
    }

    boolean result = indexData(jwtToken, args.indexingEndpoint, args.customerId, args.corpusId);
    if (!result) {
      LOGGER.log(Level.SEVERE, "Indexing failed. Please see previous logs for details.");
      System.exit(1);
    }

    // It takes some time to index data in Vectara platform. It is possible that query will
    // return zero results immediately after indexing. Please wait 3-5 minutes and try again if
    // that happens.
    result = queryData(jwtToken, args.servingEndpoint, "test", args.customerId, args.corpusId);
    if (!result) {
      LOGGER.log(Level.SEVERE, "Querying failed. Please see previous logs for details.");
      System.exit(1);
    }
    result = createCorpus(jwtToken, args.adminEndpoint, "VectaraDemo", args.customerId);
    if (!result) {
      LOGGER.log(Level.SEVERE, "Create Corpus failed. Please see previous logs for details.");
      System.exit(1);
    }
  }

  /** Retrieves a JWT Token based on authUrl, ClientID and ClientSecret. */
  private static String getJwtToken(String authUrl, String clientId, String clientSecret) {
    try {
      JwtFetcher fetcher = new JwtFetcher(new URI(authUrl), clientId, clientSecret);
      String jwtToken = fetcher.fetchClientCredentialsJwt();
      if (jwtToken == null) {
        LOGGER.log(Level.SEVERE, "Could not obtain client credentials.");
        return null;
      }
      return jwtToken;
    } catch (URISyntaxException e) {
      LOGGER.log(Level.SEVERE, "Could not obtain client credentials.");
      return null;
    }
  }

  private static String loadFileFromResources() {
    try {
      InputStream in = RestBasicOperations.class.getResourceAsStream("/upload.pdf");
      File tempFile = File.createTempFile("temp", ".pdf");
      in.transferTo(new FileOutputStream(tempFile));
      return tempFile.getAbsolutePath();
    } catch (NullPointerException | IOException exception) {
      LOGGER.log(Level.SEVERE, "Could not read file from resources.");
      return null;
    }
  }

  /** Returns a default HTTP_2 client with a timeout of 20 seconds. */
  private static HttpClient httpClient() {
    return HttpClient.newBuilder()
        .version(Version.HTTP_2)
        .followRedirects(Redirect.NORMAL)
        .connectTimeout(Duration.ofSeconds(20))
        .build();
  }

  /**
   * Indexes some dummy data to a pre-created corpus in a customer account.
   *
   * @param jwtToken A valid JWT token.
   * @param indexingUrl Indexing URL at which gRPC endpoints are available.
   * @param customerId The unique customer ID in the Vectara platform.
   * @param corpusId The unique corpus ID.
   * @return success or failure.
   */
  public static boolean indexData(
      String jwtToken, String indexingUrl, long customerId, long corpusId) {

    Map<Object, Object> data = new LinkedHashMap<>();
    data.put("c", customerId);
    data.put("o", corpusId);
    String filePath = loadFileFromResources();
    if (filePath == null) {
      return false;
    }
    data.put("file", Paths.get(filePath));

    // Random 256 length string is used as multipart boundary
    String boundary = new BigInteger(256, new Random()).toString();
    try {
      HttpRequest.Builder builder =
          HttpRequest.newBuilder()
              .uri(URI.create(String.format("https://h.%s/upload", indexingUrl)))
              .header("Content-Type", "multipart/form-data;boundary=" + boundary)
              .POST(BodyPublisherHelper.ofMultipartData(data, boundary));
      builder.header("Authorization", "Bearer " + jwtToken);
      HttpRequest request = builder.build();
      HttpResponse<String> response = httpClient().send(request, BodyHandlers.ofString());
      LOGGER.info(String.format("Indexing response: %s", response.toString()));
      return true;
    } catch (IOException | InterruptedException e) {
      LOGGER.log(Level.SEVERE, String.format("Error while indexing data: %s", e));
      return false;
    }
  }

  /**
   * Queries from Vectara Serving platform.
   *
   * @param jwtToken A valid JWT token.
   * @param servingUrl Serving URL at which gRPC endpoints are available.
   * @param query The query text.
   * @param customerId The unique customer ID in the Vectara platform.
   * @param corpusId The unique corpus ID.
   * @return success or failure.
   */
  public static boolean queryData(
      String jwtToken, String servingUrl, String query, long customerId, long corpusId) {
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

      HttpRequest.Builder builder =
          HttpRequest.newBuilder()
              .uri(URI.create(String.format("https://h.%s/v1/query", servingUrl)))
              .headers(
                  "Content-Type", "application/json", "customer-id", String.valueOf(customerId))
              .POST(BodyPublishers.ofString(queryJson));

      builder.header("Authorization", "Bearer " + jwtToken);
      HttpRequest request = builder.build();
      HttpResponse<String> response = httpClient().send(request, BodyHandlers.ofString());
      LOGGER.info(String.format("Querying response: %s", response.toString()));
      return true;
    } catch (IOException | InterruptedException e) {
      LOGGER.log(Level.SEVERE, String.format("Error while indexing data: %s", e));
      return false;
    }
  }

  /**
   * Calls Vectara Admin platform to create a corpus.
   *
   * @param jwtToken A valid JWT token.
   * @param adminUrl Admin URL at which gRPC endpoints are available.
   * @param corpusName The name of the corpus to be created.
   * @param customerId The unique customer ID in Vectara platform.
   * @return success or failure.
   */
  public static boolean createCorpus(
      String jwtToken, String adminUrl, String corpusName, long customerId) {
    String corpusJson =
        "{"
          + "\"corpus\":"
            + "{"
              + "\"name\":\"vectara-rest-demo-corpus\","
              + "\"description\":\"Dummy description\""
            + "}"
        + "}";
    try {
      HttpRequest.Builder builder =
          HttpRequest.newBuilder()
              .uri(URI.create(String.format("https://h.%s/v1/create-corpus", adminUrl)))
              .headers(
                  "Content-Type", "application/json", "customer-id", String.valueOf(customerId))
              .POST(BodyPublishers.ofString(corpusJson));

      builder.header("Authorization", "Bearer " + jwtToken);
      HttpRequest request = builder.build();
      HttpResponse<String> response = httpClient().send(request, BodyHandlers.ofString());
      LOGGER.info(String.format("Create Corpus response: %s", response.toString()));
      return true;
    } catch (IOException | InterruptedException e) {
      LOGGER.log(Level.SEVERE, String.format("Error while indexing data: %s", e));
      return false;
    }
  }
}
