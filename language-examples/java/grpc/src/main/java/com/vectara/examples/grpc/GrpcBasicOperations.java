package com.vectara.examples.grpc;

import com.vectara.AdminServiceGrpc;
import com.vectara.AdminServiceGrpc.AdminServiceBlockingStub;
import com.vectara.IndexServiceGrpc;
import com.vectara.IndexServiceGrpc.IndexServiceBlockingStub;
import com.vectara.QueryServiceGrpc;
import com.vectara.QueryServiceGrpc.QueryServiceBlockingStub;
import com.vectara.ServiceProtos.IndexDocumentRequest;
import com.vectara.ServiceProtos.IndexDocumentResponse;
import com.vectara.admin.AdminProtos.Corpus;
import com.vectara.admin.AdminProtos.CreateCorpusRequest;
import com.vectara.admin.AdminProtos.CreateCorpusResponse;
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
 * called using gRPC and OAuth (for authentication)
 */
public class GrpcBasicOperations {
  private static final Logger LOGGER = Logger.getLogger(GrpcBasicOperations.class.getName());

  public static void main(String[] argv) {
    GrpcArgs args = new GrpcArgs();
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

  private static ManagedChannel managedChannel(String url) throws SSLException {
    return NettyChannelBuilder.forAddress(url, 443)
        .sslContext(GrpcSslContexts.forClient().trustManager((File) null).build())
        .build();
  }

  private static IndexDocumentRequest getDocument(long customerId, long corpusId) {
    return IndexDocumentRequest.newBuilder()
        .setCorpusId(corpusId)
        .setCustomerId(customerId)
        .setDocument(
            Document.newBuilder()
                .setDocumentId("doc-id-1")
                .setTitle("Title of the document.")
                .setDescription("Description of the document.")
                .setMetadataJson("{\"author\":\"Vectara\", \"date_created\":\"Jul 1st, 2022\"}")
                .addSection(
                    Section.newBuilder()
                        .setId(1)
                        .setTitle("Test Section")
                        .setText("Some dummy text.")
                        .build())
                .build())
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
    ManagedChannel channel = null;
    try {
      channel = managedChannel(indexingUrl);
      IndexServiceBlockingStub indexing =
          IndexServiceGrpc.newBlockingStub(channel);
      IndexDocumentResponse response =
          indexing
              .withCallCredentials(new VectaraCallCredentials(AuthType.OAUTH_TOKEN,
                                                              jwtToken,
                                                              customerId,
                                                              corpusId))
              .index(getDocument(customerId, corpusId));
      LOGGER.info(String.format("Indexing response: %s", response.toString()));
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
      LOGGER.info(String.format("Querying response: %s", response.toString()));
      return true;
    } catch (SSLException e) {
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
   * @return success or failure.
   */
  public static boolean createCorpus(
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
      LOGGER.info(String.format("Create Corpus response: %s", response.toString()));
      return true;
    } catch (SSLException e) {
      LOGGER.log(Level.SEVERE, String.format("Error while creating a corpus: %s", e));
      return false;
    } finally {
      if (channel != null) {
        channel.shutdown();
      }
    }
  }
}
