""" This is an example of calling Vectara API via python using gRPC as communication protocol.
"""

import argparse
import json
import logging
import struct

from authlib.integrations.requests_client import OAuth2Session
import grpc

import admin_pb2
import indexing_pb2
import services_pb2
import services_pb2_grpc
import serving_pb2


INDEXING_DATA = [
    {
        "title": "21 Lessons for the 21st Century",
        "author": "Yuval Noah Harari",
        "excerpt": "How do computers and robots change the meaning of being human? How do we " +
                   "deal with the epidemic of fake news? Are nations and religions still " +
                   "relevant? What should we teach our children?",
        "genre": "Philosophy"
    },
    {
        "title": "Mars Rover Curiosity: An Inside Account from Curiosity's Chief Engineer",
        "author": "Rob Manning",
        "excerpt": "In the course of our enduring quest for knowledge about ourselves and our " +
                   "universe, we haven't found answers to one of our most fundamental questions: " +
                   "Does life exist anywhere else in the universe?",
        "genre": "Science"
    },
]

def _get_jwt_token(auth_url: str, app_client_id: str, app_client_secret: str):
    """Connect to the server and get a JWT token."""
    token_endpoint = f"{auth_url}/oauth2/token"
    session = OAuth2Session(
        app_client_id, app_client_secret, scope="")
    token = session.fetch_token(token_endpoint, grant_type="client_credentials")
    return token["access_token"]


def generate_index_data():
    """ Generates some example indexing data. """
    documents = []
    for i, book in enumerate(INDEXING_DATA):
        document = indexing_pb2.Document()
        document.document_id = f"doc-id-example-{i}"
        document.title = book["title"]
        document.metadata_json = json.dumps(
            {
                "book-name": book["title"],
                "collection": book["genre"],
                "author": book["author"]
            }
        )
        section = indexing_pb2.Section()
        section.text = book["excerpt"]
        document.section.extend([section])
        documents.append(document)
    return documents


def index(customer_id: int, corpus_id: int, idx_address: str, jwt_token: str):
    """ Indexes data to the corpus.
    Args:
        customer_id: Unique customer ID in vectara platform.
        corpus_id: ID of the corpus to which data needs to be indexed.
        idx_address: Address of the indexing server. e.g., indexing.vectara.io
        jwt_token: A valid Auth token.

    Returns:
        (None, True) in case of success and returns (error, False) in case of failure.

    """

    logging.info("Indexing data into the corpus.")
    documents = generate_index_data()
    for document in documents:
        index_req = services_pb2.IndexDocumentRequest()
        index_req.customer_id = customer_id
        index_req.corpus_id = corpus_id
        index_req.document.MergeFrom(document)

        try:
            index_stub = services_pb2_grpc.IndexServiceStub(
                grpc.secure_channel(idx_address, grpc.ssl_channel_credentials()))
            
            # Vectara API expects customer_id as a 64-bit binary encoded value in the metadata of
            # all grpcs calls. Following line generates the encoded value from customer ID.
            packed_customer_id = struct.pack(">q", customer_id)
            response = index_stub.Index(index_req,
                                        credentials=grpc.access_token_call_credentials(jwt_token),
                                        metadata=[("customer-id-bin", packed_customer_id)])
            logging.info("Indexed document successful: %s", response)
        except grpc.RpcError as rpc_error:
            return rpc_error, False
    return None, True


def query(customer_id: int, corpus_id: int, query_address: str, jwt_token: str, query: str):
    """This method queries the data.
    Args:
        customer_id: Unique customer ID in vectara platform.
        corpus_id: ID of the corpus to which data needs to be indexed.
        query_address: Address of the querying server. e.g., serving.vectara.io
        jwt_token: A valid Auth token.

    Returns:
        (None, True) in case of success and returns (error, False) in case of failure.

    """

    corpus_key = serving_pb2.CorpusKey()
    corpus_key.corpus_id = corpus_id
    corpus_key.customer_id = customer_id

    request = serving_pb2.QueryRequest()
    request.query = query
    request.num_results = 10
    request.corpus_key.extend([corpus_key])

    batch_request = serving_pb2.BatchQueryRequest()
    batch_request.query.extend([request])

    try:
        query_stub = services_pb2_grpc.QueryServiceStub(
            grpc.secure_channel(query_address, grpc.ssl_channel_credentials()))
        packed_customer_id = struct.pack(">q", customer_id)
        response = query_stub.Query(batch_request,
                                    credentials=grpc.access_token_call_credentials(jwt_token),
                                    metadata=[("customer-id-bin", packed_customer_id)])
        logging.info("Query succeeded with response: %s", response)
        return None, True
    except grpc.RpcError as rpc_error:
        return rpc_error, False


def create_corpus(customer_id: int, admin_address: str, jwt_token: str):
    """Create a corpus.
    Args:
        customer_id: Unique customer ID in vectara platform.
        admin_address: Address of the admin server. e.g., admin.vectara.io
        jwt_token: A valid Auth token.

    Returns:
        (None, True) in case of success and returns (error, False) in case of failure.
    """

    create_corpus_request = admin_pb2.CreateCorpusRequest()
    corpus = admin_pb2.Corpus()
    corpus.name = "Vectara-test-corpus"
    corpus.description = "Corpus created from Vectara demo."
    logging.info("Creating corpus in customer %d with name: %s", customer_id, corpus.name)
    create_corpus_request.corpus.CopyFrom(corpus)

    try:
        admin_stub = services_pb2_grpc.AdminServiceStub(
            grpc.secure_channel(admin_address, grpc.ssl_channel_credentials()))
        packed_customer_id = struct.pack(">q", customer_id)

        response = admin_stub.CreateCorpus(
            create_corpus_request,
            credentials=grpc.access_token_call_credentials(jwt_token),
            metadata=[("customer-id-bin", packed_customer_id)])

        if response.status.status_detail == "Corpus Created":
            logging.info("Corpus %d created successfully.", response.corpus_id)
            return None, True
    except grpc.RpcError as rpc_error:
        logging.error(rpc_error)
        return rpc_error, False



if __name__ == "__main__":
    logging.basicConfig(
        format="%(asctime)s %(levelname)-8s %(message)s", level=logging.INFO)

    parser = argparse.ArgumentParser(description="Vectara gRPC example")

    parser.add_argument("--customer-id", type=int, help="Unique customer ID in Vectara platform.")
    parser.add_argument("--corpus-id",
                        type=int,
                        help="Corpus ID to which data will be indexed and queried from.")

    parser.add_argument("--admin-endpoint", help="The endpoint of admin server.",
                        default="admin.vectara.io")
    parser.add_argument("--indexing-endpoint", help="The endpoint of indexing server.",
                        default="indexing.vectara.io")
    parser.add_argument("--serving-endpoint", help="The endpoint of querying server.",
                        default="serving.vectara.io")

    parser.add_argument("--app-client-id", help="This app client should have enough rights.")
    parser.add_argument("--app-client-secret")
    parser.add_argument("--auth-url", help="The authentication URL for this customer.")
    parser.add_argument("--query", help="Query to run against the corpus.", default="Test query")

    args = parser.parse_args()

    if args:
        token = _get_jwt_token(args.auth_url, args.app_client_id, args.app_client_secret)

        if token:
            error, status = index(args.customer_id,
                                  args.corpus_id,
                                  args.indexing_endpoint,
                                  token)
            error, status = query(args.customer_id,
                                  args.corpus_id,
                                  args.serving_endpoint,
                                  token,
                                  args.query)
            error, status = create_corpus(args.customer_id,
                                          args.admin_endpoint,
                                          token)
