package com.vectara.examples.rest;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.*;

public class RestIndex {
  /**
   * Indexes a document with a single section and a title.
   * <p>
   * Document can have one or more sections. Sections can be nested.
   * Title is not required but if provided, it will also get indexed.
   *
   * @param jwtToken    A valid JWT token.
   * @param indexingUrl Indexing URL at which gRPC endpoints are available.
   * @param docTitle    document title
   * @param docText     document text to index
   * @param docId       document id
   * @param customerId  The unique customer ID in the Vectara platform.
   * @param corpusId    The unique corpus ID.
   * @return success or failure.
   */
  public static boolean indexDocument(
      String jwtToken, String indexingUrl, String docTitle, String docText, String docId, long customerId, long corpusId) {
    try {
      ObjectMapper mapper = new ObjectMapper();
      Map<String, Object> writeRequest = new HashMap<>();
      writeRequest.put("customerId", customerId);
      writeRequest.put("corpusId", corpusId);
      Map<String, Object> doc = new HashMap<>();
      doc.put("documentId", docId);
      doc.put("title", docTitle); // optional
      // let's add some metadata to our document (optional)
      Map<String, String> metadata = new HashMap<>();
      metadata.put("author", "vectara");
      metadata.put("date", new Date().toString());
      // metadata has to be added as a json text
      doc.put("metadataJson", mapper.writer().writeValueAsString(metadata)); // optional
      List<Map<String, Object>> section = new LinkedList<>();
      Map<String, Object> singleSection = new HashMap<>();
      singleSection.put("text", docText);
      // each section can have its own metadata also
      section.add(singleSection);
      doc.put("section", section);
      writeRequest.put("document", doc);
      String indexJsonRequest = mapper.writer().writeValueAsString(writeRequest);
      System.out.println(indexJsonRequest);
      HttpRequest.Builder builder = HttpRequest.newBuilder()
          .uri(URI.create(String.format("https://h.%s/v1/index", indexingUrl)))
          .headers("Content-Type", "application/json", "customer-id", String.valueOf(customerId))
          .POST(HttpRequest.BodyPublishers.ofString(indexJsonRequest));
      builder.header("Authorization", "Bearer " + jwtToken);
      HttpRequest httpRequest = builder.build();
      HttpResponse<String> response = RestUtil.newHttpClient().send(httpRequest, BodyHandlers.ofString());
      System.out.printf("Index response: %s%n", response.toString());
      JsonNode responseNode = new ObjectMapper().readTree(response.body());
      JsonNode status = responseNode.get("status");
      String statusCode = status.get("code").asText();
      System.out.println(statusCode);
      return "OK".equals(statusCode);
    } catch (Exception e) {
      e.printStackTrace();
      return false;
    }
  }
}
