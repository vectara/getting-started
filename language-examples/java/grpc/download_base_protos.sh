 #!/bin/bash

# This script downloads google base (http.proto and annotations.proto) and the protoc-gen-openapiv2 protos into
# the /tmp/protos directory. These protos are used in Vectara proto definitions.

EXROOT=/tmp/protos

curl --create-dirs -o ${EXROOT}/google/api/annotations.proto\
    https://raw.githubusercontent.com/googleapis/googleapis/master/google/api/annotations.proto
curl --create-dirs -o ${EXROOT}/google/api/http.proto\
    https://raw.githubusercontent.com/googleapis/googleapis/master/google/api/http.proto

curl --create-dirs -o ${EXROOT}/protoc-gen-openapiv2/options/annotations.proto\
    https://raw.githubusercontent.com/grpc-ecosystem/grpc-gateway/main/protoc-gen-openapiv2/options/annotations.proto
curl --create-dirs -o ${EXROOT}/protoc-gen-openapiv2/options/openapiv2.proto\
    https://raw.githubusercontent.com/grpc-ecosystem/grpc-gateway/main/protoc-gen-openapiv2/options/openapiv2.proto
