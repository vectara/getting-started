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

python3 -m grpc_tools.protoc -I=../../../public/proto/ -I=${EXROOT} --python_out=. --grpc_python_out=. \
    ../../../public/proto/admin.proto \
    ../../../public/proto/services.proto \
    ../../../public/proto/common.proto \
    ../../../public/proto/currency.proto \
    ../../../public/proto/indexing.proto \
    ../../../public/proto/status.proto \
    ../../../public/proto/core-services.proto \
    ../../../public/proto/custom-dim.proto \
    ../../../public/proto/serving.proto \
    ../../../public/proto/indexing-core.proto
