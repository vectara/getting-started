package com.vectara.examples.rest;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.math.BigInteger;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;

public class RestUploadFile {
  
  /**
   * Indexes some dummy data to a pre-created corpus in a customer account.
   *
   * @param jwtToken A valid JWT token.
   * @param indexingUrl Indexing URL at which gRPC endpoints are available.
   * @param customerId The unique customer ID in the Vectara platform.
   * @param corpusId The unique corpus ID.
   * @return success or failure.
   */
  public static boolean indexFile(
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
              .uri(URI.create(String.format("https://%s/v1/upload", indexingUrl)))
              .header("Content-Type", "multipart/form-data;boundary=" + boundary)
              .POST(BodyPublisherHelper.ofMultipartData(data, boundary));
      builder.header("Authorization", "Bearer " + jwtToken);
      HttpRequest request = builder.build();
      HttpResponse<String> response = RestUtil.newHttpClient().send(request, BodyHandlers.ofString());
      System.out.printf("Index response: %s%n", response.toString());
      return true;
    } catch (Exception e) {
      e.printStackTrace();
      return false;
    }
  }

  private static String loadFileFromResources() {
    try {
      InputStream in = RestUploadFile.class.getResourceAsStream("/upload.pdf");
      File tempFile = File.createTempFile("temp", ".pdf");
      in.transferTo(new FileOutputStream(tempFile));
      return tempFile.getAbsolutePath();
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }

}
