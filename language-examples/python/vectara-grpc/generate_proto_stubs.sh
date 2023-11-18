 #!/bin/bash

# For all the proto definitions available in ../../../public/proto folder, this script generates 
# python proto stubs in the current directory. These stubs will be needed to make gRPC calls 
# in python program.

set -e

EXROOT=$(mktemp -d)
GOOGLE_API_ROOT="${EXROOT}/google/api"
OPENAPIV2_ROOT="${EXROOT}/protoc-gen-openapiv2"

mkdir -p "${GOOGLE_API_ROOT}"
mkdir -p "${OPENAPIV2_ROOT}/options"

echo "Downloading protos to ${EXROOT}"

curl https://raw.githubusercontent.com/googleapis/googleapis/master/google/api/http.proto \
    > ${GOOGLE_API_ROOT}/http.proto
curl https://raw.githubusercontent.com/googleapis/googleapis/master/google/api/annotations.proto \
    > ${GOOGLE_API_ROOT}/annotations.proto
curl https://raw.githubusercontent.com/grpc-ecosystem/grpc-gateway/main/protoc-gen-openapiv2/options/openapiv2.proto \
    > ${OPENAPIV2_ROOT}/options/openapiv2.proto
curl https://raw.githubusercontent.com/grpc-ecosystem/grpc-gateway/main/protoc-gen-openapiv2/options/annotations.proto \
    > ${OPENAPIV2_ROOT}/options/annotations.proto

python3 -m grpc_tools.protoc -I=../../../protos/ -I=${EXROOT} --python_out=. --grpc_python_out=. \
    admin.proto \
    common.proto \
    custom_dim.proto \
    indexing.proto \
    services.proto \
    serving.proto \
    status.proto \
    protoc-gen-openapiv2/options/annotations.proto \
    protoc-gen-openapiv2/options/openapiv2.proto

echo "Proto compilation completed."
