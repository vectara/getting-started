"""Main file for Vectara feature examples related to User Management."""

import argparse
import logging
import sys

from user import create_user
from user import delete_user
from user import disable_user
from user import list_users
from utils import utils


def main() -> None:
    """Main function."""
    parser = argparse.ArgumentParser(
        description="Vectara example to perform different operations related to user management."
    )

    parser.add_argument(
        "--customer-id",
        type=int,
        required=True,
        help="Unique customer ID in Vectara platform.",
    )
    parser.add_argument(
        "--app-client-id",
        required=True,
        help="This app client should have enough rights.",
    )
    parser.add_argument("--app-client-secret", required=True)
    parser.add_argument(
        "--auth-url", required=True, help="The auth url for this customer."
    )

    args = parser.parse_args()

    jwt_token = utils.get_jwt_token(
        args.auth_url, args.app_client_id, args.app_client_secret
    )
    if not jwt_token:
        logging.error("Failed to get JWT token.")
        sys.exit(1)

    users = list_users.list_users(args.customer_id, jwt_token)
    logging.info("ListUsers response: %s", users)

    user_id: int = create_user.create_user(args.customer_id, jwt_token)
    logging.info("CreateUser created user id: %d", user_id)

    disable_user.disable_user(args.customer_id, user_id, jwt_token)
    logging.info("DisableUser disabled user id: %d", user_id)

    delete_user.delete_user(args.customer_id, user_id, jwt_token)
    logging.info("DeleteUser deleted user id: %d", user_id)


if __name__ == "__main__":
    logging.basicConfig(
        format="%(asctime)s %(levelname)-8s %(message)s", level=logging.INFO
    )
    main()
