#!/usr/bin/python3
"""
Tests for '/iam/event'

Missing tests:
- Query with filters
"""

import pytest
from datetime import datetime, timedelta, timezone
from lib import api_get, api_post, api_put, api_delete, get_auth


class TestEventAPI:
    """Test suite for /iam/event endpoints"""

    def test_list_events_as_exampleuser(self):
        """Test GET /iam/event as exampleuser"""
        auth = get_auth()
        try:
            auth.switch_user_context("exampleuser")
            response = api_get("/event")
            response.raise_for_status()
            events = response.json()
            assert isinstance(events, list)
        finally:
            auth.clear_user_context()

    def test_event_lifecycle_create_update_query_delete(self):
        """
        Test complete event lifecycle
        """
        # Generate test data
        test_title = f"Test Event {datetime.now().strftime('%Y%m%d_%H%M%S')}"
        test_description = "This is a test event for API testing"
        test_site = "Test site"
        start_datetime = datetime.now(timezone.utc) + timedelta(days=1)
        end_datetime = datetime.now(timezone.utc) + timedelta(days=2)
        start_date = start_datetime.strftime("%Y-%m-%d")
        end_date = end_datetime.strftime("%Y-%m-%d")
        # Convert to timestamps (milliseconds since epoch) for comparison
        # API returns dates as timestamps in milliseconds and in UTC
        start_timestamp = int(start_datetime.replace(hour=0, minute=0, second=0, microsecond=0).timestamp() * 1000)
        end_timestamp = int(end_datetime.replace(hour=0, minute=0, second=0, microsecond=0).timestamp() * 1000)

        created_event_id = None

        try:
            # Create a new event
            event_data = {
                "title": test_title,
                "description": test_description,
                "site": test_site,
                "startDate": start_date,
                "endDate": end_date,
            }

            create_response = api_post("/event", data=event_data)
            create_response.raise_for_status()

            # Extract the Event ID
            created_event = create_response.json()
            created_event_id = created_event.get("id")

            print(f"Created event with ID {created_event_id}")

            # Query for the event and verify attributes
            get_response = api_get(f"/event/{created_event_id}")
            get_response.raise_for_status()
            retrieved_event = get_response.json()
            assert retrieved_event["id"] == created_event_id
            assert retrieved_event["title"] == test_title
            assert retrieved_event["description"] == test_description
            assert retrieved_event["site"] == test_site
            assert retrieved_event["startDate"] == start_timestamp
            assert retrieved_event["endDate"] == end_timestamp

            # Also verify the event appears in the list
            list_response = api_get("/event")
            list_response.raise_for_status()
            events_list = list_response.json()
            event_found_in_list = any(
                event["id"] == created_event_id for event in events_list
            )
            assert event_found_in_list

            # Update the event
            updated_title = f"{test_title} - Updated"
            updated_description = "Updated description"
            update_data = {
                "id": created_event_id,
                "title": updated_title,
                "description": updated_description,
                "site": test_site,
                "startDate": start_date,
                "endDate": end_date,
            }
            update_response = api_put("/event", data=update_data)
            update_response.raise_for_status()

            # Query list and verify the updated attributes
            verify_response = api_get(f"/event/{created_event_id}")
            verify_response.raise_for_status()

            updated_event = verify_response.json()
            assert updated_event["id"] == created_event_id
            assert updated_event["title"] == updated_title
            assert updated_event["description"] == updated_description

            # Delete the event
            delete_response = api_delete(f"/event/{created_event_id}")
            delete_response.raise_for_status()

            # Verify the event no longer exists
            get_deleted_response = api_get(f"/event/{created_event_id}")
            assert get_deleted_response.status_code == 404

            # Verify it's not in the events list
            final_list_response = api_get("/event")
            final_list_response.raise_for_status()
            final_events_list = final_list_response.json()
            event_still_in_list = any(
                event["id"] == created_event_id for event in final_events_list
            )
            assert not event_still_in_list

            # Mark that cleanup is done
            created_event_id = None

        finally:
            # Cleanup: If test failed and event still exists, try to delete it
            if created_event_id is not None:
                try:
                    cleanup_response = api_delete(f"/event/{created_event_id}")
                except Exception as exc:
                    print(f"Warning: Cleanup failed for event {created_event_id}: {exc}")

    def test_get_event_by_invalid_id(self):
        """Test GET /iam/event/{id} with invalid event ID"""
        response = api_get("/event/00000000-0000-0000-0000-000000000000")
        assert response.status_code == 404

    def test_create_event_with_missing_fields(self):
        """Test POST /iam/event - Creating event with missing required fields"""
        incomplete_data = {
            "title": "Incomplete Event"
        }

        response = api_post("/event", data=incomplete_data)

        assert response.status_code == 400

    def test_update_nonexistent_event(self):
        """Test PUT /iam/event with invalid event ID"""
        update_data = {
            "id": "00000000-0000-0000-0000-000000000000",
            "title": "This won't work",
            "startDate": "2025-01-01",
            "endDate": "2025-01-02",
            "description": "Description of something non-existent",
            "site": "Nowhere",
        }
        response = api_put("/event", data=update_data)
        assert response.status_code == 400

    def test_delete_nonexistent_event(self):
        """Test DELETE /iam/event/{id} with invalid event ID"""
        response = api_delete("/event/00000000-0000-0000-0000-000000000000")
        assert response.status_code == 404
