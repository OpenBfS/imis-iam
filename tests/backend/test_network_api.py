#!/usr/bin/python3
"""
Tests for '/iam/networks'
"""

import pytest
from lib import api_get


class TestNetworkAPI:
    """Test suite for /iam/networks endpoints"""

    def test_list_networks(self):
        """Test GET /iam/networks/ - List all networks"""
        response = api_get("/networks/")
        response.raise_for_status()

        networks = response.json()

        assert len(networks) >= 2

        for network in networks:
            assert "name" in network
            assert isinstance(network["name"], str)

        network_names = [n["name"] for n in networks]
        assert "08" in network_names
        assert "20" in network_names
