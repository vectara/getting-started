package com.vectara.examples.rest;

import java.io.IOException;
import java.net.http.HttpRequest;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class BodyPublisherHelper {
  /**
   * Java 11 HttpClient doesn't have helper methods for Multipart form data. We need multipart form
   * data support for indexing data when we are sending files along with params such as customer ID
   * and corpus ID. The following method does this and is taken from:
   * <a href="https://stackoverflow.com/a/56482187/30341">Stackoverflow</a>
   *
   * @param data A Hashmap containing the parameters to be passed including files (if any)
   * @param boundary A boundary generated randomly based on which parts will be separated.
   * @return A BodyPublisher object
   */
  static HttpRequest.BodyPublisher ofMultipartData(Map<Object, Object> data, String boundary)
      throws IOException {
    // Result request body
    List<byte[]> byteArrays = new ArrayList<>();

    // Separator with boundary
    byte[] separator =
        ("--" + boundary + "\r\nContent-Disposition: form-data; name=")
            .getBytes(StandardCharsets.UTF_8);

    // Iterating over data parts
    for (Map.Entry<Object, Object> entry : data.entrySet()) {
      // Opening boundary
      byteArrays.add(separator);
      // If value is type of Path (file) append content type with file name and file binaries,
      // otherwise simply append key=value
      if (entry.getValue() instanceof Path) {
        Path path = (Path) entry.getValue();
        String mimeType = Files.probeContentType(path);
        byteArrays.add(
            ("\""
                    + entry.getKey()
                    + "\"; filename=\""
                    + path.getFileName()
                    + "\"\r\nContent-Type: "
                    + mimeType
                    + "\r\n\r\n")
                .getBytes(StandardCharsets.UTF_8));
        byteArrays.add(Files.readAllBytes(path));
        byteArrays.add("\r\n".getBytes(StandardCharsets.UTF_8));
      } else {
        byteArrays.add(
            ("\"" + entry.getKey() + "\"\r\n\r\n" + entry.getValue() + "\r\n")
                .getBytes(StandardCharsets.UTF_8));
      }
    }
    // Closing boundary
    byteArrays.add(("--" + boundary + "--").getBytes(StandardCharsets.UTF_8));
    // Serializing as byte array
    return HttpRequest.BodyPublishers.ofByteArrays(byteArrays);
  }
}
