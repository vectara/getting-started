 #!/bin/bash

# For all the proto definitions available in ../../../public/proto folder, this script generates 
# php proto stubs in the current directory. These stubs will be needed to make grpc calls 
# in php scripts.

# protoc (Protobuf compiler) can be installed using instructions here:
# https://grpc.io/docs/protoc-installation/

protoc --proto_path=../../../public/proto/ --php_out=. --grpc_out=. --plugin=protoc-gen-grpc=grpc_php_plugin \
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
