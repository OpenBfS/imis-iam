#!/usr/bin/python3
"""
Tests for '/iam/mail'

TODO:
POST /mail - Send mail
GET /mail/archive/{id} - Archive mail
"""

import pytest
from lib import api_get, api_post


class TestMailAPI:
    """Test suite for /iam/mail endpoints"""

    def test_list_mails(self):
        """Test GET /iam/mail - List mails"""
        response = api_get("/mail", params={"count": 10})
        response.raise_for_status()

        mails = response.json()
        assert mails == []

    def test_get_mail_types(self):
        """Test GET /iam/mail/type - List mail types"""
        response = api_get("/mail/type")
        response.raise_for_status()

        data = response.json()
        assert len(data) == 8  # See example data in docker/keycloak/add_example_data.sql
        for tag in data:
            assert list(tag.keys()) == ['id', 'name']
