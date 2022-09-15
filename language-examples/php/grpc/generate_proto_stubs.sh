 #!/bin/bash

# For all the proto definitions available in ../../../public/proto folder, this script generates 
# php proto stubs in the current directory. These stubs will be needed to make grpc calls 
# in php scripts.

# protoc (Protobuf compiler) can be installed using instructions here:
# https://grpc.io/docs/protoc-installation/

protoc --proto_path=../../../protos/ --php_out=. --grpc_out=. --plugin=protoc-gen-grpc=grpc_php_plugin \
    ../../../protos/admin.proto \
    ../../../protos/common.proto \
    ../../../protos/custom-dim.proto \
    ../../../protos/indexing.proto \
    ../../../protos/services.proto \
    ../../../protos/serving.proto \
    ../../../protos/status.proto
