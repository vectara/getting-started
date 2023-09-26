package com.vectara.auth;

import com.google.gson.Gson;
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
import java.util.Base64;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nullable;

/**
 * A helper that retrieves a JSON Web Token from an authorization code grant. Most of the details
 * for how to format the HTTP request can be found at:
 *
 * <p>https://aws.amazon.com/blogs/mobile/understanding-amazon-cognito-user-pool-oauth-2-0-grants/
 */
public class JwtFetcher {
  private static final Logger LOGGER = Logger.getLogger(JwtFetcher.class.getName());

  private URI tokenEndpoint;
  private URI redirectUri;
  private String clientId;
  private String clientSecret;

  private HttpClient httpClient;

  /**
   * Construct a JWT fetcher for machine-to-machine authentication (also known as "client
   * credentials").
   */
  public JwtFetcher(URI authDomain, String clientId, String clientSecret) {
    init(authDomain, null, clientId, clientSecret);
  }

  /**
   * Initializes a new HTTPClient object
   * @param authDomain    Vectara auth domain such as https://vectara.auth.us-west-2.amazoncognito.com
   * @param redirectUri   Redirect URI where caller will be redirected after successful
   *                      authentication. Can be null.
   * @param clientId      Vectara client ID such as "259xxxxxxxxxxxxxxxxxxxxxxxxxx9p"
   * @param clientSecret  Vectara client secret such as "2vxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxt"
   */
  private void init(
      URI authDomain, @Nullable URI redirectUri, String clientId, String clientSecret) {
    String strAuthDomain = authDomain.toASCIIString();
    if (strAuthDomain.endsWith("/oauth2/token")) {
      tokenEndpoint = asUrl(strAuthDomain);
    } else {
      if (!strAuthDomain.endsWith("/")) {
        strAuthDomain += "/";
      }
      tokenEndpoint = asUrl(strAuthDomain + "oauth2/token");
    }
    this.redirectUri = redirectUri;
    this.clientId = clientId;
    this.clientSecret = clientSecret;
    httpClient =
        HttpClient.newBuilder()
            .version(Version.HTTP_2)
            .followRedirects(Redirect.NORMAL)
            .connectTimeout(Duration.ofSeconds(20))
            .build();
  }

  private URI asUrl(String url) {
    return URI.create(url);
  }

  public String fetchClientCredentialsJwt() {
    HttpRequest.Builder builder =
        HttpRequest.newBuilder()
            .uri(tokenEndpoint)
            .header("Content-Type", "application/x-www-form-urlencoded")
            .POST(
                BodyPublishers.ofString(
                    String.format(
                        "grant_type=%s&client_id=%s&redirect_uri=%s",
                        "client_credentials", clientId, redirectUri)));
    builder.header(
        "Authorization",
        "Basic " + Base64.getEncoder().encodeToString((clientId + ":" + clientSecret).getBytes()));
    HttpRequest request = builder.build();

    try {
      HttpResponse<String> response = httpClient.send(request, BodyHandlers.ofString());
      Map<String, Object> map = new Gson().fromJson(response.body(), Map.class);
      if (map.containsKey("error")) {
        LOGGER.log(
            Level.SEVERE,
            String.format(
                "Error while retrieving JWT Token: %s", String.valueOf(map.get("error"))));
        return null;
      }
      if (map.containsKey("access_token")) {
        return String.valueOf(map.get("access_token"));
      }
      return null;
    } catch (IOException | InterruptedException e) {
      LOGGER.log(Level.SEVERE, String.format("Error while retrieving JWT Token: %s", e));
      return null;
    }
  }
}
