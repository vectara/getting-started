"""Example of calling Vectara API via python using HTTP/REST as communication protocol."""

import argparse
import logging
from rest_create_corpus import create_corpus
from rest_index_document import index_document
from rest_delete_document import delete_document
from rest_query import query
from rest_upload_file import upload_file
from rest_util import _get_jwt_token

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
        token = _get_jwt_token(args.auth_url, args.app_client_id, args.app_client_secret)

        if token:
            error, status = create_corpus(args.customer_id,
                                          args.admin_endpoint,
                                          token)
            logging.info("Create Corpus response: %s", error.text)
            error, status = upload_file(args.customer_id,
                                        args.corpus_id,
                                        args.indexing_endpoint,
                                        token)
            logging.info("Upload File response: %s", error.text)
            error, status = index_document(args.customer_id,
                                           args.corpus_id,
                                           args.indexing_endpoint,
                                           token)
            logging.info("Index Document response: %s", error.text)
            error, status = delete_document(args.customer_id,
                                            args.corpus_id,
                                            args.indexing_endpoint,
                                            token,
                                            "doc-id-2")
            logging.info("Delete Document response: %s", error.text)
            error, status = query(args.customer_id,
                                  args.corpus_id,
                                  args.serving_endpoint,
                                  token,
                                  args.query)
            logging.info("Query response: %s", error.text)
        else:
            logging.error("Could not generate an auth token. Please check your credentials.")
