"""Example of using the Vectara REST API to delete a User."""

import logging

import requests

from user import exceptions


def delete_user(
    customer_id: int,
    user_id: int,
    jwt_token: str,
) -> None:
    """Deletes a User.

    Args:
        customer_id: Unique customer ID in vectara platform.
        user_id: ID of the user to be deleted.
        jwt_token: JWT token to be used for authentication.

    Returns:
        None.

    Raises:
        UserException: In case of any error.
    """
    post_headers = {
        "customer-id": f"{customer_id}",
        "Authorization": f"Bearer {jwt_token}",
    }

    # A request can contain multiple users. We are deleting only one.
    request = {
        "userAction": [
            {
                "user": {
                    "id": user_id,  # ID of the user to be deleted.
                },
                "userActionType": "USER_ACTION_TYPE__DELETE"
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
            "DeleteUser failed with code %d, reason: %s, text: %s",
            response.status_code,
            response.reason,
            response.text,
        )
        raise exceptions.UserException(str(response))

    message = response.json()
    if message["response"]:
        if len(message["response"]) != 1:
            logging.error("DeleteUser failed with response %s", message["response"])
            raise exceptions.UserException(str(message))

        status = message["response"][0]["status"]
        if status["code"] == "OK":
            return

        logging.error("DeleteUser failed with status %s", status)
        raise exceptions.UserException(str(status))

    raise exceptions.UserException(str(message))
