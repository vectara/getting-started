"""Example of using the Vectara REST API to read usage metrics."""

import logging

import requests

from corpus import data_objects
from corpus import exceptions


def read_usage_metrics(
    customer_id: int,
    corpus_id: int,
    jwt_token: str,
) -> list[data_objects.QueryUsageData]:
    """Reads usage metrics for a corpus.

    Args:
        customer_id: Unique customer ID in vectara platform.
        corpus_id: Corpus ID for which usage metrics are to be read.
        jwt_token: JWT token to be used for authentication.

    Returns:
        List of UsageData objects aggregated by interval.

    Raises:
        CorpusException: In case of any error.
    """
    post_headers = {
        "customer-id": f"{customer_id}",
        "Authorization": f"Bearer {jwt_token}",
    }

    # corpusId in request can be left unset to retrieve usage metrics for all the corpora.
    request = {
        "corpusId": corpus_id,
        "window": {
            "startEpochSecs": 1703462400, # 2023-12-25T00:00:00.00Z
            "endEpochSecs": 1704196800 # 2024-01-02T12:00:00.00Z
        },
        "type": "METRICTYPE__SERVING",
        "aggreagationIntervalSecs": "3600",  # 1 Hour
    }

    response = requests.post(
        "https://api.vectara.io/v1/get-usage-metrics",
        json=request,
        verify=True,
        headers=post_headers,
        timeout=50,
    )

    if response.status_code != 200:
        logging.error(
            "ReadUsageMetrics failed with code %d, reason: %s, text: %s",
            response.status_code,
            response.reason,
            response.text,
        )
        raise exceptions.CorpusException(str(response))

    message = response.json()
    if message["status"]:
        if message["status"]["code"] != "OK":
            raise exceptions.CorpusException(str(message["status"]))

        return [
            data_objects.QueryUsageData(
                rows_read=usage["servingValue"]["rowsRead"],
                query_count=usage["servingValue"]["queryCount"],
                start_time=usage["servingValue"]["startEpochSecs"],
            )
            for usage in message["values"]
        ]

    raise exceptions.CorpusException(str(message))
