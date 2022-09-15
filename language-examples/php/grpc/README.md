# README #

### Calling Vectara API via gRPC ###

The Vectara APIs are divided into three parts:

* Indexing (Or Ingesting)
* Serving (Or Querying)
* Admin

This logical separation requires an API user to connect to them with different parameters.
This example will give an idea of what parameters to pass and what needs to be done in 
order to be able to successfully call these APIs.

### Authentication

There are two supported authentication methods in Vectara. 
Please see the details here: [Authentication](../../../README.md).


### Pre-requisites

1. You need to have php installed. This example is built with php 8.1.2

2. This example requires php-curl to be installed on the server. You can install it using the following command:

    `sudo apt-get install php8.1-curl`

3. Install the php composer using [instructions from here](https://getcomposer.org/doc/00-intro.md#locally)

4. Install gRPC extension for php using one of the methods datailed [here](https://github.com/grpc/grpc/tree/master/src/php):

5. Make sure that `grpc.so` is added to your default `php.ini`

6. Generate the `grpc_php_plugin` using the method outlined [here](https://www.grpc.io/docs/languages/php/basics/#setup). This will be needed to generate php proto stubs later.


### Run the example
To run the example, do the following:

1. Clone the repo (Please see details here: [cloning guidelines](../../../README.md)).
2. cd to `vectara-demo/php/grpc` directory.
3. Run the `generate_proto_stubs.sh` shell script:

    `sh generate_proto_stubs.sh`
   
   This will generate two folders containing Vectara proto php stubs: `Com/Vectara/` and `GPBMetadata/`

4. Install php dependencies using the composer script. This will make sure that all required dependencies are downloaded:

    `composer install`

5. Run your PHP server with a command like following:

    `php -S localhost:8000`

6. Open your web browser with URL: http://localhost:8000
7. Fill in the required information in the fields such as customer ID, corpus ID, auth URL, etc.

    > All of the required fields can be obtained from [Vectara Console](https://vectara.com/console/overview)

8. Try four actions: `Create Corpus`, `Query Data (OAuth)`, `Query Data (API Key)` and `Index Data`.
9. The result of these actions will be displayed in the Result status textarea.
