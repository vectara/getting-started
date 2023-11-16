"""Simple example of using the Vectara REST API for creating a corpus.
"""

import json
import logging
import requests

def _get_create_corpus_json():
    """ Returns a create corpus json. """
    corpus = {}
    corpus["name"] = "Vectara Test Corpus(Python)"
    corpus["description"] = "An example corpus generated via REST API from Python code."

    return json.dumps({"corpus":corpus})

def create_corpus(customer_id: int, admin_address: str, jwt_token: str):
    """Create a corpus.
    Args:
        customer_id: Unique customer ID in vectara platform.
        admin_address: Address of the admin server. e.g., api.vectara.io
        jwt_token: A valid Auth token.

    Returns:
        (response, True) in case of success and returns (error, False) in case of failure.
    """

    post_headers = {
        "customer-id": f"{customer_id}",
        "Authorization": f"Bearer {jwt_token}"
    }
    response = requests.post(
        f"https://{admin_address}/v1/create-corpus",
        data=_get_create_corpus_json(),
        verify=True,
        headers=post_headers)

    if response.status_code != 200:
        logging.error("Create Corpus failed with code %d, reason %s, text %s",
                       response.status_code,
                       response.reason,
                       response.text)
        return response, False

    message = response.json()
    if message["status"]["code"] != "OK":
        logging.error("Create Corpus failed with status: %s", message["status"])
        return message["status"], False

    return message, True
