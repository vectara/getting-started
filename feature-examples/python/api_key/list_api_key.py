"""Example of using the Vectara REST API to list the Api Keys."""

import logging
from typing import Any

import requests


def list_apikeys(
    customer_id: int,
    jwt_token: str,
) -> tuple[Any, bool]:
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

    request = {"numResults": 10}

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
        return response, False

    message = response.json()
    if message["status"]:
        status = message["status"][0]
        if status["code"] == "OK":
            return message, True

        logging.error("ListApiKeys failed with status %s", status)
        return status, False

    return message, False
