#!/usr/bin/python3
"""
Tests for '/iam/user'
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

    def test_user_tag_search_exact(self):
        """
        Test that user search by tag is exact:
        1. Create a user with tag "IBG"
        2. Create a user with tag "IBG nachrichtlich"
        3. Search for tag "IBG" - result must be only the first user
        """
        # Generate unique test data
        test_username_1 = generate_test_username()
        test_email_1 = f"{test_username_1}@example.com"
        test_first_name_1 = "IBG"
        test_last_name_1 = "User1"

        test_username_2 = generate_test_username()
        test_email_2 = f"{test_username_2}@example.com"
        test_first_name_2 = "IBG"
        test_last_name_2 = "User2"

        user_1_id = None
        user_2_id = None

        try:
            # Create user with tag "IBG"
            user_1_data = {
                "attributes": {
                    "username": [test_username_1],
                    "email": [test_email_1],
                    "firstName": [test_first_name_1],
                    "lastName": [test_last_name_1],
                    "position": ["Mitarbeitende"],
                    "tags": ["IBG"]
                },
                "institutions": ["Institution 1"],
                "role": "user",
                "network": "08",
                "enabled": True,
            }

            create_response_1 = api_post("/user", data=user_1_data)
            create_response_1.raise_for_status()
            user_1_id = create_response_1.json().get("id")

            # Create user with tag "IBG nachrichtlich"
            user_2_data = {
                "attributes": {
                    "username": [test_username_2],
                    "email": [test_email_2],
                    "firstName": [test_first_name_2],
                    "lastName": [test_last_name_2],
                    "position": ["Mitarbeitende"],
                    "tags": ["IBG nachrichtlich"]
                },
                "institutions": ["Institution 2"],
                "role": "user",
                "network": "08",
                "enabled": True,
            }

            create_response_2 = api_post("/user", data=user_2_data)
            create_response_2.raise_for_status()
            user_2_id = create_response_2.json().get("id")

            # Search for tag "IBG"
            search_response = api_get("/user", params={
                "search": create_search_query(tags="IBG"),
                "maxResults": 100
            })
            search_response.raise_for_status()
            search_data = search_response.json()["list"]

            # Verify only the first user with tag "IBG" is returned
            assert len(search_data) == 1
            assert search_data[0]["id"] == user_1_id
            assert search_data[0]["attributes"]["tags"] == ["IBG"]
            assert search_data[0]["attributes"]["username"] == [test_username_1]

        finally:
            if user_1_id:
                delete_user_from_db(user_1_id)
            if user_2_id:
                delete_user_from_db(user_2_id)

    def test_user_search_institution_multiple_choice(self):
        """
        Test multiple choice search in user search for institutions:
        1. Create a new user with institution "Institution 2"
        2. Search for users in institution "Institution 1" - result size should be 3 (all previous users)
        3. Search for users in institution "Institution 2" - result size should be 1 (the new user)
        4. Search for users in both institutions - result size should be 4 (all users)
        """
        # Generate unique test data
        test_username = generate_test_username()
        test_email = f"{test_username}@example.com"
        test_first_name = "Institution"
        test_last_name = "Two"

        user_id = None

        try:
            # Create a new user with institution "Institution 2"
            user_data = {
                "attributes": {
                    "username": [test_username],
                    "email": [test_email],
                    "firstName": [test_first_name],
                    "lastName": [test_last_name],
                    "position": ["Mitarbeitende"],
                    "tags": ["AK UN"]
                },
                "institutions": ["Institution 2"],
                "role": "user",
                "network": "08",
                "enabled": True,
            }

            create_response = api_post("/user", data=user_data)
            create_response.raise_for_status()
            user_id = create_response.json().get("id")

            # Search for users in institution "Institution 1"
            search_response_1 = api_get("/user", params={
                "search": create_search_query(institutions="Institution 1"),
                "maxResults": 100
            })
            search_response_1.raise_for_status()
            search_data_1 = search_response_1.json()

            assert search_data_1["size"] == 3 == len(search_data_1["list"])

            # Search for users in institution "Institution 2"
            search_response_2 = api_get("/user", params={
                "search": create_search_query(institutions="Institution 2"),
                "maxResults": 100
            })
            search_response_2.raise_for_status()
            search_data_2 = search_response_2.json()

            assert search_data_2["size"] == 1 == len(search_data_2["list"])

            # Search for users in both institutions
            search_response_3 = api_get("/user", params={
                "search": f'{create_search_query(institutions="Institution 1")} {create_search_query(institutions="Institution 2")}',
                "maxResults": 100
            })
            search_response_3.raise_for_status()
            search_data_3 = search_response_3.json()

            assert search_data_3["size"] == 4 == len(search_data_3["list"])
        finally:
            if user_id:
                delete_user_from_db(user_id)

    def test_user_search_position_multiple_choice(self):
        """
        Test multiple choice search for field position:
        1. Search for position "Leitung/Vertretung" - result size should be 2 (Redakteur & Chefredakteur)
        2. Search for position "Mitarbeitende" - result size should be 1 (User)
        3. Search for both positions - result size should be 3 (all)
        """
        # Search for position "Leitung/Vertretung"
        search_response_1 = api_get("/user", params={
            "search": create_search_query(position="Leitung/Vertretung"),
            "maxResults": 100
        })
        search_response_1.raise_for_status()
        search_data_1 = search_response_1.json()

        assert search_data_1["size"] == 2
        assert len(search_data_1["list"]) == 2

        # Search for position "Mitarbeitende"
        search_response_2 = api_get("/user", params={
            "search": create_search_query(position="Mitarbeitende"),
            "maxResults": 100
        })
        search_response_2.raise_for_status()
        search_data_2 = search_response_2.json()

        assert search_data_2["size"] == 1
        assert len(search_data_2["list"]) == 1

        # Search for both positions
        search_response_3 = api_get("/user", params={
            "search": f'{create_search_query(position="Mitarbeitende")} {create_search_query(position="Leitung/Vertretung")}',
            "maxResults": 100
        })
        search_response_3.raise_for_status()
        search_data_3 = search_response_3.json()

        assert search_data_3["size"] == 3
        assert len(search_data_3["list"]) == 3

    def test_user_search_tags_multiple_choice(self):
        """
        Test multiple choice search for field tags:
        1. Create a new user with tag "Kleine Lagen"
        2. Create another new user with tags "AK UN" and "Kleine Lagen"
        3. Search for users with tag "AK UN" - result size should be 4 (all previous users + second new user)
        4. Search for users with tag "Kleine Lagen" - result size should be 2 (the 2 new users)
        5. Search for both tags - result size should be 5 (all)
        """
        # Generate unique test data
        test_username_1 = generate_test_username()
        test_email_1 = f"{test_username_1}@example.com"
        test_first_name_1 = "Kleine"
        test_last_name_1 = "Lagen"

        test_username_2 = generate_test_username()
        test_email_2 = f"{test_username_2}@example.com"
        test_first_name_2 = "Both"
        test_last_name_2 = "Tags"

        user_1_id = None
        user_2_id = None

        try:
            # Create a new user with tag "Kleine Lagen"
            user_1_data = {
                "attributes": {
                    "username": [test_username_1],
                    "email": [test_email_1],
                    "firstName": [test_first_name_1],
                    "lastName": [test_last_name_1],
                    "position": ["Mitarbeitende"],
                    "tags": ["Kleine Lagen"]
                },
                "institutions": ["Institution 1"],
                "role": "user",
                "network": "08",
                "enabled": True,
            }

            create_response_1 = api_post("/user", data=user_1_data)
            create_response_1.raise_for_status()
            user_1_id = create_response_1.json().get("id")

            # Create another new user with tags "AK UN" and "Kleine Lagen"
            user_2_data = {
                "attributes": {
                    "username": [test_username_2],
                    "email": [test_email_2],
                    "firstName": [test_first_name_2],
                    "lastName": [test_last_name_2],
                    "position": ["Mitarbeitende"],
                    "tags": ["AK UN", "Kleine Lagen"]
                },
                "institutions": ["Institution 1"],
                "role": "user",
                "network": "08",
                "enabled": True,
            }

            create_response_2 = api_post("/user", data=user_2_data)
            create_response_2.raise_for_status()
            user_2_id = create_response_2.json().get("id")

            # Search for users with tag "AK UN"
            search_response_1 = api_get("/user", params={
                "search": create_search_query(tags="AK UN"),
                "maxResults": 100
            })
            search_response_1.raise_for_status()
            search_data_1 = search_response_1.json()

            assert search_data_1["size"] == 4 == len(search_data_1["list"])

            # Search for users with tag "Kleine Lagen"
            search_response_2 = api_get("/user", params={
                "search": create_search_query(tags="Kleine Lagen"),
                "maxResults": 100
            })
            search_response_2.raise_for_status()
            search_data_2 = search_response_2.json()

            assert search_data_2["size"] == 2 == len(search_data_2["list"])

            # Search for both tags
            search_response_3 = api_get("/user", params={
                "search": f'{create_search_query(tags="AK UN")} {create_search_query(tags="Kleine Lagen")}',
                "maxResults": 100
            })
            search_response_3.raise_for_status()
            search_data_3 = search_response_3.json()

            assert search_data_3["size"] == 5 == len(search_data_3["list"])

            # Verify tag distribution
            ak_un_count = sum(1 for user in search_data_3["list"] if "AK UN" in user["attributes"]["tags"])
            kleine_lagen_count = sum(1 for user in search_data_3["list"] if "Kleine Lagen" in user["attributes"]["tags"])
            both_tags_count = sum(1 for user in search_data_3["list"]
                                  if "AK UN" in user["attributes"]["tags"]
                                  and "Kleine Lagen" in user["attributes"]["tags"])

            assert ak_un_count == 4
            assert kleine_lagen_count == 2
            assert both_tags_count == 1

        finally:
            if user_1_id:
                delete_user_from_db(user_1_id)
            if user_2_id:
                delete_user_from_db(user_2_id)


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
                assert data["size"] == 3, f"{user} can see {data['size']} users, not 3: {[u['attributes']['username'][0] for u in data['list']]}"

            finally:
                auth.clear_user_context()

    def test_update_own_profile(self):
        """Test that everyone can view and update their own profile, test by updating BAW fields"""
        auth = get_auth()

        for user in TEST_USERS.keys():
            try:
                auth.switch_user_context(user)

                # Get profile
                profile_response = api_get("/user/profile")
                profile_response.raise_for_status()

                profile = profile_response.json()
                username = profile["attributes"]["username"][0]
                assert username == user

                # Update profile with BAW fields
                baw_email = f"baw.{user}@example.com"
                user_hash = abs(hash(user)) % 10000  # 4 digits
                baw_phone = f"+491234{user_hash}"
                baw_sms = f"+499876{user_hash}"
                profile["attributes"]["operationModeChangeMailAddresses"] = [baw_email]
                profile["attributes"]["operationModeChangePhoneNumbers"] = [baw_phone]
                profile["attributes"]["operationModeChangeSmsPhoneNumbers"] = [baw_sms]

                # Update profile
                update_response = api_put("/user/profile", data=profile)
                update_response.raise_for_status()

                # Verify fields are saved
                verify_response = api_get("/user/profile")
                verify_response.raise_for_status()
                updated_profile = verify_response.json()

                assert updated_profile["attributes"]["operationModeChangeMailAddresses"] == [baw_email]
                assert updated_profile["attributes"]["operationModeChangePhoneNumbers"] == [baw_phone]
                assert updated_profile["attributes"]["operationModeChangeSmsPhoneNumbers"] == [baw_sms]

            finally:
                # Clean up: Remove fields
                profile_response = api_get("/user/profile")
                profile_response.raise_for_status()
                profile = profile_response.json()
                profile["attributes"]["operationModeChangeMailAddresses"] = []
                profile["attributes"]["operationModeChangePhoneNumbers"] = []
                profile["attributes"]["operationModeChangeSmsPhoneNumbers"] = []
                put_request = api_put("/user/profile", data=profile)
                put_request.raise_for_status()

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

    def test_baw_fields_visibility(self):
        """
        Test BAW (Operation Mode Change) fields visibility:
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

    def test_baw_fields_updateability(self):
        """
        Test that chefredakteur cannot set BAW fields for another user:
        1. Login as chefredakteur
        2. Set BAW fields for exampleuser
        3. Verify the data is not saved
        4. Login as exampleuser
        5. Verify the data is in the profile information
        """
        baw_emails = ["baw.user@example.com"]
        baw_phones = ["+49111222333"]
        baw_sms = ["+49444555666"]
        auth = get_auth()

        # Login as chefredakteur and set BAW fields for exampleuser
        auth.switch_user_context("chefredakteur")

        # First, get current data
        search_response = api_get("/user", params={
            "search": create_search_query(username="exampleuser"),
            "maxResults": 1
        })
        search_response.raise_for_status()
        search_data = search_response.json()
        assert search_data["size"] == 1

        exampleuser_data = search_data["list"][0]
        exampleuser_id = exampleuser_data["id"]

        # Try to set BAW fields
        exampleuser_data["attributes"]["operationModeChangeMailAddresses"] = baw_emails
        exampleuser_data["attributes"]["operationModeChangePhoneNumbers"] = baw_phones
        exampleuser_data["attributes"]["operationModeChangeSmsPhoneNumbers"] = baw_sms
        update_response = api_put("/user/", data=exampleuser_data)
        assert update_response.status_code == 400  # Bad Request

        # Verify the data
        verify_response = api_get(f"/user/{exampleuser_id}")
        verify_response.raise_for_status()
        verified_data = verify_response.json()

        assert "operationModeChangeMailAddresses" not in verified_data["attributes"]
        assert "operationModeChangePhoneNumbers" not in verified_data["attributes"]
        assert "operationModeChangeSmsPhoneNumbers" not in verified_data["attributes"]

        # Login as exampleuser and verify the data is also not in the profile
        auth.switch_user_context("exampleuser")
        profile_response = api_get("/user/profile")
        profile_response.raise_for_status()
        profile_data = profile_response.json()

        assert profile_data["attributes"]["operationModeChangeMailAddresses"] == []
        assert profile_data["attributes"]["operationModeChangePhoneNumbers"] == []
        assert profile_data["attributes"]["operationModeChangeSmsPhoneNumbers"] == []

    def test_hidden_in_addressbook_visibility(self):
        """
        Test hiddenInAddressbook visibility rules:
        1. Chief editor creates a user with hiddenInAddressbook=true
        2. Verify exampleuser cannot query it directly (403)
        3. Verify exampleuser cannot see it in the user list
        4. Verify redakteur cannot query it directly (403)
        5. Verify redakteur cannot see it in the user list
        6. Verify chefredakteur CAN query it directly (200)
        7. Verify chefredakteur CAN see it in the user list
        """
        # Generate unique test data
        test_username = generate_test_username()
        test_email = f"{test_username}@example.com"
        created_user_id = None

        try:
            auth = get_auth()
            auth.switch_user_context("chefredakteur")

            # 1: Create user with hiddenInAddressbook=true
            user_data = {
                "attributes": {
                    "username": [test_username],
                    "email": [test_email],
                    "firstName": ["Hidden"],
                    "lastName": ["User"],
                    "position": ["Mitarbeitende"],
                },
                "institutions": ["Institution 1"],
                "role": "user",
                "network": "08",
                "enabled": True,
                "hiddenInAddressbook": True,
                "retired": False,
            }

            create_response = api_post("/user", data=user_data)
            create_response.raise_for_status()
            created_user = create_response.json()
            created_user_id = created_user["id"]

            assert created_user["hiddenInAddressbook"] is True
            assert created_user["enabled"] is True

            for username in ("exampleuser", "redakteur"):
                auth.switch_user_context(username)

                # 2+4: Try to query the hidden user
                get_response = api_get(f"/user/{created_user_id}")
                assert get_response.status_code == 403

                # 3+5: Verify user not in list
                list_response = api_get("/user", params={
                    "search": create_search_query(username=test_username),
                    "maxResults": 100
                })
                list_response.raise_for_status()
                users = list_response.json()["list"]
                assert not any(u["id"] == created_user_id for u in users)

            # 6: chefredakteur can query the hidden user
            auth.switch_user_context("chefredakteur")

            get_response = api_get(f"/user/{created_user_id}")
            get_response.raise_for_status()
            retrieved_user = get_response.json()
            assert retrieved_user["id"] == created_user_id
            assert retrieved_user["hiddenInAddressbook"] is True

            # 7: chefredakteur can see hidden user in list
            list_response = api_get("/user", params={
                "search": create_search_query(username=test_username),
                "maxResults": 100
            })
            list_response.raise_for_status()
            users = list_response.json()["list"]
            assert any(u["id"] == created_user_id for u in users)

        finally:
            if created_user_id:
                delete_user_from_db(created_user_id)

    def test_create_user_with_invalid_position(self):
        """
        Test that creating a user with an invalid position value fails with validation error.

        Valid position values are:
        - "Leitung/Vertretung"
        - "Fachliche Ansprechperson"
        - "Mitarbeitende"
        """
        auth = get_auth()
        auth.switch_user_context("chefredakteur")

        test_user = {
            "attributes": {
                "username": [generate_test_username()],
                "firstName": ["Test"],
                "lastName": ["User"],
                "email": ["invalid.position@example.com"],
                "position": ["Invalid Position Value"]  # Invalid value
            },
            "institutions": ["Test Institution"],
            "network": "BfS",
            "role": "user",
            "enabled": True,
            "hiddenInAddressbook": False,
            "retired": False
        }

        # Attempt to create user with invalid position
        response = api_post("/user", test_user)

        # Should receive a validation error (400 Bad Request)
        assert response.status_code == 400

        # Verify error response contains validation information
        # error_data: [{'attribute': 'position', 'message': 'error-invalid-value', 'messageParameters': ['position'], 'statusCode': 'BAD_REQUEST'}]
        error_data = response.json()
        assert any(item.get("attribute") == "position" for item in error_data)


    def test_retired_field_is_privileged(self):
        """
        Test that only chefredakteur can set retired=true.
        Editors cannot set retired status.
        Regular users can't even create a new user -> irrelevant
        """
        test_username = generate_test_username()
        test_email = f"{test_username}@example.com"
        created_user_id = None

        try:
            # Login as redakteur and try to create user with retired=true
            auth = get_auth()
            auth.switch_user_context("redakteur")

            user_data = {
                "attributes": {
                    "username": [test_username],
                    "email": [test_email],
                    "firstName": ["Test"],
                    "lastName": ["Retired"],
                    "position": ["Mitarbeitende"],
                },
                "institutions": ["Institution 1"],
                "role": "user",
                "network": "08",
                "enabled": False,
                "retired": True,
            }

            # Should fail with 403 Forbidden
            create_response = api_post("/user", data=user_data)
            assert create_response.status_code == 403

            # Countercheck: Create user as chefredakteur should succeed
            auth.switch_user_context("chefredakteur")

            create_response = api_post("/user", data=user_data)
            create_response.raise_for_status()
            created_user = create_response.json()
            created_user_id = created_user.get("id")

            # Verify the user
            assert created_user["retired"] is True

        finally:
            if created_user_id:
                delete_user_from_db(created_user_id)

    def test_retired_users_automatically_disabled(self):
        """
        Test that when retired=true, enabled is automatically set to false.
        Test both create and update operations
        """
        test_username = generate_test_username()
        test_email = f"{test_username}@example.com"
        created_user_id = None

        try:
            auth = get_auth()
            auth.switch_user_context("chefredakteur")

            # Create user with retired=true and enabled=true
            user_data = {
                "attributes": {
                    "username": [test_username],
                    "email": [test_email],
                    "firstName": ["Test"],
                    "lastName": ["Retired"],
                    "position": ["Mitarbeitende"],
                },
                "institutions": ["Institution 1"],
                "role": "user",
                "network": "08",
                "enabled": True,  # Try to enable
                "retired": True,  # But also retire
            }

            create_response = api_post("/user", data=user_data)
            create_response.raise_for_status()
            created_user = create_response.json()
            created_user_id = created_user.get("id")

            # Enabled should be set to false
            assert created_user["retired"] is True
            assert created_user["enabled"] is False

            # Update user to retired=true
            update_data = created_user.copy()
            update_data["enabled"] = True
            update_data["retired"] = False

            update_response = api_put("/user", data=update_data)
            update_response.raise_for_status()
            updated_user = update_response.json()

            # Verify user is now enabled and not retired
            assert updated_user["retired"] is False
            assert updated_user["enabled"] is True

            # Now retire the user again
            update_data["retired"] = True
            update_data["enabled"] = True  # Try to keep enabled

            update_response = api_put("/user", data=update_data)
            update_response.raise_for_status()
            updated_user = update_response.json()

            # Verify enabled was set to false
            assert updated_user["retired"] is True
            assert updated_user["enabled"] is False

        finally:
            if created_user_id:
                delete_user_from_db(created_user_id)

    def test_retired_user_visibility(self):
        """
        Test retired user visibility:
        - Chief editors can see retired users
        - Regular editors/users cannot see retired users
        """
        test_username = generate_test_username()
        test_email = f"{test_username}@example.com"
        created_user_id = None

        try:
            auth = get_auth()
            auth.switch_user_context("chefredakteur")

            # Create retired user
            user_data = {
                "attributes": {
                    "username": [test_username],
                    "email": [test_email],
                    "firstName": ["Retired"],
                    "lastName": ["User"],
                    "position": ["Mitarbeitende"],
                },
                "institutions": ["Institution 1"],
                "role": "user",
                "network": "08",
                "enabled": False,
                "retired": True,
            }

            create_response = api_post("/user", data=user_data)
            create_response.raise_for_status()
            created_user = create_response.json()
            created_user_id = created_user.get("id")

            # Chief editor should be able to see the retired user
            get_response = api_get(f"/user/{created_user_id}")
            get_response.raise_for_status()
            retrieved_user = get_response.json()
            assert retrieved_user["retired"] is True

            # Chief editor should see retired user in list
            list_response = api_get("/user", params={
                "search": create_search_query(username=test_username),
                "maxResults": 100
            })
            list_response.raise_for_status()
            users = list_response.json()["list"]

            # Chief editor should see retired user in list
            retired_user_in_list = any(u["id"] == created_user_id for u in users)
            assert retired_user_in_list

            # Search for retired users: test user should be in data
            retired_search_response = api_get("/user", params={
                "search": f'{create_search_query(username=test_username, retired="true")}',
                "maxResults": 100
            })
            retired_search_response.raise_for_status()
            retired_users = retired_search_response.json()["list"]
            assert any(u["id"] == created_user_id for u in retired_users), retired_users

            # Search for non-retired users: test user should NOT be in data
            non_retired_search_response = api_get("/user", params={
                "search": f'{create_search_query(username=test_username, retired="false")}',
                "maxResults": 100
            })
            non_retired_search_response.raise_for_status()
            non_retired_users = non_retired_search_response.json()["list"]
            assert not any(u["id"] == created_user_id for u in non_retired_users)

            # Switch to regular user
            auth.switch_user_context("exampleuser")

            # Should get 403 Forbidden
            get_response = api_get(f"/user/{created_user_id}")
            assert get_response.status_code == 403

            # Retired user should not appear in user list
            list_response = api_get("/user", params={
                "search": create_search_query(username=test_username),
                "maxResults": 100
            })
            list_response.raise_for_status()
            users = list_response.json()["list"]
            assert not any(u["id"] == created_user_id for u in users)

            # Switch to editor
            auth.switch_user_context("redakteur")

            # Should get 403 Forbidden
            get_response = api_get(f"/user/{created_user_id}")
            assert get_response.status_code == 403

            # Retired user should not appear in user list
            list_response = api_get("/user", params={
                "search": create_search_query(username=test_username),
                "maxResults": 100
            })
            list_response.raise_for_status()
            users = list_response.json()["list"]
            assert not any(u["id"] == created_user_id for u in users)

        finally:
            if created_user_id:
                delete_user_from_db(created_user_id)

    def test_retired_user_uid_preservation(self):
        """
        Test that UIDs of preserved users cannot be reused
        """
        test_username = generate_test_username()
        test_email = f"{test_username}@example.com"
        created_user_id = None

        try:
            auth = get_auth()
            auth.switch_user_context("chefredakteur")

            # Create and retire a user
            user_data = {
                "attributes": {
                    "username": [test_username],
                    "email": [test_email],
                    "firstName": ["Test"],
                    "lastName": ["Retired"],
                    "position": ["Mitarbeitende"],
                },
                "institutions": ["Institution 1"],
                "role": "user",
                "network": "08",
                "enabled": True,
                "retired": True,
            }

            create_response = api_post("/user", data=user_data)
            create_response.raise_for_status()
            created_user = create_response.json()
            created_user_id = created_user.get("id")

            # Verify user exists
            get_response = api_get(f"/user/{created_user_id}")
            get_response.raise_for_status()
            retired_user = get_response.json()
            assert retired_user["id"] == created_user_id
            assert retired_user["retired"] is True

            # Try to create new user with same username - should fail
            new_user_data = {
                "attributes": {
                    "username": [test_username],
                    "email": [f"new_{test_email}"],
                    "firstName": ["New"],
                    "lastName": ["User"],
                    "position": ["Mitarbeitende"],
                },
                "institutions": ["Institution 1"],
                "role": "user",
                "network": "08",
                "enabled": True,
                "retired": False,
            }

            # This should fail because username already exists
            create_response = api_post("/user", data=new_user_data)
            # [{"statusCode":"CONFLICT","message":"usernameExistsMessage","attribute":"username","messageParameters":["username"]}]
            assert create_response.status_code == 400
            assert create_response.json()[0]['statusCode'] == 'CONFLICT'

        finally:
            if created_user_id:
                delete_user_from_db(created_user_id)

    def test_retired_user_cannot_login(self):
        """
        Test complete retired user workflow:
        1. Chief editor creates a user
        2. Login as that user (should succeed)
        3. Chief editor retires the user
        4. Try to login as that user again (should fail)
        """
        test_username = generate_test_username()
        test_password = "password"
        test_email = f"{test_username}@example.com"
        created_user_id = None

        try:
            auth = get_auth()
            auth.switch_user_context("chefredakteur")

            # Create active, non-retired user
            user_data = {
                "attributes": {
                    "username": [test_username],
                    "email": [test_email],
                    "firstName": ["Test"],
                    "lastName": ["LoginUser"],
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

            # Set password for the new user via Keycloak Admin API
            admin_headers = auth.get_admin_headers()
            password_url = f"{auth.keycloak_url}/admin/realms/{auth.realm}/users/{created_user_id}/reset-password"
            password_data = {
                "type": "password",
                "value": test_password,
                "temporary": False
            }

            password_response = requests.put(password_url, json=password_data,
                                             headers=admin_headers)
            password_response.raise_for_status()

            # Login as the new user (should succeed)
            try:
                user_token = auth.get_access_token(test_username, test_password)
                assert user_token is not None
                # Clear token cache for subsequent tests
                if test_username in auth._token_cache:
                    del auth._token_cache[test_username]
            except Exception as e:
                pytest.fail(f"Initial login failed for active user: {str(e)}")

            # Retire the user
            auth.switch_user_context("chefredakteur")
            update_data = created_user.copy()
            update_data["retired"] = True
            update_response = api_put("/user", data=update_data)
            update_response.raise_for_status()
            updated_user = update_response.json()

            # Verify user is retired and disabled
            assert updated_user["retired"] is True
            assert updated_user["enabled"] is False

            # Try to login as retired user (should fail)
            login_failed = False
            try:
                # Clear cache to force fresh login attempt
                if test_username in auth._token_cache:
                    del auth._token_cache[test_username]

                retired_user_token = auth.get_access_token(test_username, test_password)
                # If we got here, login succeeded when it shouldn't have
                login_failed = False
            except Exception as e:
                # Login should fail for retired/disabled user
                login_failed = True
                # Verify it's an authentication error
                assert "Failed to get access token" in str(e) or "401" in str(e)

            # Verify that login actually failed
            assert login_failed, "Retired user should not be able to login, but authentication succeeded"

        finally:
            if created_user_id:
                delete_user_from_db(created_user_id)

    def test_user_network_visibility_and_disabled_state(self):
        """
        Test user visibility based on network and disabled state:
        1. Create a user with network "11"
        2. Verify redakteur and exampleuser can query it
        3. Disable the user
        4. Verify redakteur (in a different network) and exampleuser cannot query it anymore
        5. Change network to "07"
        6. Verify redakteur can query
        7. Verify exampleuser cannot query it
        """
        # Example user data
        test_username = generate_test_username()
        created_user_id = None
        auth = get_auth()
        user_data = {
            "attributes": {
                "username": [test_username],
                "email": [f"{test_username}@example.com"],
                "firstName": ["Network"],
                "lastName": ["Test"],
                "position": ["Mitarbeitende"],
            },
            "institutions": ["Institution 1"],
            "role": "user",
            "network": "11",
            "enabled": True,
        }

        auth.switch_user_context("chefredakteur")

        try:
            # 1: Create user with network "11"
            create_response = api_post("/user", data=user_data)
            create_response.raise_for_status()
            created_user = create_response.json()
            created_user_id = created_user["id"]

            assert created_user["network"] == "11"
            assert created_user["enabled"] is True

            # 2: Verify redakteur and exampleuser can query it
            for username in ("redakteur", "exampleuser"):
                auth.switch_user_context(username)

                # Query by ID
                get_response = api_get(f"/user/{created_user_id}")
                get_response.raise_for_status()
                retrieved_user = get_response.json()
                assert retrieved_user["id"] == created_user_id

                # Query via search
                list_response = api_get("/user", params={
                    "search": create_search_query(username=test_username),
                    "maxResults": 100
                })
                list_response.raise_for_status()
                users = list_response.json()["list"]
                assert any(u["id"] == created_user_id for u in users)

            # 3: Disable the user
            auth.switch_user_context("chefredakteur")
            user_data["id"] = created_user_id
            user_data["enabled"] = False

            update_response = api_put("/user", data=user_data)
            update_response.raise_for_status()
            updated_user = update_response.json()
            assert updated_user["enabled"] is False

            # 4: Verify redakteur and exampleuser cannot query it anymore
            for username in ("redakteur", "exampleuser"):
                auth.switch_user_context(username)

                # Query by ID should still work but show disabled
                get_response = api_get(f"/user/{created_user_id}")
                assert get_response.status_code == 403, username

                # Disabled user should not appear in the search
                list_response = api_get("/user", params={
                    "search": create_search_query(username=test_username),
                    "maxResults": 100
                })
                list_response.raise_for_status()
                users = list_response.json()["list"]
                assert not any(u["id"] == created_user_id for u in users), username

            # 5: Change network to "07"
            auth.switch_user_context("chefredakteur")
            user_data["network"] = "07"

            update_response = api_put("/user", data=user_data)
            update_response.raise_for_status()
            updated_user = update_response.json()
            assert updated_user["network"] == "07"
            assert updated_user["enabled"] is False

            # 6: Verify redakteur can query it
            auth.switch_user_context("redakteur")
            get_response = api_get(f"/user/{created_user_id}")
            get_response.raise_for_status()
            retrieved_user = get_response.json()
            assert retrieved_user["id"] == created_user_id
            assert retrieved_user["network"] == "07"

            list_response = api_get("/user", params={
                "search": create_search_query(username=test_username),
                "maxResults": 100
            })
            list_response.raise_for_status()
            users = list_response.json()["list"]
            assert any(u["id"] == created_user_id for u in users)

            # 7. Verify exampleuser cannot query it
            auth.switch_user_context("exampleuser")
            get_response = api_get(f"/user/{created_user_id}")
            assert get_response.status_code == 403

            list_response = api_get("/user", params={
                "search": create_search_query(username=test_username),
                "maxResults": 100
            })
            list_response.raise_for_status()
            users = list_response.json()["list"]
            assert not any(u["id"] == created_user_id for u in users)

        finally:
            if created_user_id:
                delete_user_from_db(created_user_id)

    def test_editor_can_modify_user_tags_same_network(self):
        """
        Test that redakteur (editor) can modify tags for users in same network:
        1. Create a temporary user in redakteur's network
        2. Login as redakteur and update the user's tags
        3. Verify tags are updated
        4. Move user to a different network
        5. Try to modify tags as redakteur (should fail)
        """
        test_username = generate_test_username()
        created_user_id = None
        auth = get_auth()
        user_data = {
            "attributes": {
                "username": [test_username],
                "email": [f"{test_username}@example.com"],
                "firstName": ["Tag"],
                "lastName": ["Test"],
                "position": ["Mitarbeitende"],
                "tags": ["IBG"],
            },
            "institutions": ["Institution 1"],
            "role": "user",
            "network": "07",  # Same network as redakteur
            "enabled": True,
        }

        auth.switch_user_context("chefredakteur")

        try:
            # 1: Create user in redakteur's network
            create_response = api_post("/user", data=user_data)
            create_response.raise_for_status()
            created_user = create_response.json()
            created_user_id = created_user["id"]

            assert created_user["network"] == user_data["network"]
            assert created_user["attributes"]["tags"] == user_data["attributes"]["tags"]

            # 2: Login as redakteur and update the user's tags
            auth.switch_user_context("redakteur")
            user_data["id"] = created_user_id
            user_data["attributes"]["tags"] = ["AK UN", "Kleine Lagen"]
            update_response = api_put("/user", data=user_data)
            update_response.raise_for_status()

            # 3: Verify tags were updated
            updated_user = update_response.json()
            assert updated_user["attributes"]["tags"] == ["AK UN", "Kleine Lagen"]

            # 4: Move user to a different network
            auth.switch_user_context("chefredakteur")
            user_data["network"] = "20"
            update_response = api_put("/user", data=user_data)
            update_response.raise_for_status()
            updated_user = update_response.json()
            assert updated_user["network"] == "20"

            # 5: Try to modify tags as redakteur (should fail with 403)
            auth.switch_user_context("redakteur")
            user_data["attributes"]["tags"] = ["IBG nachrichtlich"]

            update_response = api_put("/user", data=user_data)
            assert update_response.status_code == 403

        finally:
            if created_user_id:
                delete_user_from_db(created_user_id)
