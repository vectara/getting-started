 #!/bin/bash

# For all the proto definitions available in ../../../public/proto folder, this script generates 
# python proto stubs in the current directory. These stubs will be needed to make gRPC calls 
# in python program.

EXROOT=/tmp
EXTERNAL=${EXROOT}/google/api

mkdir -p ${EXTERNAL}
curl https://raw.githubusercontent.com/googleapis/googleapis/master/google/api/http.proto\
    > ${EXTERNAL}/http.proto
curl https://raw.githubusercontent.com/googleapis/googleapis/master/google/api/annotations.proto\
    > ${EXTERNAL}/annotations.proto

python3 -m grpc_tools.protoc -I=../../../protos/ -I=${EXROOT} --python_out=. --grpc_python_out=. \
    ../../../protos/admin.proto \
    ../../../protos/common.proto \
    ../../../protos/custom_dim.proto \
    ../../../protos/indexing.proto \
    ../../../protos/services.proto \
    ../../../protos/serving.proto \
    ../../../protos/status.proto 
