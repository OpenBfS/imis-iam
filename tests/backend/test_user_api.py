#!/usr/bin/python3
"""
Tests for '/iam/user

TODO:
In an update request, if position is an invalid value, the API ignores the whole request without error
"""

import pytest
import requests
from lib import (
    api_get, api_post, api_put, api_delete,
    get_auth,
    generate_test_username, TEST_USERS,
    create_search_query,
    delete_user_from_db
)


class TestUserAPI:
    """Test suite for /iam/user endpoints"""

    def test_list_users(self):
        """Test GET /iam/user - List users"""
        response = api_get("/user", params={"maxResults": 10})
        response.raise_for_status()

        data = response.json()
        assert data["size"] == 3 == len(data["list"])

    def test_list_users_with_pagination(self):
        """Test GET /iam/user - List users with pagination"""
        response = api_get("/user", params={
            "firstResult": 0,
            "maxResults": 1
        })
        response.raise_for_status()

        data = response.json()
        assert len(data["list"]) <= 1

    def test_list_users_with_search(self):
        """Test GET /iam/user - List users with search"""
        response = api_get("/user", params={
            "search": create_search_query(firstName='Example'),
            "maxResults": 10
        })
        response.raise_for_status()

        data = response.json()
        assert data["size"] == 3

    def test_list_users_with_sorting(self):
        """Test GET /iam/user - List users with sorting"""
        response = api_get("/user", params={
            "sortBy": "username",
            "order": "asc",
            "maxResults": 5
        })
        response.raise_for_status()

        data = response.json()
        assert len(data["list"]) > 0 or data["size"] == 0

    def test_get_user_by_id(self):
        """Test GET /iam/user/{id} - Get user by ID"""
        # First get a list to find a user ID
        list_response = api_get("/user", params={"maxResults": 1})
        list_response.raise_for_status()

        users = list_response.json()["list"]
        if not users:
            pytest.skip("No users available for testing")

        user_id = users[0]["id"]

        # Now get the specific user
        response = api_get(f"/user/{user_id}")
        response.raise_for_status()

        user = response.json()
        assert user["id"] == user_id

    def test_get_current_user_profile(self):
        """Test GET /iam/user/profile - Get current user profile"""
        response = api_get("/user/profile")
        response.raise_for_status()

        profile = response.json()
        assert "attributes" in profile
        assert "username" in profile["attributes"]

    def test_get_available_roles(self):
        """Test GET /iam/user/roles - Get available roles"""
        response = api_get("/user/roles")
        response.raise_for_status()

        roles = response.json()
        assert isinstance(roles, list)

    def test_get_user_profile_metadata(self):
        """Test GET /iam/user/userprofilemetadata - Get user profile metadata"""
        response = api_get("/user/userprofilemetadata")
        response.raise_for_status()

    def test_get_invalid_user_id(self):
        """Test GET /iam/user/{id} - Invalid user ID returns error"""
        response = api_get("/user/00000000-0000-0000-0000-000000000000")

        # Should raise HTTPError for 4xx status
        with pytest.raises(requests.HTTPError):
            response.raise_for_status()

    def test_update_user_by_id(self):
        """Test PUT /iam/user/{id} - Update user by ID"""
        # Generate unique test data
        test_username = generate_test_username()
        test_email = f"{test_username}@example.com"
        created_user_id = None

        try:
            # Create a test user first
            user_data = {
                "attributes": {
                    "username": [test_username],
                    "email": [test_email],
                    "firstName": ["OriginalFirst"],
                    "lastName": ["OriginalLast"],
                    "position": ["Mitarbeitende"],
                },
                "institutions": ["Institution 1"],
                "role": "user",
                "network": "08",
                "enabled": True,
            }

            create_response = api_post("/user", data=user_data)
            create_response.raise_for_status()
            created_user = create_response.json()
            created_user_id = created_user.get("id")

            # Update the user's attributes
            update_data = {
                "id": created_user_id,
                "attributes": {
                    "username": [test_username],
                    "email": [test_email],
                    "firstName": ["UpdatedFirst"],
                    "lastName": ["UpdatedLast"],
                    "position": ["Leitung/Vertretung"],
                    "secondaryEmail":[],
                    "mobile":[],
                    "locale":[],
                    "title":[],
                    "tags":[],
                    "phone":[],
                    "fax":[],
                },
                "institutions": ["Institution 2"],
                "role": "user",
                "network": "19",
                "enabled": True,
                "hiddenInAddressbook": False,
            }

            update_response = api_put(f"/user/", data=update_data)
            print(update_response.request.body)
            try:
                create_response.raise_for_status()
            except Exception:
                print(create_response.text)
                raise

            # Verify the updates
            get_response = api_get(f"/user/{created_user_id}")
            get_response.raise_for_status()
            updated_user = get_response.json()

            assert updated_user["attributes"]["firstName"] == ["UpdatedFirst"]
            assert updated_user["attributes"]["lastName"] == ["UpdatedLast"]
            assert updated_user["attributes"]["position"] == ["Leitung/Vertretung"]
            assert updated_user["institutions"] == ["Institution 2"]
            assert updated_user["network"] == "19"

        finally:
            if created_user_id:
                delete_user_from_db(created_user_id)

    def test_update_nonexistent_user(self):
        """Test PUT /iam/user/{id} - Update non-existent user returns error"""
        fake_id = "00000000-0000-0000-0000-000000000000"

        update_data = {
            "id": fake_id,
            "attributes": {
                "username": ["testuser"],
                "email": ["test@example.com"],
                "firstName": ["Test"],
                "lastName": ["User"],
            },
            "institutions": ["Institution 1"],
            "role": "user",
            "network": "08",
            "enabled": True,
        }

        response = api_put(f"/user/{fake_id}", data=update_data)

        assert response.status_code == 404

    def test_user_lifecycle_create_search_delete(self):
        """
        Test complete user lifecycle:
        1. Create a new user
        2. Search for the user and verify attributes
        3. Delete the user
        4. Search for the user and verify it doesn't exist
        """
        # Generate unique test data
        test_username = generate_test_username()
        test_email = f"{test_username}@example.com"
        test_first_name = "TestFirst"
        test_last_name = "TestLast"

        created_user_id = None

        try:
            # Create a new user
            user_data = {
                "attributes": {
                    "username": [test_username],
                    "email": [test_email],
                    "firstName": [test_first_name],
                    "lastName": [test_last_name],
                    "position": ["Mitarbeitende"],
                },
                "institutions": ["Institution 1"],
                "role": "user",
                "network": "08",
                "enabled": True,
            }

            create_response = api_post("/user", data=user_data)
            create_response.raise_for_status()

            # Get the created user ID from the response
            created_user = create_response.json()
            created_user_id = created_user.get("id")
            assert created_user_id is not None

            # Search for the user and verify attributes
            search_response = api_get("/user", params={
                "search": create_search_query(username=test_username),
                "maxResults": 10
            })
            search_response.raise_for_status()

            search_data = search_response.json()
            assert search_data["size"] == 1

            # Verify all attributes
            assert search_data["list"][0]["id"] == created_user_id
            attrs = search_data["list"][0].get("attributes", {})
            assert attrs.get("username", [None])[0] == test_username
            assert attrs.get("email", [None])[0] == test_email
            assert attrs.get("firstName", [None])[0] == test_first_name
            assert attrs.get("lastName", [None])[0] == test_last_name
            assert 'Institution 1' in search_data["list"][0].get("institutions", [])
            assert search_data["list"][0]["enabled"] is True

            # Delete the user
            delete_user_data = user_data.copy()
            delete_user_data['enabled'] = False
            delete_user_data['id'] = created_user_id
            delete_response = api_put(f"/user/", data=delete_user_data)

            delete_response.raise_for_status()

            # Verify the user is not enabled
            get_response = api_get(f"/user/{created_user_id}", params={
                "search": f"search:{test_username}",
                "maxResults": 10
            })
            get_response.raise_for_status()
            get_json = get_response.json()
            assert get_json['enabled'] is False

            # Search for active users should not find the user anymore
            verify_search_response = api_get("/user", params={
                "search": f"search:{test_username}",
                "maxResults": 10,
                "search": 'enabled:"true"',
            })
            verify_search_response.raise_for_status()

            verify_data = verify_search_response.json()

            # Check that the deleted user is not in the results
            for user in verify_data["list"]:
                assert user["id"] != created_user_id
        finally:
            # Delete user from database
            if created_user_id:
                delete_user_from_db(created_user_id)


