package com.vectara.examples.rest;

import com.beust.jcommander.JCommander;
import com.vectara.auth.JwtFetcher;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * A class that demonstrates how different Vectara APIs such as Indexing, Serving and Admin can be
 * called using Rest and OAuth (for authentication)
 */
public class RestBasicOperations {
  public static void main(String[] argv) {
    RestArgs args = new RestArgs();
    JCommander.newBuilder().addObject(args).build().parse(argv);
    String jwtToken = getJwtToken(args.authUrl, args.appClientId, args.appClientSecret);
    if (jwtToken == null) {
      System.out.println("Could not obtain JWTToken. Please check your authentication variables "
          + "such as AUTH_URL, CLIENT_ID, CLIENT_SECRET");
      return;
    }
    // here is how you can programmatically create a new corpus
    RestCreateCorpus.createCorpus(jwtToken, args.adminEndpoint, "VectaraDemo", args.customerId);
    // now we have the corpus, we can index documents
    String docId = "mydocumentId1";
    String docTitle = "mydoc-title";
    String docText = "potentially very big content to index";
    RestIndex.indexDocument(jwtToken, args.indexingEndpoint, docTitle, docText, docId, args.customerId, args.corpusId);
    // we can also upload a file to be indexed.
    RestUploadFile.indexFile(jwtToken, args.indexingEndpoint, args.customerId, args.corpusId);
    // It takes some time to index data in Vectara platform. It is possible that query will
    // return zero results immediately after indexing. Please wait 3-5 minutes and try again if
    // that happens.
    RestQuery.queryData(jwtToken, args.servingEndpoint, "test", args.customerId, args.corpusId);
  }

  /**
   * Retrieves a JWT Token based on authUrl, ClientID and ClientSecret.
   */
  private static String getJwtToken(String authUrl, String clientId, String clientSecret) {
    try {
      JwtFetcher fetcher = new JwtFetcher(new URI(authUrl), clientId, clientSecret);
      String jwtToken = fetcher.fetchClientCredentialsJwt();
      if (jwtToken == null) {
        System.out.println("Could not obtain client credentials.");
        return null;
      }
      return jwtToken;
    } catch (URISyntaxException e) {
      System.out.println("Could not obtain client credentials.");
      return null;
    }
  }
}
