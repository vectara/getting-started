package com.vectara.examples.grpc;

import com.beust.jcommander.Parameter;
import com.google.common.base.Strings;
import com.vectara.QueryServiceGrpc.QueryServiceBlockingStub;
import com.vectara.StatusProtos.StatusCode;
import com.vectara.serving.ServingProtos.BatchQueryRequest;
import com.vectara.serving.ServingProtos.BatchQueryResponse;
import com.vectara.serving.ServingProtos.CorpusKey;
import com.vectara.serving.ServingProtos.QueryRequest;
import com.beust.jcommander.JCommander;
import com.vectara.auth.VectaraCallCredentials;
import com.vectara.auth.VectaraCallCredentials.AuthType;
import io.grpc.ManagedChannel;
import io.grpc.StatusRuntimeException;
import io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.NettyChannelBuilder;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nullable;
import javax.net.ssl.SSLException;

/**
 * A class that demonstrates how Vectara Serving API can be called using gRPC and an
 * API Key for authentication.
 */
public class GrpcApiKeyQueries {
  private static final Logger LOGGER = Logger.getLogger(GrpcApiKeyQueries.class.getName());


  private static final class Args {
    @Parameter(
        names = {"--customer-id"},
        description = "Unique customer ID in Vectara platform.",
        required = true)
    Long customerId = null;

    @Parameter(
        names = {"--corpus-id"},
        description = "Corpus ID against which examples need to be run.",
        required = true)
    Long corpusId = 1L;

    @Parameter(
        names = {"--serving-host"},
        description = "Serving host")
    String servingHost = "serving.vectara.io";

    @Parameter(
        names = {"--serving-port"},
        description = "Serving port")
    int servingPort = 443;

    @Parameter(
        names= {"--tls-trust-manager"},
        description = "Trusted certificates for verifying the remote endpoint's "
            + "certificate. The file should contain an X.509 certificate "
            + "collection in PEM format. Unset uses the system default.")
    String tlsTrustManager = null;

    @Parameter(
        names= {"--api-key"},
        description = "API key retrieved from Vectara console",
        required = true)
    String apiKey = null;

    @Parameter(
        names= {"--query"},
        description = "The query you want to send to Vectara.")
    String query = "What is the meaning of life?";

    @Parameter(
        names= {"--help"},
        help = true)
    private boolean help = false;
  }


  public static void main(String[] argv) {
    var args = new Args();
    JCommander cli = JCommander
        .newBuilder()
        .addObject(args).build();
    cli.setProgramName("GrpcApiKeyQueries");
    cli.parse(argv);
    if (args.help) {
      cli.usage();
      System.exit(0);
    }

    var response = queryData(
        args.apiKey,
        args.servingHost,
        args.servingPort,
        args.tlsTrustManager,
        args.query,
        args.customerId,
        args.corpusId);
    if (response == null) {
      LOGGER.log(Level.SEVERE, "Querying failed. Please see previous logs for details.");
      System.exit(1);
    }
    for (var status : response.getStatusList()) {
      if (status.getCode() != StatusCode.OK) {
        LOGGER.severe("Failure status on query: " + status);
        System.exit(1);
      }
    }
    for (var responseSet : response.getResponseSetList()) {
      for (var status : responseSet.getStatusList()) {
        if (status.getCode() != StatusCode.OK) {
          LOGGER.severe("Failure querying corpus: " + status);
          System.exit(1);
        }
      }
    }
    LOGGER.info(String.format("Querying response: %s", response.toString()));
  }

  /**
   * Query data and return the response. On failure, null is returned.
   */
  @Nullable
  private static BatchQueryResponse queryData(
      String apiKey,
      String servingHost,
      int servingPort,
      String tlsTrustManager,
      String query,
      Long customerId,
      Long corpusId) {
    ManagedChannel channel = null;
    try {
      channel = NettyChannelBuilder.forAddress(servingHost, servingPort)
          .sslContext(GrpcSslContexts
              .forClient()
              .trustManager(
                  Strings.isNullOrEmpty(tlsTrustManager)
                  ? null
                  : new File(tlsTrustManager))
              .build())
          .build();
      QueryServiceBlockingStub querying =
          com.vectara.QueryServiceGrpc.newBlockingStub(channel);
      BatchQueryRequest.Builder builder = BatchQueryRequest.newBuilder();
      builder.addQuery(
          QueryRequest.newBuilder()
              .setQuery(query)
              .setNumResults(10)
              .addCorpusKey(
                  CorpusKey.newBuilder()
                      .setCorpusId(corpusId.intValue())
                      .setCustomerId(customerId.intValue())
                      .build())
              .build());


      return querying
          .withCallCredentials(
              new VectaraCallCredentials(AuthType.API_KEY, apiKey, customerId, corpusId))
          .query(builder.build());

    } catch (SSLException | StatusRuntimeException e) {
      LOGGER.log(Level.SEVERE, String.format("Error while querying data: %s", e));
      return null;
    } finally {
      if (channel != null) {
        channel.shutdown();
      }
    }
  }
}
