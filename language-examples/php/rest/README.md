# README #

### Calling Vectara API via REST/HTTP ###

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

### Running the example
> This example is built with php 8.1.2.

This example requires php-curl to be installed on the server. You can install it using the following command:

`sudo apt-get install php8.1-curl`

To run the example, do the following:

1. Clone the repo (Please see details here: [cloning guidelines](../../../README.md)).
2. cd to `vectara-demo/php/rest` directory.
3. Run your PHP server with a command like following:

    `php -S localhost:8000`

4. Open your web browser with URL: http://localhost:8000
5. Fill in the required information in the fields such as customer ID, corpus ID, auth URL, etc.

    > All of the required fields can be obtained from [Vectara Console](https://vectara.com/console/overview)

6. Try four actions: `Create Corpus`, `Query Data (OAuth)`, `Query Data (API Key)` and `Index Data`.
7. The result of these actions will be displayed in the Result status textarea.
