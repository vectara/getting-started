"""Example of using the Vectara REST API to list the Users."""

import dataclasses
import logging

import requests

from user import exceptions


@dataclasses.dataclass(frozen=True)
class UserData:
    """User data such as id, name, comment, status of user etc."""

    user_id: int
    name: str
    email: str
    type: str
    comment: str
    status: str


def _users_from_message(message: dict) -> list[UserData]:
    """Helper function to parse the response message."""
    result: list[UserData] = []
    for user in message["user"]:
        result.append(
            UserData(
                user_id=user["id"],
                name=user["handle"],
                email=user["email"],
                type=user["type"],
                comment=user["comment"],
                status=user["userStatus"],
            )
        )
    return result


def list_users(
    customer_id: int,
    jwt_token: str,
) -> list[UserData]:
    """Retrieves the list of Users.

    Args:
        customer_id: Unique customer ID in vectara platform.
        jwt_token: JWT token to be used for authentication.

    Returns:
        list of UserData objects.

    Raises:
        UserException: In case of any error.
    """
    post_headers = {
        "customer-id": f"{customer_id}",
        "Authorization": f"Bearer {jwt_token}",
    }

    request = {"listUsersType": "LIST_USERS_TYPE__ALL", "numResults": 10}

    response = requests.post(
        "https://api.vectara.io/v1/list-users",
        json=request,
        verify=True,
        headers=post_headers,
        timeout=50,
    )

    if response.status_code != 200:
        logging.error(
            "ListUsers failed with code %d, reason: %s, text: %s",
            response.status_code,
            response.reason,
            response.text,
        )
        raise exceptions.UserException(str(response))

    message = response.json()
    if message["status"] is None:
        # The old API does not set the status field in response.
        return _users_from_message(message)

    status = message["status"]
    if status["code"] == "OK":
        return _users_from_message(message)

    logging.error("ListUsers failed with status %s", status)
    raise exceptions.UserException(str(status))
