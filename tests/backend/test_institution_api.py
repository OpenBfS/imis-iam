#!/usr/bin/python3
"""
Tests for '/iam/institution'
"""

import pytest
from lib import (
    api_get, api_post, api_put,
    get_auth,
    generate_test_username,
    delete_institution_from_db,
    create_search_query
)



class TestInstitutionAPI:
    """Test suite for /iam/institution endpoints"""

    def test_list_institutions(self):
        """Test GET /iam/institution - List institutions"""
        response = api_get("/institution", params={"maxResults": 10})
        response.raise_for_status()

        data = response.json()
        assert data['size'] >= 2
        assert len(data['list']) >= 2
        assert data['list'][0]['measFacilName'] == 'inst_1'
        assert data['list'][1]['measFacilName'] == 'inst_2'

    def test_list_institutions_with_pagination(self):
        """Test GET /iam/institution - Pagination"""
        response = api_get("/institution", params={
            "firstResult": 0,
            "maxResults": 1
        })
        response.raise_for_status()
        data = response.json()
        assert data['size'] >= 2  # 'size' is the total number of search results
        assert len(data["list"]) == 1
        assert data['list'][0]['measFacilName'] == 'inst_1'

        # Query again for Institution 2
        response = api_get("/institution", params={
            "firstResult": 1,
            "maxResults": 1
        })
        response.raise_for_status()
        data = response.json()
        assert data['size'] >= 2
        assert len(data["list"]) == 1
        assert data['list'][0]['measFacilName'] == 'inst_2'

    def test_get_institution_by_id(self):
        """Test GET /iam/institution/{id} - Get institution by ID"""
        # First get a list to find an institution ID
        list_response = api_get("/institution", params={"maxResults": 1})
        list_response.raise_for_status()

        institutions = list_response.json()["list"]
        if not institutions:
            pytest.skip("No institutions available for testing")

        institution_id = institutions[0]["id"]

        # Now get the specific institution
        response = api_get(f"/institution/{institution_id}")
        response.raise_for_status()

        institution = response.json()
        assert institution["id"] == institution_id
        assert "name" in institution
        assert "network" in institution

    def test_get_institution_tags(self):
        """Test GET /iam/institution/tag - Get all tags"""
        response = api_get("/institution/tag")
        response.raise_for_status()

        data = response.json()
        assert len(data) == 11, data  # See example data in docker/keycloak/add_example_data.sql
        for tag in data:
            assert list(tag.keys()) == ['name']

    def test_institution_lifecycle_create_update_get(self):
        """
        Test complete institution lifecycle:
        1. Create a new institution
        2. Get the institution and verify attributes
        3. Update the institution
        4. Verify updated attributes
        Note: Institutions are not deleted to maintain referential integrity
        """
        # Generate test data
        suffix = generate_test_username(prefix='')[1:]
        test_name = f"Test Institution {suffix}"
        test_meas_facil_name = f"test_{suffix}"
        test_meas_facil_id = f"99{suffix[:5]}"

        # Create a new institution
        institution_data = {
            "name": test_name,
            "measFacilName": test_meas_facil_name,
            "measFacilId": test_meas_facil_id,
            "serviceBuildingStreet": "Test Street 123",
            "serviceBuildingPostalCode": "12345",
            "serviceBuildingLocation": "Test City",
            "serviceBuildingState": "Test State",
            "network": "08",
            "active": True,
            "tags": ["Bundesministerium"]
        }

        create_response = api_post("/institution", data=institution_data)
        try:
            create_response.raise_for_status()
        except Exception:
            print(create_response.text)
            raise

        # Get the created institution ID
        created_institution = create_response.json()
        created_institution_id = created_institution.get("id")

        # Get the institution and verify the attributes
        get_response = api_get(f"/institution/{created_institution_id}")
        get_response.raise_for_status()
        retrieved_institution = get_response.json()
        assert retrieved_institution["id"] == created_institution_id
        assert {key: value for key, value in retrieved_institution.items()
                 if key != 'id' and value != []} == institution_data

        # Update the institution
        updated_name = f"{test_name} - Updated"
        update_data = {
            "id": created_institution_id,
            "name": updated_name,
            "measFacilName": test_meas_facil_name,
            "measFacilId": test_meas_facil_id,
            "serviceBuildingStreet": "Updated Street 456",
            "serviceBuildingPostalCode": "54321",
            "serviceBuildingLocation": "Updated City",
            "serviceBuildingState": "Updated State",
            "network": "19",
            "active": False,
            "tags": ["Landesmessstelle"]
        }

        update_response = api_put("/institution", data=update_data)
        update_response.raise_for_status()

        # Verify the updates
        verify_response = api_get(f"/institution/{created_institution_id}")
        verify_response.raise_for_status()

        updated_institution = verify_response.json()
        assert {key: value for key, value in updated_institution.items()
                if value != []} == update_data

        # Cleanup: Delete institution from database
        delete_institution_from_db(created_institution_id)

    def test_create_institution_with_missing_fields(self):
        """Test POST /iam/institution - Creating institution with missing required fields"""
        incomplete_data = {
            "name": "Incomplete Institution"
            # Missing other required fields
        }

        response = api_post("/institution", data=incomplete_data)
        assert response.status_code == 400

    def test_update_nonexistent_institution(self):
        """Test PUT /iam/institution - Updating non-existent institution"""
        fake_id = 999999

        update_data = {
            "id": fake_id,
            "name": "This won't work",
            "serviceBuildingStreet": "Test",
            "serviceBuildingPostalCode": "12345",
            "serviceBuildingLocation": "Test",
            "network": "08",
            "active": True
        }

        response = api_put("/institution", data=update_data)

        assert response.status_code == 400

    def test_get_nonexistent_institution(self):
        """Test GET /iam/institution/{id} - Get non-existent institution"""
        response = api_get("/institution/999999")

        assert response.status_code == 404

    def test_institution_category_search_exact(self):
        """
        Test that institution search by category/tag is exact:
        1. Creates an institution with category "Landesmessstelle"
        2. Creates an institution with category "Landesmessstelle REI"
        3. Search for tag "Landesmessstelle" - result must be only the first institution
        """
        # Test data
        suffix = generate_test_username(prefix='')[1:]
        test_name_1 = f"Test Landesmessstelle {suffix}"
        test_meas_facil_name_1 = f"test_lms_{suffix}"
        test_meas_facil_id_1 = f"91{suffix[:5]}"

        test_name_2 = f"Test Landesmessstelle REI {suffix}"
        test_meas_facil_name_2 = f"test_lms_rei_{suffix}"
        test_meas_facil_id_2 = f"92{suffix[:5]}"

        institution_1_id = None
        institution_2_id = None

        try:
            # Create institution 1 with category "Landesmessstelle"
            institution_1_data = {
                "name": test_name_1,
                "measFacilName": test_meas_facil_name_1,
                "measFacilId": test_meas_facil_id_1,
                "serviceBuildingStreet": "Test Street 1",
                "serviceBuildingPostalCode": "11111",
                "serviceBuildingLocation": "Test City 1",
                "serviceBuildingState": "Test State 1",
                "network": "08",
                "active": True,
                "tags": ["Landesmessstelle"]
            }

            create_response_1 = api_post("/institution", data=institution_1_data)
            create_response_1.raise_for_status()
            institution_1_id = create_response_1.json().get("id")

            # Create institution 2 with category "Landesmessstelle REI"
            institution_2_data = {
                "name": test_name_2,
                "measFacilName": test_meas_facil_name_2,
                "measFacilId": test_meas_facil_id_2,
                "serviceBuildingStreet": "Test Street 2",
                "serviceBuildingPostalCode": "22222",
                "serviceBuildingLocation": "Test City 2",
                "serviceBuildingState": "Test State 2",
                "network": "08",
                "active": True,
                "tags": ["Landesmessstelle REI"]
            }

            create_response_2 = api_post("/institution", data=institution_2_data)
            create_response_2.raise_for_status()
            institution_2_id = create_response_2.json().get("id")

            # Search for tag "Landesmessstelle" - should return only the first institution
            search_response = api_get("/institution", params={
                "search": create_search_query(tags="Landesmessstelle"),
                "maxResults": 100
            })
            search_response.raise_for_status()
            search_data = search_response.json()['list']

            # Verify only the first institution (with tag "Landesmessstelle") is returned
            assert len(search_data) == 1
            assert search_data[0]["id"] == institution_1_id
            assert search_data[0]["tags"] == ["Landesmessstelle"]
            assert search_data[0]["name"] == test_name_1

        finally:
            if institution_1_id:
                delete_institution_from_db(institution_1_id)
            if institution_2_id:
                delete_institution_from_db(institution_2_id)

    def test_institution_search_tags(self):
        """
        Test search for institution tags:
        1. Create a new institution with tag "Leitstelle"
        2. Create another new institution with tags "Bundesministerium" and "Leitstelle"
        3. Search for institutions with tag "Bundesministerium" - result size should be 2
        4. Search for institutions with tag "Leitstelle" - result size should be 2
        5. Search for both tags (like in multiple choice, but not available here) - should return only search for one tag
        """
        # Generate unique test data
        suffix = generate_test_username(prefix='')[1:]
        test_name_1 = f"Test Leitstelle {suffix}"
        test_meas_facil_name_1 = f"test_lst_{suffix}"
        test_meas_facil_id_1 = f"81{suffix[:5]}"

        test_name_2 = f"Test Both Tags {suffix}"
        test_meas_facil_name_2 = f"test_both_{suffix}"
        test_meas_facil_id_2 = f"82{suffix[:5]}"

        institution_1_id = None
        institution_2_id = None

        try:
            # Create a new institution with tag "Leitstelle"
            institution_1_data = {
                "name": test_name_1,
                "measFacilName": test_meas_facil_name_1,
                "measFacilId": test_meas_facil_id_1,
                "serviceBuildingStreet": "Test Street 1",
                "serviceBuildingPostalCode": "11111",
                "serviceBuildingLocation": "Test City 1",
                "serviceBuildingState": "Test State 1",
                "network": "08",
                "active": True,
                "tags": ["Leitstelle"]
            }

            create_response_1 = api_post("/institution", data=institution_1_data)
            create_response_1.raise_for_status()
            institution_1_id = create_response_1.json().get("id")

            # Create another new institution with tags "Bundesministerium" and "Leitstelle"
            institution_2_data = {
                "name": test_name_2,
                "measFacilName": test_meas_facil_name_2,
                "measFacilId": test_meas_facil_id_2,
                "serviceBuildingStreet": "Test Street 2",
                "serviceBuildingPostalCode": "22222",
                "serviceBuildingLocation": "Test City 2",
                "serviceBuildingState": "Test State 2",
                "network": "08",
                "active": True,
                "tags": ["Bundesministerium", "Leitstelle"]
            }

            create_response_2 = api_post("/institution", data=institution_2_data)
            create_response_2.raise_for_status()
            institution_2_id = create_response_2.json().get("id")

            # 3. Search for institutions with tag "Bundesministerium"
            search_response_1 = api_get("/institution", params={
                "search": create_search_query(tags="Bundesministerium"),
                "maxResults": 100
            })
            search_response_1.raise_for_status()
            search_data_1 = search_response_1.json()

            # 1 existing institution (Institution 1) + 1 new institution with both tags = 2
            assert search_data_1["size"] == 2 == len(search_data_1["list"])

            # 4. Search for institutions with tag "Leitstelle"
            search_response_2 = api_get("/institution", params={
                "search": create_search_query(tags="Leitstelle"),
                "maxResults": 100
            })
            search_response_2.raise_for_status()
            search_data_2 = search_response_2.json()

            # 1 institution with only "Leitstelle" + 1 institution with both tags = 2
            assert search_data_2["size"] == 2 == len(search_data_2["list"])

            # 5: Search for both tags as in multiple-choice
            # But this is not available, result should be only for one tag
            search_response_3 = api_get("/institution", params={
                "search": f'{create_search_query(tags="Bundesministerium")} {create_search_query(tags="Leitstelle")}',
                "maxResults": 100
            })
            search_response_3.raise_for_status()
            search_data_3 = search_response_3.json()

            assert search_data_3["size"] != 3
        finally:
            if institution_1_id:
                delete_institution_from_db(institution_1_id)
            if institution_2_id:
                delete_institution_from_db(institution_2_id)
