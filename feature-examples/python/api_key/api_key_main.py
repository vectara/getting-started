"""Main file for Vectara feature examples related to ApiKey."""
import argparse
import logging
import sys

from api_key import create_api_key
from api_key import delete_api_key
from api_key import enable_api_key
from api_key import list_api_key
from utils import utils


def main() -> None:
    """Main function."""
    parser = argparse.ArgumentParser(
        description="Vectara example to perform different operations related to API keys."
    )

    parser.add_argument(
        "--customer-id",
        type=int,
        required=True,
        help="Unique customer ID in Vectara platform.",
    )
    parser.add_argument(
        "--corpus-id",
        type=int,
        required=True,
        help="Corpus ID to which API key will be created.",
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

    response, status = create_api_key.create_apikey(
        args.customer_id, args.corpus_id, jwt_token
    )
    logging.info("CreateApiKey response: %s, status: %s", response, status)

    if not status:
        sys.exit(1)

    api_key = response
    response, status = list_api_key.list_apikeys(args.customer_id, jwt_token)
    logging.info("ListApiKeys response: %s, status: %s", response, status)

    response, status = enable_api_key.enable_apikey(
        args.customer_id, api_key, jwt_token, False  # Disable the API key.
    )
    logging.info("DisableApiKey response: %s, status: %s", response, status)

    response, status = delete_api_key.delete_apikey(args.customer_id, api_key, jwt_token)
    logging.info("DeleteApiKey response: %s, status: %s", response, status)


if __name__ == "__main__":
    logging.basicConfig(
        format="%(asctime)s %(levelname)-8s %(message)s", level=logging.INFO
    )
    main()
