#!/usr/bin/python3
"""
Selenium helper functions for frontend testing.
"""

import os
from selenium import webdriver
from selenium.webdriver import FirefoxService, FirefoxOptions
from webdriver_manager.firefox import GeckoDriverManager


def get_firefox_driver(headless: bool = None):
    """
    Create and configure a Firefox WebDriver instance.
    """
    options = FirefoxOptions()

    if headless is None:
        headless_env = os.getenv('SELENIUM_HEADLESS', 'true')
        # Convert string to boolean: 'false', '0', 'no' are False, everything else is True
        headless = headless_env.lower() not in ('false', '0', 'no', '')
    if headless:
        options.add_argument("--headless")

    # Additional options for better stability
    options.add_argument("--width=1920")
    options.add_argument("--height=1080")

    # Use webdriver-manager to automatically manage geckodriver
    #service = FirefoxService(GeckoDriverManager().install())
    #driver = webdriver.Firefox(service=service, options=options)

    driver = webdriver.Firefox(options=options)

    # Set implicit wait
    driver.implicitly_wait(10)

    # Set window size
    #driver.set_window_size(1920, 1080)

    return driver


def get_keycloak_url():
    """
    Get Keycloak base URL from environment or use default.
    """
    return os.getenv('KEYCLOAK_URL', 'http://localhost:48081')


def get_realm():
    """
    Get realm name from environment or use default.
    """
    return os.getenv('REALM', 'imis3')


def get_application_url():
    """
    Get the Keycloak login URL for the realm.
    """
    keycloak_url = get_keycloak_url()
    realm = get_realm()
    return f"{keycloak_url}/adressbuch/"
