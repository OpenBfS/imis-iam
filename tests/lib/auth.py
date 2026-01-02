#!/usr/bin/python3
"""
Authentication helper for Keycloak
"""

import os
import requests


# Test users with their roles and permissions
# chefredakteur: Can see and edit everything
# redakteur: Can see and edit users in the same organization
# exampleuser: Can only edit own details and view other users in the same organization
TEST_USERS = {
    "chefredakteur": "secret",
    "redakteur": "secret",
    "exampleuser": "secret",
}


class KeycloakAuth:
    def __init__(self,
                 keycloak_url: str = None,
                 realm: str = None,
                 client_id: str = None,
                 client_secret: str = None,
                 username: str = None,
                 password: str = None):
        """
        Initialize Keycloak authentication.

        Args:
            keycloak_url: Keycloak base URL (default from environment)
            realm: Realm name (default from environment)
            client_id: OAuth2 client ID (default from environment)
            client_secret: OAuth2 client secret (default from environment)
            username: Default username (default from environment)
            password: Default password (default from environment)
        """
        self.keycloak_url = keycloak_url or os.getenv('KEYCLOAK_URL', 'http://localhost:48080')
        self.realm = realm or os.getenv('REALM', 'imis3')
        self.client_id = client_id or os.getenv('CLIENT_ID', 'iam-client')
        self.client_secret = client_secret or os.getenv('CLIENT_SECRET', 'exampleclientsecret')
        self.default_username = username or 'chefredakteur'
        self.default_password = password or 'secret'

        self._token_cache = {}
        self._current_user = None
        self._current_token = None

    def get_access_token(self, username: str = None, password: str = None, realm: str = None, client_id: str = None) -> str:
        """
        Get access token from Keycloak.
        """
        username = username or self.default_username
        password = password or self.default_password
        realm = realm or self.realm
        client_id = client_id or self.client_id

        # Check cache first
        cached_token = self._token_cache.get(username)
        if cached_token:
            return cached_token

        # Request new token
        token_url = f"{self.keycloak_url}/realms/{realm}/protocol/openid-connect/token"

        data = {
            'client_id': client_id,
            'client_secret': self.client_secret,
            'grant_type': 'password',
            'username': username,
            'password': password
        }

        try:
            response = requests.post(token_url, data=data)
            response.raise_for_status()

            token_data = response.json()
            access_token = token_data.get('access_token')

            if not access_token:
                raise Exception(f"No access token in response for user {username}")

            # Cache the token
            self._token_cache[username] = access_token

            return access_token

        except requests.RequestException as e:
            raise Exception(f"Failed to get access token for {username}: {str(e)}")

    def get_token_for_role(self, role: str) -> str:
        """
        Get access token for a specific test user role.
        """
        if role not in TEST_USERS:
            raise ValueError(f"Unknown role: {role}.")

        return self.get_access_token(role, TEST_USERS[role])

    def switch_user_context(self, role: str):
        """
        Switch to a different user context for testing.
        """
        self._current_user = role
        self._current_token = self.get_token_for_role(role)

    def clear_user_context(self):
        """Clear current user context (return to default user)"""
        self._current_user = None
        self._current_token = None

    def get_current_token(self) -> str:
        """
        Get token for current context.
        """
        return self._current_token or self.get_access_token()

    def delete_user_via_admin_api(self, user_id: str) -> bool:
        """
        Delete a user using Keycloak Admin account.

        This does not work and returns a 409 Conflict
        User account cannot be deleted via keycloak because of intentionally
        missing DELETE CASCADEs on foreign keys.
        Use db_helpers.delete_user_from_db instead.
        """
        raise NotImplementedError("This is not working. Use 'db_helpers.delete_user_from_db'.")
        # Get admin token
        admin_token = self.get_access_token("admin", "secret",
                                            realm="master",
                                            client_id="security-admin-console")

        # Use Keycloak Admin API to delete user
        admin_url = f"{self.keycloak_url}/admin/realms/{self.realm}/users/{user_id}"

        headers = {
            "Authorization": f"Bearer {admin_token}",
            "Content-Type": "application/json"
        }

        response = requests.delete(admin_url, headers=headers)
        response.raise_for_status()

        if response.status_code == 204:
            return True
        elif response.status_code == 404:
            # User already deleted
            return True
        else:
            raise ValueError(f"Failed to delete user {user_id}: HTTP {response.status_code}")
            return False


# Global auth instance
_auth_instance = KeycloakAuth()


def get_auth() -> KeycloakAuth:
    """Get or create global auth instance"""
    global _auth_instance
    return _auth_instance
