package com.vectara.examples.rest;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.Iterator;

public class RestQuery {

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
              .uri(URI.create(String.format("https://%s/v1/query", servingUrl)))
              .headers(
                  "Content-Type", "application/json", "customer-id", String.valueOf(customerId))
              .POST(BodyPublishers.ofString(queryJson));

      builder.header("Authorization", "Bearer " + jwtToken);
      HttpRequest request = builder.build();
      HttpResponse<String> response = RestUtil.newHttpClient().send(request, BodyHandlers.ofString());
      System.out.printf("Query response: %s", response.toString());
      JsonNode responseNode = new ObjectMapper().readTree(response.body());
      Iterator<JsonNode> responseSetArray = responseNode.get("responseSet").elements();
      while (responseSetArray.hasNext()) {
        JsonNode responseSet = responseSetArray.next();
        Iterator<JsonNode> docSections = responseSet.get("response").elements();
        JsonNode documents = responseSet.get("document"); // array of documents
        while (docSections.hasNext()) {
          JsonNode docSection = docSections.next();
          String matchingText = docSection.get("text").asText();
          double score = docSection.get("score").asDouble();
          int documentIndex = docSection.get("documentIndex").asInt();
          JsonNode doc = documents.get(documentIndex); // doc that this section belongs to
          String docId = doc.get("id").asText();
          System.out.printf("[score:%.4f] [docId:%s] [text:%s]%n", score, docId, matchingText);
        }
      }
      return true;
    } catch (Exception e) {
      e.printStackTrace();
      return false;
    }
  }
}
