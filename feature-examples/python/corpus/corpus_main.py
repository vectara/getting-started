"""Main file for Vectara feature examples related to Corpus Management."""

import argparse
import logging
import sys

from corpus import compute_corpus_size
from corpus import disable_corpus
from corpus import read_corpus
from corpus import read_usage_metrics
from utils import utils


def main() -> None:
    """Main function."""
    parser = argparse.ArgumentParser(
        description="Vectara example to perform different operations related to corpus management."
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
        help="Corpus ID on which operations will be performed.",
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

    corpus = read_corpus.read_corpus(args.customer_id, args.corpus_id, jwt_token)
    logging.info("ReadCorpus response: %s", corpus)

    corpus_size = compute_corpus_size.compute_corpus_size(args.customer_id,
                                                          args.corpus_id,
                                                          jwt_token)
    logging.info("ComputeCorpusSize response: %s", corpus_size)

    disable_corpus.disable_corpus(args.customer_id, args.corpus_id, jwt_token)
    logging.info("DisableCorpus corpus id: %d", args.corpus_id)

    usage_data = read_usage_metrics.read_usage_metrics(args.customer_id, args.corpus_id, jwt_token)
    logging.info("ReadUsageMetrics response: %s", usage_data)


if __name__ == "__main__":
    logging.basicConfig(
        format="%(asctime)s %(levelname)-8s %(message)s", level=logging.INFO
    )
    main()
