package com.vectara.examples.grpc;

import com.vectara.QueryServiceGrpc.QueryServiceBlockingStub;
import com.vectara.serving.ServingProtos.BatchQueryRequest;
import com.vectara.serving.ServingProtos.BatchQueryResponse;
import com.vectara.serving.ServingProtos.CorpusKey;
import com.vectara.serving.ServingProtos.QueryRequest;
import com.beust.jcommander.JCommander;
import com.vectara.auth.VectaraCallCredentials;
import com.vectara.auth.VectaraCallCredentials.AuthType;
import io.grpc.ManagedChannel;
import io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.NettyChannelBuilder;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.net.ssl.SSLException;

/**
 * A class that demonstrates how Vectara Serving API can be called using gRPC and an
 * API Key for authentication.
 */
public class GrpcApiKeyQueries {
  private static final Logger LOGGER = Logger.getLogger(GrpcApiKeyQueries.class.getName());

  public static void main(String[] argv) {
    GrpcArgs args = new GrpcArgs();
    JCommander.newBuilder().addObject(args).build().parse(argv);
    if (args.apiKey == null) {
      LOGGER.log(Level.SEVERE, "Please provide an API Key to run this example.");
      System.exit(1);
    }
    String apiKey = args.apiKey;

    var result = queryData(apiKey, args.servingEndpoint, "test", args.customerId, args.corpusId);
    if (!result) {
      LOGGER.log(Level.SEVERE, "Querying failed. Please see previous logs for details.");
      System.exit(1);
    }
  }

  private static boolean queryData(String apiKey,
                                   String servingUrl,
                                   String query,
                                   Long customerId,
                                   Long corpusId) {
    ManagedChannel channel = null;
    try {
      channel = NettyChannelBuilder.forAddress(servingUrl, 443)
          .sslContext(GrpcSslContexts.forClient().trustManager((File) null).build())
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


      BatchQueryResponse response = querying
          .withCallCredentials(
              new VectaraCallCredentials(AuthType.API_KEY, apiKey, customerId, corpusId))
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
}
