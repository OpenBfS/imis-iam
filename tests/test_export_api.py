#!/usr/bin/python3
"""
Tests for '/iam/export/user' and '/iam/export/institution'

TODO:
Test with different encodings
"""

import pytest
from lib import api_get, create_search_query


class TestExportAPI:
    """Test suite for /iam/export endpoints"""

    def test_export_users_basic(self):
        """Test GET /iam/export/user - Export users to CSV"""
        response = api_get("/export/user", params={
            "fieldSeparator": ";",
            "encoding": "iso-8859-15",
            "rowDelimiter": "\r\n",
            "quoteType": '"',
            "attributes": "firstName",
            "search": create_search_query(enabled="true")  # ignored disabled users (created by the user-tests)
        })
        response.raise_for_status()

        csv_lines = response.text.strip().splitlines()
        assert len(csv_lines) == 4  # 4 lines (1 header + 3 users)
        assert csv_lines[0] == 'firstName'

    def test_export_users_multiple_attributes(self):
        """Test GET /iam/export/user - Export users with multiple attributes"""
        response = api_get("/export/user", params={
            "fieldSeparator": ";",
            "encoding": "iso-8859-15",
            "rowDelimiter": "\r\n",
            "quoteType": '"',
            "attributes": ["firstName", "lastName", "email", "username", "institutions"],
            "search": create_search_query(enabled="true")
        })
        response.raise_for_status()

        csv_lines = response.text.strip().splitlines()
        assert len(csv_lines) == 4

        # Verify header
        header = csv_lines[0].split(';')
        assert len(header) == 5
        assert "firstName" in header
        assert "lastName" in header
        assert "email" in header
        assert "username" in header
        assert "institutions" in header

        # Verify data lines
        for line in csv_lines[1:]:
            assert len(line.split(";")) == 5

    def test_export_users_separators(self):
        """Test GET /iam/export/user - Export with different field separators"""
        for separator in (",", "\t", "."):
            response = api_get("/export/user", params={
                "fieldSeparator": separator,
                "encoding": "utf-8",
                "rowDelimiter": "\n",
                "quoteType": '"',
                "attributes": ["firstName", "lastName"],
                "search": create_search_query(enabled="true")
            })
            response.raise_for_status()

            csv_lines = response.text.strip().splitlines()
            assert separator in csv_lines[0]

    def test_export_users_with_search_filter(self):
        """Test GET /iam/export/user - Export with search filter"""
        response = api_get("/export/user", params={
            "fieldSeparator": ";",
            "encoding": "utf-8",
            "rowDelimiter": "\r\n",
            "quoteType": '"',
            "attributes": "username",
            "search": create_search_query(username="chefredakteur"),
        })
        response.raise_for_status()

        csv_lines = response.text.strip().splitlines()
        assert len(csv_lines) == 2
        assert "chefredakteur" in csv_lines[1]

    def test_export_institutions_basic(self):
        """Test GET /iam/export/institution - Export institutions to CSV"""
        response = api_get("/export/institution", params={
            "fieldSeparator": ";",
            "encoding": "iso-8859-15",
            "rowDelimiter": "\r\n",
            "quoteType": '"',
        })
        response.raise_for_status()

        csv_data = response.text.strip().splitlines()
        assert len(csv_data) == 3
        assert 'active;' in csv_data[0]  # header contains all available attributes
        assert 'inst_1;' in csv_data[1]
        assert 'inst_2;' in csv_data[2]

    def test_export_institutions_specific_attributes(self):
        """Test GET /iam/export/institution - Export institutions with specific attributes"""
        response = api_get("/export/institution", params={
            "fieldSeparator": ";",
            "encoding": "utf-8",
            "rowDelimiter": "\r\n",
            "quoteType": '"',
            "attributes": ["name", "measFacilName", "network"],
        })
        response.raise_for_status()

        csv_lines = response.text.strip().splitlines()
        assert len(csv_lines) == 3  # Header plus 2 data lines

        # Verify header
        header = csv_lines[0]
        assert "name" in header
        assert "measFacilName" in header
        assert "network" in header

    def test_export_institutions_with_id_filter(self):
        """Test GET /iam/export/institution - Export specific institutions by ID"""
        # First get institution IDs
        list_response = api_get("/institution", params={"maxResults": 1})
        list_response.raise_for_status()

        institutions = list_response.json()["list"]
        institution_id = institutions[0]["id"]

        # Export only this institution
        response = api_get("/export/institution", params={
            "fieldSeparator": ";",
            "encoding": "utf-8",
            "rowDelimiter": "\r\n",
            "quoteType": '"',
            "id": [institution_id],
        })
        response.raise_for_status()

        csv_lines = response.text.strip().splitlines()
        assert len(csv_lines) == 2

    def test_export_empty_results(self):
        """Test export with search that returns no results"""
        response = api_get("/export/user", params={
            "fieldSeparator": ";",
            "encoding": "utf-8",
            "rowDelimiter": "\r\n",
            "quoteType": '"',
            "attributes": "username",
            "search": create_search_query(username="nonexistentuser12345"),
        })
        response.raise_for_status()

        # Empty result, not even the header
        assert response.text.strip() == ''
