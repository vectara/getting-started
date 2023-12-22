"""Example of using the Vectara REST API to create a User."""

import logging

import requests

from utils import error_handling


def create_user(
    customer_id: int,
    jwt_token: str,
) -> int:
    """Creates a User.

    Args:
        customer_id: Unique customer ID in vectara platform.
        jwt_token: JWT token to be used for authentication.

    Returns:
        ID of the created user.

    Raises:
        UserException: In case of any error.
    """
    post_headers = {
        "customer-id": f"{customer_id}",
        "Authorization": f"Bearer {jwt_token}",
    }

    # A request can contain multiple users. We are creating only one.
    request = {
        "userAction": [
            {
                "user": {
                    "handle": "testUser1",
                    "email": "testUser1@test.com",
                    "type": 1,
                    "role": [
                        2
                    ],  # 1 - Owner 2 - Admin, 3 - Billing Admin, 4 - Corpus Admin.
                },
                "userActionType": 1,  # 1 for create.
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
            "CreateUser failed with code %d, reason %s, text %s",
            response.status_code,
            response.reason,
            response.text,
        )
        raise error_handling.UserException(str(response))

    message = response.json()
    if message["response"]:
        if len(message["response"]) != 1:
            logging.error("CreateUser failed with response %s", message["response"])
            raise error_handling.UserException(str(message))

        status = message["response"][0]["status"]
        if status["code"] == "OK":
            return message["response"][0]["user"]["id"]

        logging.error("CreateUser failed with status %s", status)
        raise error_handling.UserException(str(status))

    raise error_handling.UserException(str(message))
