 #!/bin/bash

# For all the proto definitions available in ../../../public/proto folder, this script generates 
# python proto stubs in the current directory. These stubs will be needed to make gRPC calls 
# in python program. It first downloads required proto definition imports that don't have released
# generated stubs for python and generates them, too.

EXROOT=/tmp/protos

curl --create-dirs -o ${EXROOT}/protoc-gen-openapiv2/options/annotations.proto\
    https://raw.githubusercontent.com/grpc-ecosystem/grpc-gateway/main/protoc-gen-openapiv2/options/annotations.proto
curl --create-dirs -o ${EXROOT}/protoc-gen-openapiv2/options/openapiv2.proto\
    https://raw.githubusercontent.com/grpc-ecosystem/grpc-gateway/main/protoc-gen-openapiv2/options/openapiv2.proto

python3 -m grpc_tools.protoc -I=../../../protos/ -I=${EXROOT} --python_out=. --grpc_python_out=. \
    ../../../protos/admin.proto \
    ../../../protos/common.proto \
    ../../../protos/custom_dim.proto \
    ../../../protos/indexing.proto \
    ../../../protos/services.proto \
    ../../../protos/serving.proto \
    ../../../protos/status.proto \
    ${EXROOT}/protoc-gen-openapiv2/options/annotations.proto \
    ${EXROOT}/protoc-gen-openapiv2/options/openapiv2.proto
