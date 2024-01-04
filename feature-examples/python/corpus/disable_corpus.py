"""Example of using the Vectara REST API to disable a Corpus."""

import logging

import requests

from corpus import exceptions


def disable_corpus(
    customer_id: int,
    corpus_id: int,
    jwt_token: str,
) -> None:
    """Disables a Corpus.

    Args:
        customer_id: Unique customer ID in vectara platform.
        corpus_id: ID of the corpus to be disabled.
        jwt_token: JWT token to be used for authentication.

    Returns:
        None.

    Raises:
        CorpusException: In case of any error.
    """
    post_headers = {
        "customer-id": f"{customer_id}",
        "Authorization": f"Bearer {jwt_token}",
    }

    request = { "corpusId": corpus_id, "enable": False }

    response = requests.post(
        "https://api.vectara.io/v1/update-corpus-enablement",
        json=request,
        verify=True,
        headers=post_headers,
        timeout=50,
    )

    if response.status_code != 200:
        logging.error(
            "Disable failed with code %d, reason %s, text %s",
            response.status_code,
            response.reason,
            response.text,
        )
        raise exceptions.CorpusException(str(response))

    message = response.json()
    if message["status"]:
        if message["status"]["code"] != "OK":
            raise exceptions.CorpusException(str(message["status"]))

        return

    raise exceptions.CorpusException(str(message))
