 #!/bin/bash

# This script downloads google base protos (http.proto and annotations.proto) into the /tmp 
# directory. These protos are used in Vectara proto definitions.

EXROOT=/tmp/protos
EXTERNAL=${EXROOT}/google/api

mkdir -p ${EXTERNAL}
curl https://raw.githubusercontent.com/googleapis/googleapis/master/google/api/http.proto\
    > ${EXTERNAL}/http.proto
curl https://raw.githubusercontent.com/googleapis/googleapis/master/google/api/annotations.proto\
    > ${EXTERNAL}/annotations.proto