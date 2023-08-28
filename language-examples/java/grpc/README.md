# README #

### Calling the Vectara API via gRPC ###

The Vectara APIs are divided into three parts:

* Indexing (Or Ingesting)
* Serving (Or Querying)
* Admin

This separation requires API users to connect to each separately. The examples in this
directory demonstrate how to connect to and use all three APIs via gRPC. Specifically, they
demonstrate the following functionality:

1. Creating a corpus using OAuth.
2. Indexing data into a corpus using OAuth.
3. Querying a corpus using both OAuth and API Keys.

### Authentication

Vectara supports OAuth2 and API Key authentication methods. For details, please visit
[docs.vectara.com](https://docs.vectara.com). You can also find more information in this
repository's top-level [README](../../../README.md) file.

### Running the Example
> This example is built with JDK 11. To run this example, JDK 11 needs to be installed and discoverable.

Following are the steps that need to be done to run this example:

1. Clone the repo (Please see details here: [cloning guidelines](../../../README.md)).
2. Build the authentication library and make it available for subsequent projects.
   ```shell
   cd language-examples/java/auth
   mvn install
   ```
3. Download the required Protobuf definitions. This script downloads some base protos and stores them in
   `/tmp/protos` directory. This script is built for a linux OS (preferably Debian-based). If you are running
   on Windows,then you will have to modify it to run with a terminal like Powershell etc.
   ```shell
   cd language-examples/java/grpc
   sh download_base_protos.sh
   ```
4. Build the gRPC example. This will create a target directory with a shaded JAR.
   ```shell
   cd language-examples/java/grpc
   mvn package
   ```
5. Run the jar file with either of the commands below, depending on the authentication
   method you are using:

    a. If you are using [OAuth2](https://docs.vectara.com/docs/api-reference/auth-apis/oauth-2) as 
       the authentication method:
    ```shell
       java -cp target/grpc-1.0-SNAPSHOT.jar com.vectara.examples.grpc.GrpcBasicOperations \
         --customer-id ${YOUR_CUSTOMER_ID} \
         --corpus-id ${YOUR_CORPUS_ID} \
         --auth-url "${YOUR_AUTH_URL}" \
         --app-client-id "${YOUR_APPCLIENT_ID}" \
         --app-client-secret "${YOUR_APPCLIENT_SECRET}"
    ```
    b. If you are using an [API key](https://docs.vectara.com/docs/common-use-cases/app-authn-authz/api-keys)
       as the authentication method:
    ```shell
       java -cp target/grpc-1.0-SNAPSHOT.jar com.vectara.examples.grpc.GrpcApiKeyQueries \
         --customer-id ${YOUR_CUSTOMER_ID} \
         --corpus-id ${YOUR_CORPUS_ID} \
         --api-key "${YOUR_API_KEY}" \
         --query "What is the meaning of life?"
    ```
Note the double quotes in the arguments, which indicate a string argument.

You can run GrpcApiKeyQueries with the `--help` option to obtain a comprehensive list of command 
line arguments.

