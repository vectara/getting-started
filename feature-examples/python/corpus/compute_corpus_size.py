"""Example of using the Vectara REST API to compute corpus size."""

import dataclasses
import logging

import requests

from corpus import exceptions

@dataclasses.dataclass(frozen=True)
class CorpusSize:
    """Corpus Size information"""

    epoch_secs: int
    size: int


def compute_corpus_size(
    customer_id: int,
    corpus_id: int,
    jwt_token: str,
) -> CorpusSize:
    """Computes a corpus size.

    Args:
        customer_id: Unique customer ID in vectara platform.
        corpus_id: Corpus ID for which size needs to be computed.
        jwt_token: JWT token to be used for authentication.

    Returns:
        CorpusSize object

    Raises:
        CorpusException: In case of any error.
    """
    post_headers = {
        "customer-id": f"{customer_id}",
        "Authorization": f"Bearer {jwt_token}",
    }

    request = { "corpusId": corpus_id }

    response = requests.post(
        "https://api.vectara.io/v1/compute-corpus-size",
        json=request,
        verify=True,
        headers=post_headers,
        timeout=50,
    )

    if response.status_code != 200:
        logging.error(
            "ComputeCorpusSize failed with code %d, reason %s, text %s",
            response.status_code,
            response.reason,
            response.text,
        )
        raise exceptions.CorpusException(str(response))

    message = response.json()
    if message["status"]:
        if message["status"]["code"] != "OK":
            raise exceptions.CorpusException(str(message["status"]))
        return CorpusSize(
            epoch_secs=message["size"]["epochSecs"],
            size=message["size"]["size"]
        )

    raise exceptions.CorpusException(str(message))
