"""Example of using the Vectara REST API to create an API Key."""

import argparse
import json
import logging
import requests
import sys

import vectara_util


def create_apikey(
    customer_id: int,
    corpus_id: int,
    auth_url: str,
    app_client_id: str,
    app_client_secret: str,
):
    """Creates an API key.

    Args:
        customer_id: Unique customer ID in vectara platform.
        corpus_id: Corpus ID to which API key will be created.
        auth_url: The cognito auth url for this customer.
        app_client_id: The id of the app client with enough rights.
        app_client_secret: The app client secret.

    Returns:
        (response, True) in case of success and returns (error, False) in case of failure.

    """
    jwt_token = vectara_util.get_jwt_token(auth_url, app_client_id, app_client_secret)
    if not jwt_token:
        logging.error("Failed to get JWT token.")
        return None, False

    post_headers = {
        "customer-id": f"{customer_id}",
        "Authorization": f"Bearer {jwt_token}",
    }

    # A request can contain multiple api keys. We are creating only one.
    request = {
        "apiKeyData": [
            {
                "description": "API Key for testing.",
                "apiKeyType": 2,  # 1 - Query, 2 - Query & Indexing
                "corpusId": [corpus_id],  # One key can be used for multiple corpora.
            }
        ]
    }

    response = requests.post(
        f"https://api.vectara.io/v1/create-api-key",
        data=json.dumps(request),
        verify=True,
        headers=post_headers,
    )

    if response.status_code != 200:
        logging.error(
            "CreateApiKey failed with code %d, reason %s, text %s",
            response.status_code,
            response.reason,
            response.text,
        )
        return response, False

    message = response.json()
    if message["response"]:
        if len(message["response"]) != 1:
            logging.error("CreateApiKey failed with response %s", message["response"])
            return message["response"], False
        status = message["response"][0]["status"]
        if status["code"] != "OK":
            logging.error("CreateApiKey failed with status %s", status)
            return status, False

        return message["response"][0]["keyId"], True
    return message, False


if __name__ == "__main__":
    logging.basicConfig(
        format="%(asctime)s %(levelname)-8s %(message)s", level=logging.INFO
    )

    parser = argparse.ArgumentParser(
        description="Vectara example to create an API key."
    )

    parser.add_argument(
        "--customer-id", type=int, help="Unique customer ID in Vectara platform."
    )
    parser.add_argument(
        "--corpus-id",
        type=int,
        help="Corpus ID to which API key will be created.",
    )
    parser.add_argument(
        "--app-client-id",
        required=True,
        help="This app client should have enough rights.",
    )
    parser.add_argument("--app-client-secret", required=True)
    parser.add_argument(
        "--auth-url", required=True, help="The cognito auth url for this customer."
    )

    args = parser.parse_args()

    if args:
        response, status = create_apikey(
            args.customer_id,
            args.corpus_id,
            args.auth_url,
            args.app_client_id,
            args.app_client_secret,
        )
        logging.info("CreateApiKey response: %s", response)
        if not status:
            sys.exit(1)
