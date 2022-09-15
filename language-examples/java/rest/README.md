# README #

### Calling Vectara API via REST ###

The Vectara APIs are divided into three parts:

* Indexing (Or Ingesting)
* Serving (Or Querying)
* Admin

This logical separation requires an API user to connect to them with different parameters.
This example will give an idea of what parameters to pass and what needs to be done in
order to be able to successfully call these APIs.

### Authentication
There are two supported authentication methods Vectara.
Please see the details here: [Authentication](../../../README.md).

### Running the Example
> This example is built with JDK 11. To run this example, jdk 11 needs to be installed and discoverable.

Following are the steps that need to be done to run this example:

1. Clone the repo (Please see details here: [cloning guidelines](../../../README.md)).
2. cd to 'vectara-java-demo/java/auth' directory and run `mvn install`. This will build the
   authentication library and will make it available for subsequent projects.
3. cd to `vectara-java-demo/java/rest` directory.
4. run `mvn package` (This will create a target directory with a shaded JAR)
5. Run the jar file with a command like following:
   a. If you are using OAuth2 as the authentication method:
    ```shell
       java -cp target/rest-1.0-SNAPSHOT.jar com.vectara.examples.rest.RestBasicOperations \
         --customer-id ${YOUR_CUSTOMER_ID} \
         --corpus-id ${YOUR_CORPUS_ID} \
         --auth-url "${YOUR_AUTH_URL}" \
         --app-client-id "${YOUR_APPCLIENT_ID}" \
         --app-client-secret "${YOUR_APPCLIENT_SECRET}"
    ```
   b. If you are using an API key as the authentication method:
    ```shell
       java -cp target/rest-1.0-SNAPSHOT.jar com.vectara.examples.rest.RestApiKeyQueries \
         --customer-id ${YOUR_CUSTOMER_ID} \
         --corpus-id ${YOUR_CORPUS_ID} \
         --api-key "${YOUR_API_KEY}"
    ```

> Please note the double quotes in the arguments, which indicate a string argument.

### Functionality exercised

The examples in this directory exercise the following functionality:

1. Creating a corpus using OAuth.
2. Indexing data into a corpus using OAuth.
3. Querying a corpus using both OAuth and API Keys.