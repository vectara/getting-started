"""Example of using the Vectara REST API to create an API Key."""

import logging

import requests


def create_apikey(
    customer_id: int,
    corpus_id: int,
    jwt_token: str,
) -> tuple[str, bool]:
    """Creates an API key.

    Args:
        customer_id: Unique customer ID in vectara platform.
        corpus_id: Corpus ID to which API key will be created.
        jwt_token: JWT token to be used for authentication.

    Returns:
        (apiKey, True) in case of success and returns (error, False) in case of failure.
    """
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
        "https://api.vectara.io/v1/create-api-key",
        json=request,
        verify=True,
        headers=post_headers,
        timeout=50,
    )

    if response.status_code != 200:
        logging.error(
            "CreateApiKey failed with code %d, reason %s, text %s",
            response.status_code,
            response.reason,
            response.text,
        )
        return str(response), False

    message = response.json()
    if message["response"]:
        if len(message["response"]) != 1:
            logging.error("CreateApiKey failed with response %s", message["response"])
            return str(message["response"]), False

        status = message["response"][0]["status"]
        if status["code"] == "OK":
            return message["response"][0]["keyId"], True
        logging.error("CreateApiKey failed with status %s", status)
        return str(status), False

    return str(message), False
