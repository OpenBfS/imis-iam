#!/usr/bin/python3
"""
API helper functions for iam_spi testing.
"""

import os
import requests
from typing import Optional, Dict, Any
from .auth import get_auth


class APIClient:
    """Client for iam_spi API requests"""

    def __init__(self, client_url: str = None, realm: str = None):
        """
        Initialize API client.
        """
        self.client_url = client_url or os.getenv('CLIENT_URL', 'http://localhost:48081')
        self.realm = realm or os.getenv('REALM', 'imis3')
        self.base_url = f"{self.client_url}/realms/{self.realm}/iam"
        self.timeout = int(os.getenv('REQUEST_TIMEOUT', '30'))

    def _get_headers(self, token: str = None) -> Dict[str, str]:
        """
        Get request headers with authentication.
        """
        if token is None:
            token = get_auth().get_current_token()

        return {
            'Authorization': f'Bearer {token}',
            'Accept': 'application/json',
            'Content-Type': 'application/json'
        }

    def get(self, endpoint: str, params: Dict[str, Any] = None, token: str = None) -> requests.Response:
        """
        Authenticated GET request.
        """
        url = f"{self.base_url}{endpoint}"
        headers = self._get_headers(token)

        response = requests.get(
            url,
            headers=headers,
            params=params,
            timeout=self.timeout
        )

        return response

    def post(self, endpoint: str, data: Dict[str, Any] = None, token: str = None) -> requests.Response:
        """
        Authenticated POST request.
        """
        url = f"{self.base_url}{endpoint}"
        headers = self._get_headers(token)

        response = requests.post(
            url,
            headers=headers,
            json=data,
            timeout=self.timeout
        )

        return response

    def put(self, endpoint: str, data: Dict[str, Any] = None, token: str = None) -> requests.Response:
        """
        Authenticated PUT request.
        """
        url = f"{self.base_url}{endpoint}"
        headers = self._get_headers(token)

        response = requests.put(
            url,
            headers=headers,
            json=data,
            timeout=self.timeout
        )

        return response

    def delete(self, endpoint: str, token: str = None) -> requests.Response:
        """
        Authenticated DELETE request.
        """
        url = f"{self.base_url}{endpoint}"
        headers = self._get_headers(token)

        response = requests.delete(
            url,
            headers=headers,
            timeout=self.timeout
        )

        return response


# Global API client instance
_api_client = APIClient()


def get_api_client() -> APIClient:
    """Get or create global API client instance"""
    global _api_client
    return _api_client


# functions to use the global client

def api_get(endpoint: str, params: Dict[str, Any] = None) -> requests.Response:
    """Make GET request using global client"""
    return get_api_client().get(endpoint, params)


def api_post(endpoint: str, data: Dict[str, Any] = None) -> requests.Response:
    """Make POST request using global client"""
    return get_api_client().post(endpoint, data)


def api_put(endpoint: str, data: Dict[str, Any] = None) -> requests.Response:
    """Make PUT request using global client"""
    return get_api_client().put(endpoint, data)


def api_delete(endpoint: str) -> requests.Response:
    """Make DELETE request using global client"""
    return get_api_client().delete(endpoint)


def create_search_query(**filters) -> str:
    """
    Create search query string from filters.

    Args:
        **filters: key-value pairs

    Returns:
        Formatted search query string

    Example:
        >>> create_search_query(role="user", network="network1")
        'role:user network:network1'
    """
    return " ".join([f'{key}:"{value}"' for key, value in filters.items()])


def generate_test_username(prefix: str = "testuser") -> str:
    """Generate a random test username"""
    import random
    import string
    suffix = ''.join(random.choices(string.ascii_lowercase + string.digits, k=8))
    return f"{prefix}_{suffix}"
