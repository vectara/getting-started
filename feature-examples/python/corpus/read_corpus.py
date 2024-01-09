"""Example of using the Vectara REST API to read the corpus info."""

import logging

import requests

from corpus import data_objects
from corpus import exceptions


def read_corpus(
    customer_id: int,
    corpus_id: int,
    jwt_token: str,
) -> data_objects.CorpusInfo:
    """Retrieves the Corpus information.

    Args:
        customer_id: Unique customer ID in vectara platform.
        corpus_id: Corpus ID to be read.
        jwt_token: JWT token to be used for authentication.

    Returns:
        CorpusInfo object.

    Raises:
        CorpusException: In case of any error.
    """
    post_headers = {
        "customer-id": f"{customer_id}",
        "Authorization": f"Bearer {jwt_token}",
    }

    # A request can contain multiple corpus ids, but we are only interested in one.
    request = {
        "corpusId": [corpus_id],
        "readBasicInfo": True,
        "readSize": True,
        "readApiKeys": True,
        "readCustomDimensions": False,
        "readFilterAttributes": False,
    }

    response = requests.post(
        "https://api.vectara.io/v1/read-corpus",
        json=request,
        verify=True,
        headers=post_headers,
        timeout=50,
    )

    if response.status_code != 200:
        logging.error(
            "ReadCorpus failed with code %d, reason: %s, text: %s",
            response.status_code,
            response.reason,
            response.text,
        )
        raise exceptions.CorpusException(str(response))

    message = response.json()
    if message["corpora"] is None or len(message["corpora"]) == 0:
        raise exceptions.CorpusException("Corpus not found")

    corpus_info = message["corpora"][0]
    corpus = corpus_info["corpus"]

    return data_objects.CorpusInfo(
        corpus=data_objects.Corpus(
            corpus_id=corpus["id"],
            name=corpus["name"],
            description=corpus["description"],
            dt_provisioned=corpus["dtProvision"],
            enabled=corpus["enabled"],
        ),
        status=corpus_info["corpusStatus"],
        size=data_objects.CorpusSize(
            epoch_secs=corpus_info["size"]["epochSecs"],
            size=corpus_info["size"]["size"],
        ),
        size_status=corpus_info["sizeStatus"],
        api_keys=[
            data_objects.ApiKey(
                api_key=api_key["id"],
                description=api_key["description"],
                key_type=api_key["keyType"],
                enabled=api_key["enabled"],
            )
            for api_key in corpus_info["apiKey"]
        ],
        api_keys_status=corpus_info["apiKeyStatus"],
    )
