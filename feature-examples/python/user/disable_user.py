"""Example of using the Vectara REST API to disable a User."""

import logging

import requests

from utils import error_handling


def disable_user(
    customer_id: int,
    user_id: int,
    jwt_token: str,
) -> bool:
    """Disables a User.

    Args:
        customer_id: Unique customer ID in vectara platform.
        user_id: ID of the user to be disabled.
        jwt_token: JWT token to be used for authentication.

    Returns:
        True/False indicating Success or Failure.

    Raises:
        UserException: In case of any error.
    """
    post_headers = {
        "customer-id": f"{customer_id}",
        "Authorization": f"Bearer {jwt_token}",
    }

    # A request can contain multiple users. We are disabling only one.
    request = {
        "userAction": [
            {
                "user": {
                    "id": user_id,  # ID of the user to be disabled.
                },
                "userActionType": 3,  # 3 for disable, 4 for enable.
            }
        ]
    }

    response = requests.post(
        "https://api.vectara.io/v1/manage-user",
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
        raise error_handling.UserException(str(response))

    message = response.json()
    if message["response"]:
        if len(message["response"]) != 1:
            logging.error("DisableUser failed with response %s", message["response"])
            raise error_handling.UserException(str(message))

        status = message["response"][0]["status"]
        if status["code"] == "OK":
            return True

        logging.error("DisableUser failed with status %s", status)
        raise error_handling.UserException(str(status))

    raise error_handling.UserException(str(message))
