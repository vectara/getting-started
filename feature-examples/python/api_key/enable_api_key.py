"""Example of using the Vectara REST API to enable or disable an API Key."""

import logging
from typing import Any

import requests


def enable_apikey(
    customer_id: int,
    key_id: str,
    jwt_token: str,
    enable: bool,
) -> tuple[Any, bool]:
    """Enables or disables an API key.

    Args:
        customer_id: Unique customer ID in vectara platform.
        key_id: API key ID to be enabled or disabled.
        jwt_token: JWT token to be used for authentication.
        enable: True to enable, False to disable.

    Returns:
        (None, True) in case of success and returns (error, False) in case of failure.
    """
    post_headers = {
        "customer-id": f"{customer_id}",
        "Authorization": f"Bearer {jwt_token}",
    }

    # A request can contain enable/disable request for multiple api keys.
    request = {
        "keyEnablement": [
            {
                "keyId": key_id,
                "enable": enable,
            }
        ]
    }

    response = requests.post(
        "https://api.vectara.io/v1/enable-api-key",
        json=request,
        verify=True,
        headers=post_headers,
        timeout=50,
    )

    if response.status_code != 200:
        logging.error(
            "%s failed with code %d, reason %s, text %s",
            "EnableApiKey" if enable else "DisableApiKey",
            response.status_code,
            response.reason,
            response.text,
        )
        return response, False

    message = response.json()
    if message["status"]:
        if len(message["status"]) != 1:
            logging.error("%s failed with status %s",
                          "EnableApiKey" if enable else "DisableApiKey",
                          message["status"])
            return message["status"], False

        status = message["status"][0]
        if status["code"] == "OK":
            return None, True

        logging.error("%s failed with status %s",
                      "EnableApiKey" if enable else "DisableApiKey",
                      status)
        return status, False

    return message, False
