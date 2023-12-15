"""Example of using the Vectara REST API to list the API Keys."""

from dataclasses import dataclass
import logging
from typing import Union

import requests


@dataclass
class CorpusData:
    """CorpusData class."""

    corpus_id: int
    corpus_name: str


@dataclass
class KeyData:
    """KeyData class."""

    key_id: str
    description: str
    key_type: str
    enabled: bool
    corpora: list[CorpusData]


def list_apikeys(
    customer_id: int,
    jwt_token: str,
) -> tuple[Union[list[KeyData], str], bool]:
    """Retrieves the list of API keys.

    Args:
        customer_id: Unique customer ID in vectara platform.
        jwt_token: JWT token to be used for authentication.

    Returns:
        (message, True) in case of success and returns (status, False) in case of failure.
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
        return str(response), False

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
            return result, True

        logging.error("ListApiKeys failed with status %s", status)
        return str(status), False

    return str(message), False
