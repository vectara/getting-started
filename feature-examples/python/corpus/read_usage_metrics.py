"""Example of using the Vectara REST API to read usage metrics."""

import dataclasses
import logging

import requests

from corpus import exceptions


@dataclasses.dataclass(frozen=True)
class QueryUsageData:
    """Query usage information"""

    rows_read: int
    query_count: int
    start_time: int


def read_usage_metrics(
    customer_id: int,
    corpus_id: int,
    jwt_token: str,
) -> list[QueryUsageData]:
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

    request = {
        "corpusId": corpus_id,
        "window": {
            "absoluteWindow": {
                "start": "2023-12-25T00:00:00.00Z",
                "end": "2024-01-02T11:45:00.00Z",
            }
        },
        "type": "METRICTYPE__SERVING",
        "interval": "PT1H",  # 1 Hour
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
            QueryUsageData(
                rows_read=usage["servingValue"]["rowsRead"],
                query_count=usage["servingValue"]["queryCount"],
                start_time=usage["servingValue"]["start"],
            )
            for usage in message["values"]
        ]

    raise exceptions.CorpusException(str(message))
