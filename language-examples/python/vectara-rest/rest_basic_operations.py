"""Example of calling Vectara API via python using HTTP/REST as communication protocol."""

import argparse
import logging
import sys

import rest_create_corpus
import rest_delete_corpus
import rest_delete_document
import rest_index_document
import rest_query
import rest_upload_file
import rest_util

if __name__ == "__main__":
    logging.basicConfig(
        format="%(asctime)s %(levelname)-8s %(message)s", level=logging.INFO)

    parser = argparse.ArgumentParser(description="Vectara gRPC example")

    parser.add_argument("--customer-id", type=int, required=True,
                        help="Unique customer ID in Vectara platform.")
    parser.add_argument("--corpus-id", type=int, required=True,
                        help="Corpus ID to which data will be indexed and queried from.")
    parser.add_argument("--admin-endpoint", help="The endpoint of admin server.",
                        default="api.vectara.io")
    parser.add_argument("--indexing-endpoint", help="The endpoint of indexing server.",
                        default="api.vectara.io")
    parser.add_argument("--serving-endpoint", help="The endpoint of querying server.",
                        default="api.vectara.io")
    parser.add_argument("--app-client-id",  required=True,
                        help="This app client should have enough rights.")
    parser.add_argument("--app-client-secret", required=True)
    parser.add_argument("--auth-url",  required=True,
                        help="The cognito auth url for this customer.")
    parser.add_argument("--query", help="Query to run against the corpus.", default="Test query")

    args = parser.parse_args()

    if args:
        token = rest_util.get_jwt_token(args.auth_url, args.app_client_id, args.app_client_secret)

        if token:
            response, status = rest_create_corpus.create_corpus(args.customer_id,
                                                                args.admin_endpoint,
                                                                token)
            logging.info("Create Corpus response: %s", response)
            if not status:
                sys.exit(1)
            corpus_to_delete = response["corpusId"]

            response, status = rest_delete_corpus.delete_corpus(args.customer_id,
                                                                corpus_to_delete,
                                                                args.admin_endpoint,
                                                                token)
            logging.info("Delete Corpus response: %s", response)
            if not status:
                sys.exit(1)

            response, status = rest_upload_file.upload_file(args.customer_id,
                                                            args.corpus_id,
                                                            args.indexing_endpoint,
                                                            token)
            logging.info("Upload File response: %s", response)
            if not status:
                sys.exit(1)

            response, status = rest_index_document.index_document(args.customer_id,
                                                                  args.corpus_id,
                                                                  args.indexing_endpoint,
                                                                  token)
            logging.info("Index Document response: %s", response)
            if not status:
                sys.exit(1)

            response, status = rest_delete_document.delete_document(args.customer_id,
                                                                    args.corpus_id,
                                                                    args.indexing_endpoint,
                                                                    token,
                                                                    "doc-id-2")
            logging.info("Delete Document response: %s", response)

            response, status = rest_query.query(args.customer_id,
                                                args.corpus_id,
                                                args.serving_endpoint,
                                                token,
                                                args.query)
            logging.info("Query response: %s", response)
            if not status:
                sys.exit(1)
        else:
            logging.error("Could not generate an auth token. Please check your credentials.")
