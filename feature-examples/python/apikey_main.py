"""Main file for Vectara feature examples related to ApiKey."""
import argparse
import logging
import sys

import create_apikey
import delete_apikey
import enable_apikey
import list_apikeys
import vectara_util

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

    jwt_token = vectara_util.get_jwt_token(args.auth_url,
                                           args.app_client_id,
                                           args.app_client_secret)
    if not jwt_token:
        logging.error("Failed to get JWT token.")
        sys.exit(1)

    response, status = create_apikey.create_apikey(
        args.customer_id,
        args.corpus_id,
        jwt_token
    )
    logging.info("CreateApiKey response: %s, status: %s", response, status)

    if not status:
        sys.exit(1)

    api_key = response
    response, status = list_apikeys.list_apikeys(
        args.customer_id,
        jwt_token
    )
    logging.info("ListApiKeys response: %s, status: %s", response, status)

    response, status = enable_apikey.enable_apikey(
        args.customer_id,
        api_key,
        jwt_token,
        False # Disable the API key.
    )
    logging.info("DisableApiKey response: %s, status: %s", response, status)

    response, status = delete_apikey.delete_apikey(
        args.customer_id,
        api_key,
        jwt_token
    )
    logging.info("DeleteApiKey response: %s, status: %s", response, status)


if __name__ == "__main__":
    logging.basicConfig(
        format="%(asctime)s %(levelname)-8s %(message)s", level=logging.INFO
    )
    main()
