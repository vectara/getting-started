"""Example of using the Vectara REST API to delete an API Key."""

import logging
from typing import Any

import requests


def delete_apikey(
    customer_id: int,
    key_id: str,
    jwt_token: str,
) -> tuple[Any, bool]:
    """Deletes an API key.

    Args:
        customer_id: Unique customer ID in vectara platform.
        key_id: API key ID to be deleted.
        jwt_token: JWT token to be used for authentication.

    Returns:
        (None, True) in case of success and returns (error, False) in case of failure.
    """
    post_headers = {
        "customer-id": f"{customer_id}",
        "Authorization": f"Bearer {jwt_token}",
    }

    # A request can contain multiple api keys. We are deleting only one.
    request = {"keyId": [key_id]}

    response = requests.post(
        "https://api.vectara.io/v1/delete-api-key",
        json=request,
        verify=True,
        headers=post_headers,
        timeout=50,
    )

    if response.status_code != 200:
        logging.error(
            "DeleteApiKey failed with code %d, reason %s, text %s",
            response.status_code,
            response.reason,
            response.text,
        )
        return response, False

    message = response.json()
    if message["status"]:
        if len(message["status"]) != 1:
            logging.error("DeleteApiKey failed with status %s", message["status"])
            return message["status"], False

        status = message["status"][0]
        if status["code"] == "OK":
            return None, True

        logging.error("DeleteApiKey failed with status %s", status)
        return status, False

    return message, False
