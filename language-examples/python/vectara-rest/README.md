# README #

### Calling Vectara API via REST ###

The Vectara APIs are divided into three parts:

* Indexing (Or Ingesting)
* Serving (Or Querying)
* Admin

This logical separation requires an API user to connect to them with different parameters.
This example will give an idea of what parameters to pass and what needs to be done in order to be able to successfully call these APIs.

### Authentication

There are two supported authentication methods in Vectara. 
Please see the details here: [Authentication](../../../README.md).

### Running this Example
> This example is compatible with Python 3.8+

Following are the steps that need to be done to run this example:

1. Clone the repo (Please see details here: [cloning guidelines](../../../README.md)).
2. Optionally, create a python virtual environment and activate it.

    `python3 -m venv path/to/venv`

    `source path/to/venv/bin/activate`

3. cd to `vectara-demo/python` directory and run the requirements.txt file to install all the required python dependencies:

    `pip3 install -r requirements.txt`

4. cd to `vectara-demo/python/vectara-rest` directory.
5. Run the python code with a command like following:

    a. If you are using OAuth2 as the authentication method:

      ```shell
      python3 rest_basic_operations.py \
        --auth-url "${YOUR_AUTH_URL}" \
        --app-client-id "${YOUR_APPCLIENT_ID}" \
        --app-client-secret "{YOUR_APPCLIENT_SECRET}" \
        --customer-id ${YOUR_CUSTOMER_ID} \
        --corpus-id ${YOUR_CORPUS_ID}
      ```

    b. If you are using an API key as the authentication method:

      ```shell
      python3 rest_api_key_queries.py \
        --api-key "${YOUR_API_KEY}" \
        --customer-id ${YOUR_CUSTOMER_ID} \
        --corpus-id ${YOUR_CORPUS_ID}
      ```

> Please note the double quotes in the arguments, which indicate a string argument.

### Functionality exercised

The examples in this directory exercise the following functionality:

1. Creating a corpus using OAuth.
2. Indexing data into a corpus using OAuth.
3. Querying a corpus using both OAuth and API Keys.
4. Deleting a corpus using OAuth.
5. Resetting a corpus using OAuth.