 #!/bin/bash

# For all the proto definitions available in ../../../protos folder, this script generates
# php proto stubs in the current directory. These stubs will be needed to make grpc calls 
# in php scripts. It first downloads required proto definition imports that don't have released
# generated stubs for php and generates them, too.

# protoc (Protobuf compiler) can be installed using instructions here:
# https://grpc.io/docs/protoc-installation/

EXROOT=/tmp/protos

curl --create-dirs -o ${EXROOT}/protoc-gen-openapiv2/options/annotations.proto\
    https://raw.githubusercontent.com/grpc-ecosystem/grpc-gateway/main/protoc-gen-openapiv2/options/annotations.proto
curl --create-dirs -o ${EXROOT}/protoc-gen-openapiv2/options/openapiv2.proto\
    https://raw.githubusercontent.com/grpc-ecosystem/grpc-gateway/main/protoc-gen-openapiv2/options/openapiv2.proto

protoc --proto_path=../../../protos/ -I=${EXROOT} --php_out=. --grpc_out=. --plugin=protoc-gen-grpc=grpc_php_plugin \
    ../../../protos/admin.proto \
    ../../../protos/common.proto \
    ../../../protos/custom_dim.proto \
    ../../../protos/indexing.proto \
    ../../../protos/services.proto \
    ../../../protos/serving.proto \
    ../../../protos/status.proto \
    ${EXROOT}/protoc-gen-openapiv2/options/annotations.proto \
    ${EXROOT}/protoc-gen-openapiv2/options/openapiv2.proto
