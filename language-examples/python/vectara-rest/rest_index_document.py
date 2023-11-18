"""Simple example of using the Vectara REST API for indexing."""

import json
import logging
import requests


def _get_index_request_json(customer_id: int, corpus_id: int):
    """Returns some example data to index."""
    document = {
        # Note that the document ID must be unique for a given corpus.
        "document_id": "doc-id-2",
        "title": "Another example Title",
        "metadata_json": json.dumps(
            {
                "book-name": "Another example title",
                "collection": "Mathematics",
                "author": "Example Author",
            }
        ),
        "section": [
            {"text": ("The answer to the ultimate question "
                      "of life, the universe, and everything is 42.")},
        ],
    }

    request = {
        "customer_id": customer_id,
        "corpus_id": corpus_id,
        "document": document,
    }

    return json.dumps(request)


def index_document(customer_id: int, corpus_id: int, idx_address: str, jwt_token: str):
    """Indexes content to the corpus.

    Args:
        customer_id: Unique customer ID in vectara platform.
        corpus_id: ID of the corpus to which data needs to be indexed.
        idx_address: Address of the indexing server. e.g., api.vectara.io
        jwt_token: A valid Auth token.

    Returns:
        (response, True) in case of success and returns (error, False) in case of failure.
    """
    post_headers = {
        "Authorization": f"Bearer {jwt_token}",
        "customer-id": f"{customer_id}"
    }
    response = requests.post(
        f"https://{idx_address}/v1/index",
        data=_get_index_request_json(customer_id, corpus_id),
        verify=True,
        headers=post_headers)

    if response.status_code != 200:
        logging.error("REST upload failed with code %d, reason %s, text %s",
                       response.status_code,
                       response.reason,
                       response.text)
        return response, False

    message = response.json()
    if message["status"] and message["status"]["code"] not in ("OK", "ALREADY_EXISTS"):
        logging.error("REST upload failed with status: %s", message["status"])
        return message["status"], False

    return message, True
