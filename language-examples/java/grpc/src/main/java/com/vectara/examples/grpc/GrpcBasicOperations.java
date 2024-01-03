package com.vectara.examples.grpc;

import com.beust.jcommander.ParameterException;
import com.google.common.base.Strings;
import com.vectara.AdminServiceGrpc;
import com.vectara.AdminServiceGrpc.AdminServiceBlockingStub;
import com.vectara.CommonProtos.DeleteDocumentRequest;
import com.vectara.CommonProtos.DeleteDocumentResponse;
import com.vectara.IndexServiceGrpc;
import com.vectara.IndexServiceGrpc.IndexServiceBlockingStub;
import com.vectara.QueryServiceGrpc;
import com.vectara.QueryServiceGrpc.QueryServiceBlockingStub;
import com.vectara.ServiceProtos.IndexDocumentRequest;
import com.vectara.ServiceProtos.IndexDocumentResponse;
import com.vectara.StatusProtos.StatusCode;
import com.vectara.admin.AdminProtos.Corpus;
import com.vectara.admin.AdminProtos.CreateCorpusRequest;
import com.vectara.admin.AdminProtos.CreateCorpusResponse;
import com.vectara.admin.AdminProtos.DeleteCorpusRequest;
import com.vectara.admin.AdminProtos.DeleteCorpusResponse;
import com.vectara.indexing.IndexingProtos.Document;
import com.vectara.indexing.IndexingProtos.Section;
import com.vectara.serving.ServingProtos.BatchQueryRequest;
import com.vectara.serving.ServingProtos.BatchQueryResponse;
import com.vectara.serving.ServingProtos.CorpusKey;
import com.vectara.serving.ServingProtos.QueryRequest;
import com.beust.jcommander.JCommander;
import com.vectara.auth.JwtFetcher;
import com.vectara.auth.VectaraCallCredentials;
import com.vectara.auth.VectaraCallCredentials.AuthType;
import com.vectara.serving.ServingProtos.QueryRequest.ContextConfig;
import io.grpc.ManagedChannel;
import io.grpc.StatusRuntimeException;
import io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.NettyChannelBuilder;
import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.net.ssl.SSLException;

/**
 * A class that demonstrates how different Vectara APIs such as Indexing, Serving and Admin can be
 * called using gRPC and OAuth (for authentication).
 */
public class GrpcBasicOperations {
  private static final Logger LOGGER = Logger.getLogger(GrpcBasicOperations.class.getName());

  public static void main(String[] argv) {
    GrpcArgs args = new GrpcArgs();
    try {
      JCommander
          .newBuilder()
          .programName(GrpcBasicOperations.class.getName())
          .addObject(args)
          .build()
          .parse(argv);
      if (Strings.isNullOrEmpty(args.authUrl)) {
        args.authUrl = defaultProdAuthUrl(args.customerId);
        LOGGER.info("Computed authentication URL: " + args.authUrl);
      }
    } catch (ParameterException e) {
      e.usage();
      System.err.println(e.getMessage());
      System.exit(1);
    }
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

    result = deleteDocument(jwtToken, args.indexingEndpoint, args.customerId, args.corpusId, "doc-id-1");
    if (!result) {
      LOGGER.log(Level.SEVERE, "Delete document failed. Please see previous logs for details.");
      System.exit(1);
    }

    // It takes some time to index data in Vectara platform. It is possible that query will
    // return zero results immediately after indexing. Please wait 3-5 minutes and try again if
    // that happens.
    result = queryData(
        jwtToken, args.servingEndpoint, "How long ago was it?", args.customerId, args.corpusId);
    if (!result) {
      LOGGER.log(Level.SEVERE, "Querying failed. Please see previous logs for details.");
      System.exit(1);
    }

    long demoCorpusId = createCorpus(jwtToken, args.adminEndpoint, "VectaraDemo", args.customerId);
    if (demoCorpusId < 0) {
      LOGGER.log(Level.SEVERE, "Create Corpus failed. Please see previous logs for details.");
      System.exit(1);
    }
    LOGGER.info("Deleting demo corpus " + demoCorpusId);
    result = deleteCorpus(jwtToken, args.adminEndpoint, demoCorpusId, args.customerId);
    if (!result) {
      LOGGER.severe("Corpus deletion failed. Please see previous logs for details.");
    }
  }

