#!/usr/bin/python3
"""
Tests for '/iam/mail'
"""

import pytest
from lib import api_get, api_post, get_auth


class TestMailAPI:
    """Test suite for /iam/mail endpoints"""

    def test_list_mails(self):
        """Test GET /iam/mail - List mails"""
        auth = get_auth()
        try:
            auth.switch_user_context("exampleuser")
            response = api_get("/mail", params={"count": 10})
            response.raise_for_status()

            mails = response.json()
            assert mails == []
        finally:
            auth.clear_user_context()

    def test_get_mail_types(self):
        """Test GET /iam/mail/type - List mail types"""
        response = api_get("/mail/type")
        response.raise_for_status()

        data = response.json()
        assert len(data) == 8  # See example data in docker/keycloak/add_example_data.sql
        for tag in data:
            assert list(tag.keys()) == ['id', 'name']

    def test_send_mail(self):
        """
        Test POST /iam/mail - Send mail
        1. Get list of users to find recipient IDs
        2. Send a mail to those users
        3. Verify mail appears in mail list
        4. Archive the mail
        """
        # Get users to send mail to
        users_response = api_get("/user", params={"maxResults": 2})
        users_response.raise_for_status()
        users = users_response.json()["list"]

        assert len(users) >= 2
        recipient_ids = [users[0]["id"], users[1]["id"]]

        # Prepare example mail data
        mail_data = {
            "sender": "test@example.com",
            "subject": "Test Mail Subject",
            "text": "This is a test mail body.",
            "type": 1,  # Type 1 = 'Einladung'
            "archived": False,
            "recipients": recipient_ids
        }

        # Send the mail
        send_response = api_post("/mail", mail_data)
        send_response.raise_for_status()

        # Verify the mail appears in the mail list
        list_response = api_get("/mail", params={
            "count": 10,
            "archived": False
        })
        list_response.raise_for_status()
        mails = list_response.json()

        assert len(mails) == 1

        print(mails)

        # Find our sent mail
        sent_mail = None
        for mail in mails:
            if (mail["subject"] == "Test Mail Subject" and
                mail["sender"] == "test@example.com"):
                sent_mail = mail
                break

        assert sent_mail is not None
        assert sent_mail["text"] == "This is a test mail body."
        assert sent_mail["type"] == 1
        assert sent_mail["archived"] is False
        assert set(sent_mail["recipients"]) == set(recipient_ids)

        # Archive the mail
        archive_response = api_get(f"/mail/archive/{sent_mail['id']}")
        archive_response.raise_for_status()
        assert archive_response.status_code == 200

        # Verify mail is archived
        archived_list_response = api_get("/mail", params={
            "count": 10,
            "archived": True
        })
        archived_list_response.raise_for_status()
        archived_mails = archived_list_response.json()

        # Find the archived mail
        archived_mail = None
        for mail in archived_mails:
            if mail["id"] == sent_mail["id"]:
                archived_mail = mail
                break
        assert archived_mail is not None
        assert archived_mail["archived"] is True
