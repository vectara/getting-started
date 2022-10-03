package com.vectara.examples.rest;

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;

public class RestCreateCorpus {

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
              + "\"name\":\"" + corpusName + "\","
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
      HttpResponse<String> response = RestUtil.newHttpClient().send(request, BodyHandlers.ofString());
      /**
       * Here is how a successful JSON response sample:
       * {
       *   "corpusId": 1,
       *   "status": {
       *     "code": "OK",
       *     "statusDetail": "Corpus Created"
       *   }
       * }
       */
      System.out.printf("Create Corpus response: %s", response.toString());
      return true;
    } catch (Exception e) {
      e.printStackTrace();
      return false;
    }
  }
}
