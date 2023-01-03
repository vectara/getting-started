package com.vectara.examples.rest;

import com.beust.jcommander.Parameter;
import javax.annotation.Nullable;

public class RestArgs {
  @Parameter(
      names = {"--customer-id"},
      description = "Unique customer ID in Vectara platform.",
      required = true)
  Long customerId = 1890073338L;

  @Parameter(
      names = {"--corpus-id"},
      description = "Corpus ID against which examples need to be run.",
      required = true)
  Long corpusId = 321L;

  @Parameter(
      names = {"--admin-endpoint"},
      description = "Admin endpoint such as api.vectara.io")
  String adminEndpoint = "api.vectara.io";

  @Parameter(
      names = {"--serving-endpoint"},
      description = "Serving endpoint such as api.vectara.io")
  String servingEndpoint = "api.vectara.io";

  @Parameter(
      names = {"--indexing-endpoint"},
      description = "Indexing endpoint such as api.vectara.io")
  String indexingEndpoint = "api.vectara.io";

  // Following args are required to obtain JWT Token.
  @Parameter(
      names = {"--auth-url"},
      description = "Authentication URL such as https://vectara-prod-{_CUSTOMER_ID}.auth.us-west-2.amazoncognito.com")
  String authUrl = "";

  @Parameter(
      names = {"--app-client-id"},
      description = "App Client ID retrieved from Vectara console.")
  String appClientId;

  @Parameter(
      names = {"--app-client-secret"},
      description = "App client secret retrieved from Vectara console.")
  String appClientSecret;

  @Parameter(
      names= {"--api-key"},
      description = "API key retrieved from Vectara console")
  @Nullable
  String apiKey = null;
}