class TestUserAPIRolePermissions:
    """Test role-based permissions for user API"""

    def test_everyone_can_list_users(self):
        """Test that everyone can list enabled users"""
        auth = get_auth()

        for user in TEST_USERS.keys():
            try:
                auth.switch_user_context(user)
                response = api_get("/user", params={"search": 'enabled:"true"'})
                response.raise_for_status()

                data = response.json()
                assert data["size"] == 3

            finally:
                auth.clear_user_context()

    def test_update_own_profile(self):
        """Test that everyone can view and update their own profile"""
        auth = get_auth()

        for user in TEST_USERS.keys():
            try:
                auth.switch_user_context(user)

                # Get profile
                response = api_get("/user/profile")
                response.raise_for_status()

                profile = response.json()
                username = profile["attributes"]["username"][0]
                assert username == user

                # Try to update profile
                update_response = api_put("/user/profile", data=profile)
                update_response.raise_for_status()

            finally:
                auth.clear_user_context()

    def test_exampleuser_cannot_create_user(self):
        """Test that exampleuser cannot create users"""
        auth = get_auth()

        try:
            auth.switch_user_context("exampleuser")

            username = generate_test_username()
            user_data = {
                "attributes": {
                    "username": [username],
                    "email": [f"{username}@example.com"],
                    "firstName": ["Test"],
                    "lastName": ["User"]
                },
                "enabled": True
            }

            response = api_post("/user", data=user_data)

            assert response.status_code == 403

        finally:
            auth.clear_user_context()

    def test_baw_fields_update_and_visibility(self):
        """
        Test BAW (Operation Mode Change) fields update and visibility:
        1. Login as chefredakteur and update own profile with BAW fields
        2. Query own user data and verify BAW fields are present in profile
        3. But not in when querying user data
        4. Login as exampleuser and verify BAW fields of chefredakteur are not visible
        """
        baw_emails = ["baw.chef@example.com"]
        baw_phones = ["+49123456789"]
        baw_sms = ["+49987654321"]
        auth = get_auth()

        try:
            # Login as chefredakteur and get own profile data
            auth.switch_user_context("chefredakteur")
            profile_response = api_get("/user/profile")
            profile_response.raise_for_status()
            profile = profile_response.json()

            # Update profile with BAW fields
            profile["attributes"]["operationModeChangeMailAddresses"] = baw_emails
            profile["attributes"]["operationModeChangePhoneNumbers"] = baw_phones
            profile["attributes"]["operationModeChangeSmsPhoneNumbers"] = baw_sms

            update_response = api_put("/user/profile", data=profile)
            update_response.raise_for_status()

            # Query own profile and verify BAW fields are present
            profile_check_response = api_get("/user/profile")
            profile_check_response.raise_for_status()
            updated_profile = profile_check_response.json()

            assert updated_profile["attributes"]["operationModeChangeMailAddresses"] == baw_emails
            assert updated_profile["attributes"]["operationModeChangePhoneNumbers"] == baw_phones
            assert updated_profile["attributes"]["operationModeChangeSmsPhoneNumbers"] == baw_sms

            chefredakteur_id = updated_profile["id"]

            # Assert that BAW data is not in the non-profile search results
            search_response = api_get(f"/user/{chefredakteur_id}")
            search_data = search_response.json()

            assert "operationModeChangeMailAddresses" not in search_data["attributes"]
            assert "operationModeChangePhoneNumbers" not in search_data["attributes"]
            assert "operationModeChangeSmsPhoneNumbers" not in search_data["attributes"]

            # Login as exampleuser and verify BAW fields of chefredakteur are not visible
            auth.switch_user_context("exampleuser")
            chef_response = api_get(f"/user/{chefredakteur_id}")
            chef_response.raise_for_status()
            chef_data = chef_response.json()

            assert "operationModeChangeMailAddresses" not in chef_data["attributes"]
            assert "operationModeChangePhoneNumbers" not in chef_data["attributes"]
            assert "operationModeChangeSmsPhoneNumbers" not in chef_data["attributes"]

        finally:
            # Clean up: Login as chefredakteur and remove BAW fields
            auth.switch_user_context("chefredakteur")

            profile_response = api_get("/user/profile")
            profile_response.raise_for_status()

            profile = profile_response.json()
            profile["attributes"]["operationModeChangeMailAddresses"] = []
            profile["attributes"]["operationModeChangePhoneNumbers"] = []
            profile["attributes"]["operationModeChangeSmsPhoneNumbers"] = []
            api_put("/user/profile", data=profile)

            auth.clear_user_context()
