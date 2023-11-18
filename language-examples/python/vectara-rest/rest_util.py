"""Utility functions for interacting with Vectara over REST."""

from authlib.integrations import requests_client


def get_jwt_token(auth_url: str, app_client_id: str, app_client_secret: str):
    """Connects to the server and returns a JWT token."""
    token_endpoint = f"{auth_url}"
    session = requests_client.OAuth2Session(
        app_client_id, app_client_secret, scope="")
    token = session.fetch_token(token_endpoint, grant_type="client_credentials")
    return token["access_token"]
