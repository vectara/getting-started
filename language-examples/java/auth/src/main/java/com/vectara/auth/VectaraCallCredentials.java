package com.vectara.auth;

import com.google.common.primitives.Longs;
import io.grpc.CallCredentials;
import io.grpc.Metadata;
import io.grpc.Metadata.AsciiMarshaller;
import io.grpc.Metadata.BinaryMarshaller;
import java.util.concurrent.Executor;
import javax.annotation.Nullable;

/**
 * A class that wraps grpc.CallCredentials. Appends required Vectara metadata (authToken, customer
 * ID, corpus ID) to requests. AuthToken can either be an OAuth2 token or an API Key.
 */
public class VectaraCallCredentials extends CallCredentials {
  private final String authToken;
  private final long customerId;
  private final @Nullable Long corpusId;
  private final AuthType authType;
  public VectaraCallCredentials(AuthType authType,
                                String authToken,
                                long customerId,
                                Long corpusId) {
    this.authType = authType;
    this.authToken = authToken;
    this.customerId = customerId;
    this.corpusId = corpusId;
  }

  public VectaraCallCredentials(AuthType authType, String authToken, long customerId) {
    this(authType, authToken, customerId, null);
  }

  /**
   *  This method appends required Vectara metadata to the gRPC call. This metadata includes
   *  authorization token, customer ID and corpus ID etc.
   */
  @Override
  public void applyRequestMetadata(
      RequestInfo requestInfo, Executor executor, MetadataApplier metadataApplier) {
    Metadata metadata = new Metadata();
    switch (authType) {
      case OAUTH_TOKEN:
        metadata.put(Metadata.Key.of("Authorization", Marshallers.STRING_MARSHALLER),
            "Bearer " + authToken);
        break;
      case API_KEY:
        metadata.put(Metadata.Key.of("x-api-key", Marshallers.STRING_MARSHALLER),
            authToken);
        break;
    }
    metadata.put(Metadata.Key.of("customer-id-bin", Marshallers.LONG_MARSHALLER), customerId);
    if (corpusId != null) {
      metadata.put(Metadata.Key.of("corpus-id-bin", Marshallers.LONG_MARSHALLER), corpusId);
    }
    metadataApplier.apply(metadata);
  }

  @Override
  public void thisUsesUnstableApi() {}

  public enum AuthType {
    OAUTH_TOKEN,
    API_KEY
  }

  private static class Marshallers {
    static final BinaryMarshaller<Long> LONG_MARSHALLER =
        new BinaryMarshaller<>() {
          @Override
          public byte[] toBytes(Long value) {
            return Longs.toByteArray(value);
          }

          @Override
          public Long parseBytes(byte[] serialized) {
            return Longs.fromByteArray(serialized);
          }
        };

    static final AsciiMarshaller<String> STRING_MARSHALLER =
        new Metadata.AsciiMarshaller<>() {
          @Override
          public String toAsciiString(String s) { return s; }

          @Override
          public String parseAsciiString(String s) { return s; }
        };
  }
}