  /**
   * Compute the OAuth 2.0 authentication URL for production for the
   * given customer.
   */
  private static String defaultProdAuthUrl(Long customerId) {
    return String.format(
        "https://vectara-prod-%d.auth.us-west-2.amazoncognito.com", customerId);
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
    } catch (URISyntaxException | RuntimeException e) {
      LOGGER.log(Level.SEVERE, "Could not obtain client credential: " + e);
      return null;
    }
  }

  private static ManagedChannel managedChannel(String url) throws SSLException {
    return NettyChannelBuilder.forAddress(url, 443)
        .sslContext(GrpcSslContexts.forClient().trustManager((File) null).build())
        .build();
  }

  /**
   * Returns a sample document for indexing.
   */
  private static IndexDocumentRequest getGettysburgAddress(long customerId, long corpusId) {
    return IndexDocumentRequest.newBuilder()
        .setCorpusId(corpusId)
        .setCustomerId(customerId)
        .setDocument(
            Document.newBuilder()
                .setDocumentId("gettysburg-address")
                .setTitle("Address Delivered at the Dedication of the Cemetery at Gettysburg")
                .setDescription(
                    "The Gettysburg Address is a speech that U.S. President Abraham Lincoln "
                        + "delivered during the American Civil War at the dedication of the "
                        + "Soldiers' National Cemetery, now known as Gettysburg National Cemetery, "
                        + "in Gettysburg, Pennsylvania on the afternoon of November 19, 1863, "
                        + "four and a half months after the Union armies defeated Confederate "
                        + "forces in the Battle of Gettysburg, the Civil War's deadliest battle."
                )
                .setMetadataJson("{\"author\":\"Abraham Lincoln\", \"delivered\":\"1863-Nov-19\"}")
                .addSection(
                    Section.newBuilder()
                        .setId(1)
                        .setText(
                            " Four score and seven years ago our fathers brought forth on this "
                                + "continent, a new nation, conceived in Liberty, and dedicated "
                                + "to the proposition that all men are created equal.\n"
                                + "\n"
                                + "Now we are engaged in a great civil war, testing whether that "
                                + "nation, or any nation so conceived and dedicated, can long "
                                + "endure. We are met on a great battle-field of that war. We have "
                                + "come to dedicate a portion of that field, as a final resting "
                                + "place for those who here gave their lives that that nation "
                                + "might live. It is altogether fitting and proper that we should "
                                + "do this.\n"
                                + "\n"
                                + "But, in a larger sense, we can not dedicate -- we can not "
                                + "consecrate -- we can not hallow -- this ground. The brave men, "
                                + "living and dead, who struggled here, have consecrated it, far "
                                + "above our poor power to add or detract. The world will little "
                                + "note, nor long remember what we say here, but it can never "
                                + "forget what they did here. It is for us the living, rather, to "
                                + "be dedicated here to the unfinished work which they who fought "
                                + "here have thus far so nobly advanced. It is rather for us to be "
                                + "here dedicated to the great task remaining before us -- that "
                                + "from these honored dead we take increased devotion to that "
                                + "cause for which they gave the last full measure of devotion -- "
                                + "that we here highly resolve that these dead shall not have died "
                                + "in vain -- that this nation, under God, shall have a new birth "
                                + "of freedom -- and that government of the people, by the people, "
                                + "for the people, shall not perish from the earth. "
        ))).build();
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
    ManagedChannel channel = null;
    try {
      channel = managedChannel(indexingUrl);
      IndexServiceBlockingStub indexing =
          IndexServiceGrpc.newBlockingStub(channel);
      var indexingRequest = getGettysburgAddress(customerId, corpusId);
      LOGGER.info("Indexing " + indexingRequest.getDocument().getDocumentId() + " in corpus " + corpusId);
      IndexDocumentResponse response =
          indexing
              .withCallCredentials(new VectaraCallCredentials(AuthType.OAUTH_TOKEN,
                                                              jwtToken,
                                                              customerId,
                                                              corpusId))
              .index(indexingRequest);
      switch (response.getStatus().getCode()) {
        case OK -> LOGGER.info("Indexing succeeded");
        case ALREADY_EXISTS -> LOGGER.info("Document was previously indexed");
        default -> {
          LOGGER.severe("Indexing failed: " + response);
          return false;
        }
      }
      return true;
    } catch (SSLException | StatusRuntimeException e) {
      LOGGER.log(Level.SEVERE, String.format("Error while indexing data: %s", e));
      return false;
    } finally {
      if (channel != null) {
        channel.shutdown();
      }
    }
  }

  /**
   * Deletes a document from a corpus in a customer account.
   *
   * @param jwtToken A valid JWT token.
   * @param indexingUrl Indexing URL at which gRPC endpoints are available.
   * @param customerId The unique customer ID in the Vectara platform.
   * @param corpusId The unique corpus ID.
   * @param documentId The ID of the document to be deleted.
   * @return success or failure.
   */
  public static boolean deleteDocument(
      String jwtToken, String indexingUrl, long customerId, long corpusId, String documentId) {
    ManagedChannel channel = null;
    try {
      channel = managedChannel(indexingUrl);
      IndexServiceBlockingStub indexing =
          IndexServiceGrpc.newBlockingStub(channel);

      DeleteDocumentRequest request =
          DeleteDocumentRequest.newBuilder()
              .setCustomerId(customerId)
              .setCorpusId(corpusId)
              .setDocumentId(documentId)
              .build();
      DeleteDocumentResponse response =
          indexing
              .withCallCredentials(new VectaraCallCredentials(AuthType.OAUTH_TOKEN,
                                                              jwtToken,
                                                              customerId,
                                                              corpusId))
              .delete(request);
      LOGGER.info("Document deletion request submitted for " + documentId + " in corpus " + corpusId);
      return true;
    } catch (SSLException | StatusRuntimeException e) {
      LOGGER.log(Level.SEVERE, String.format("Error while deleting document: %s", e));
      return false;
    } finally {
      if (channel != null) {
        channel.shutdown();
      }
    }
  }

  /**
   * Queries a Vectara corpus.
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
    ManagedChannel channel = null;
    try {
      channel = managedChannel(servingUrl);
      QueryServiceBlockingStub querying =
          QueryServiceGrpc.newBlockingStub(channel);
      BatchQueryRequest.Builder builder = BatchQueryRequest.newBuilder();
      builder.addQuery(
          QueryRequest.newBuilder()
              .setQuery(query)
              .setNumResults(10)
              .addCorpusKey(
                  CorpusKey.newBuilder()
                      .setCorpusId((int) corpusId)
                      .setCustomerId((int) customerId)
                      .build())
              .build());

      BatchQueryResponse response =
          querying
              .withCallCredentials(new VectaraCallCredentials(AuthType.OAUTH_TOKEN,
                                                              jwtToken,
                                                              customerId,
                                                              corpusId))
              .query(builder.build());
      boolean queryFailure = false;
      for (var status : response.getStatusList()) {
        if (status.getCode() != StatusCode.OK) {
          LOGGER.warning("Failure status on query: " + status);
          queryFailure = true;
        }
      }
      for (var responseSet : response.getResponseSetList()) {
        for (var status : responseSet.getStatusList()) {
          if (status.getCode() != StatusCode.OK) {
            LOGGER.warning("Failure querying corpus: " + status);
            queryFailure = true;
          }
        }
      }
      LOGGER.info(
          String.format("Query <%s> response:\n%s", query, response.toString()));
      return !queryFailure;
    } catch (SSLException | StatusRuntimeException e) {
      LOGGER.log(Level.SEVERE, String.format("Error while querying data: %s", e));
      return false;
    } finally {
      if (channel != null) {
        channel.shutdown();
      }
    }
  }

  /**
   * Calls Vectara Admin platform to create a corpus.
   *
   * @param jwtToken A valid JWT token.
   * @param adminUrl Admin URL at which gRPC endpoints are available.
   * @param corpusName The name of the corpus to be created.
   * @param customerId The unique customer ID in Vectara platform.
   * @return The ID of the new corpus, or -1 on failure.
   */
  public static long createCorpus(
      String jwtToken, String adminUrl, String corpusName, long customerId) {
    ManagedChannel channel = null;
    try {
      channel = managedChannel(adminUrl);
      AdminServiceBlockingStub admin = AdminServiceGrpc.newBlockingStub(channel);
      CreateCorpusRequest.Builder builder = CreateCorpusRequest.newBuilder();
      builder.setCorpus(
          Corpus.newBuilder()
              .setName(corpusName)
              .setDescription("Test Corpus")
              .build());

      CreateCorpusResponse response =
          admin
              .withCallCredentials(new VectaraCallCredentials(AuthType.OAUTH_TOKEN,
                                                              jwtToken,
                                                              customerId))
              .createCorpus(builder.build());
      if (response.getStatus().getCode() != StatusCode.OK) {
        LOGGER.severe("Corpus creation failed for " + corpusName + ": " + response.getStatus());
        return -1;
      }
      LOGGER.info("Corpus " + corpusName + " created with id " + response.getCorpusId());
      return Integer.toUnsignedLong(response.getCorpusId());
    } catch (SSLException | StatusRuntimeException e) {
      LOGGER.log(Level.SEVERE, String.format("Error while creating a corpus: %s", e));
      return -1;
    } finally {
      if (channel != null) {
        channel.shutdown();
      }
    }
  }

  /**
   * Deletes a corpus.
   *
   * @param jwtToken A valid JWT token.
   * @param adminUrl Admin URL at which gRPC endpoints are available.
   * @param corpusId The id of the corpus to be deleted.
   * @param customerId The unique customer ID in Vectara platform.
   * @return success or failure.
   */
  public static boolean deleteCorpus(
      String jwtToken, String adminUrl, long corpusId, long customerId) {
    ManagedChannel channel = null;
    try {
      channel = managedChannel(adminUrl);
      AdminServiceBlockingStub admin = AdminServiceGrpc.newBlockingStub(channel);
      DeleteCorpusRequest.Builder builder = DeleteCorpusRequest.newBuilder()
          .setCorpusId((int) corpusId)
          .setCustomerId((int) customerId);

      DeleteCorpusResponse response =
          admin
              .withCallCredentials(new VectaraCallCredentials(AuthType.OAUTH_TOKEN,
                                                              jwtToken,
                                                              customerId))
              .deleteCorpus(builder.build());
      if (response.getStatus().getCode() != StatusCode.OK) {
        LOGGER.severe("Corpus deletion failed for corpus " + corpusId + ": " + response.getStatus());
        return false;
      }
      LOGGER.info("Corpus " + corpusId + " deleted");
      return true;
    } catch (SSLException | StatusRuntimeException e) {
      LOGGER.log(Level.SEVERE, String.format("Error while deleting a corpus: %s", e));
      return false;
    } finally {
      if (channel != null) {
        channel.shutdown();
      }
    }
  }
}
