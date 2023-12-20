"""Example of using the Vectara REST API to list the API Keys."""

import dataclasses
import logging

import requests


@dataclasses.dataclass(frozen=True)
class CorpusData:
    """Corpus id and name."""

    corpus_id: int
    corpus_name: str


@dataclasses.dataclass(frozen=True)
class KeyData:
    """API Key data such as id, type, list of corpora etc."""

    key_id: str
    description: str
    key_type: str
    enabled: bool
    corpora: list[CorpusData]

class ListApiKeyException(Exception):
    """Base class for exceptions in this module."""


def list_api_keys(
    customer_id: int,
    jwt_token: str,
) -> list[KeyData]:
    """Retrieves the list of API keys.

    Args:
        customer_id: Unique customer ID in vectara platform.
        jwt_token: JWT token to be used for authentication.

    Returns:
        list of KeyData objects.

    Raises:
        Exception: In case of any error.
    """
    post_headers = {
        "customer-id": f"{customer_id}",
        "Authorization": f"Bearer {jwt_token}",
    }

    request = {"numResults": 10, "readCorporaInfo": True}

    response = requests.post(
        "https://api.vectara.io/v1/list-api-keys",
        json=request,
        verify=True,
        headers=post_headers,
        timeout=50,
    )

    if response.status_code != 200:
        logging.error(
            "ListApiKeys failed with code %d, reason %s, text %s",
            response.status_code,
            response.reason,
            response.text,
        )
        raise Exception(str(response))

    message = response.json()
    if message["status"]:
        status = message["status"]
        if status["code"] == "OK":
            result: list[KeyData] = []
            for key in message["keyData"]:
                result.append(
                    KeyData(
                        key_id=key["apiKey"]["id"],
                        description=key["apiKey"]["description"],
                        key_type=key["apiKey"]["keyType"],
                        enabled=key["apiKey"]["enabled"],
                        corpora=[
                            CorpusData(
                                corpus_id=corpus["id"], corpus_name=corpus["name"]
                            )
                            for corpus in key["corpus"]
                        ],
                    )
                )
            return result

        logging.error("ListApiKeys failed with status %s", status)
        raise ListApiKeyException(str(status))

    raise Exception(str(message))
