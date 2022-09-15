""" This is an example of calling Vectara API via python using gRPC as communication protocol.
"""

import argparse
import logging
import struct

import grpc

import services_pb2_grpc
import serving_pb2


def query(customer_id: int, corpus_id: int, query_address: str, api_key: str, query: str):
    """This method queries the data.
    Args:
        customer_id: Unique customer ID in vectara platform.
        corpus_id: ID of the corpus to which data needs to be indexed.
        query_address: Address of the querying server. e.g., serving.vectara.io
        api_key: A valid API key with query access on the corpus.

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
                                    metadata=[("customer-id-bin", packed_customer_id),
                                              ("x-api-key", api_key)])
        logging.info("Query succeeded with response: %s", response)
        return None, True
    except grpc.RpcError as rpc_error:
        return rpc_error, False


if __name__ == "__main__":
    logging.basicConfig(
        format="%(asctime)s %(levelname)-8s %(message)s", level=logging.INFO)

    parser = argparse.ArgumentParser(
                description="Vectara gRPC example (with API Key authentication)")

    parser.add_argument("--customer-id", type=int, help="Unique customer ID in Vectara platform.")
    parser.add_argument("--corpus-id",
                        type=int,
                        help="Corpus ID to which data will be indexed and queried from.")

    parser.add_argument("--serving-endpoint", help="The endpoint of querying server.",
                        default="serving.vectara.io")
    parser.add_argument("--api-key", help="API key retrieved from Vectara console.")
    parser.add_argument("--query", help="Query to run against the corpus.", default="Test query")

    args = parser.parse_args()
    if args:
        error, status = query(args.customer_id,
                              args.corpus_id,
                              args.serving_endpoint,
                              args.api_key,
                              args.query)
