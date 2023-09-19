# README #

### Calling Vectara API via gRPC ###

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

There are two supported authentication methods Vectara. 
Please see the details here: [Authentication](../../../README.md).

### Running the Example
> This example is built with JDK 11. To run this example, JDK 11 needs to be installed and discoverable.

Following are the steps that need to be done to run this example:

1. Clone the repo (Please see details here: [cloning guidelines](../../../README.md)).
2. Build the authentication library and make it available for subsequent projects.
   ```shell
   cd language-examples/java/auth
   mvn install
   ```
3. Build the gRPC example. This will create a target directory with a shaded JAR.
   ```shell
   cd language-examples/java/grpc
   mvn package
   ```
5. Run the jar file with a command like following:

    a. If you are using OAuth2 as the authentication method:
    ```shell
       java -cp target/grpc-1.0-SNAPSHOT.jar com.vectara.examples.grpc.GrpcBasicOperations \
         --customer-id ${YOUR_CUSTOMER_ID} \
         --corpus-id ${YOUR_CORPUS_ID} \
         --auth-url "${YOUR_AUTH_URL}" \
         --app-client-id "${YOUR_APPCLIENT_ID}" \
         --app-client-secret "${YOUR_APPCLIENT_SECRET}"
    ```
    b. If you are using an API key as the authentication method:
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

