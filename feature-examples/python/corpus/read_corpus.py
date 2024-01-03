"""Example of using the Vectara REST API to read the corpus info."""

import dataclasses
import logging

import requests

from utils import error_handling


@dataclasses.dataclass(frozen=True)
class Corpus:
    """Basic Corpus data."""

    corpus_id: int
    name: str
    description: str
    dt_provisioned: str
    enabled: bool


@dataclasses.dataclass(frozen=True)
class CorpusSize:
    """Corpus Size information"""

    epoch_secs: int
    size: int


@dataclasses.dataclass(frozen=True)
class ApiKey:
    """API Key information"""

    api_key: str
    description: str
    key_type: str
    enabled: bool


@dataclasses.dataclass(frozen=True)
class CorpusInfo:
    """All corpus data such as id, name, size, associated api keys etc."""

    corpus: Corpus
    status: str
    size: CorpusSize
    size_status: str
    api_keys: list[ApiKey]
    api_keys_status: str


def read_corpus(
    customer_id: int,
    corpus_id: int,
    jwt_token: str,
) -> CorpusInfo:
    """Retrieves the Corpus information.

    Args:
        customer_id: Unique customer ID in vectara platform.
        corpus_id: Corpus ID to be read.
        jwt_token: JWT token to be used for authentication.

    Returns:
        CorpusInfo objects.

    Raises:
        CorpusException: In case of any error.
    """
    post_headers = {
        "customer-id": f"{customer_id}",
        "Authorization": f"Bearer {jwt_token}",
    }

    request = {
        "corpusId": [int(corpus_id)],
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
            "ReadCorpus failed with code %d, reason %s, text %s",
            response.status_code,
            response.reason,
            response.text,
        )
        raise error_handling.CorpusException(str(response))

    message = response.json()
    if message["corpora"] is None or len(message["corpora"]) == 0:
        raise error_handling.CorpusException("Corpus not found")

    corpus_info = message["corpora"][0]
    corpus = corpus_info["corpus"]
    api_keys = corpus_info["apiKey"]

    return CorpusInfo(
        corpus=Corpus(
            corpus_id=corpus["id"],
            name=corpus["name"],
            description=corpus["description"],
            dt_provisioned=corpus["dtProvision"],
            enabled=corpus["enabled"],
        ),
        status=corpus_info["corpusStatus"],
        size=CorpusSize(
            epoch_secs=corpus_info["size"]["epochSecs"],
            size=corpus_info["size"]["size"],
        ),
        size_status=corpus_info["sizeStatus"],
        api_keys=[
            ApiKey(
                api_key=api_key["id"],
                description=api_key["description"],
                key_type=api_key["keyType"],
                enabled=api_key["enabled"],
            )
            for api_key in api_keys
        ],
        api_keys_status=corpus_info["apiKeyStatus"],
    )
