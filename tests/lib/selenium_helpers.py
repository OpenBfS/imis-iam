#!/usr/bin/python3
"""
Selenium helper functions for frontend testing.
"""

import pytest
import os
from selenium import webdriver
from selenium.webdriver import FirefoxService, FirefoxOptions
from selenium.webdriver.support.ui import WebDriverWait
from webdriver_manager.firefox import GeckoDriverManager
from selenium.webdriver.support import expected_conditions as EC
from selenium.webdriver.common.by import By
from .auth import TEST_USERS


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


class SeleniumTestSuite:
    """Base Test suite for Integration tests"""

    @pytest.fixture(scope="function")
    def driver(self):
        """
        Fixture that provides a Firefox WebDriver instance.
        Automatically closes the browser after each test.
        """
        driver = get_firefox_driver()
        driver.set_page_load_timeout(os.getenv('REQUEST_TIMEOUT', '30'))
        yield driver
        driver.quit()

    @pytest.fixture(scope="function")
    def authenticated_driver_factory(self):
        """
        Factory fixture that provides authenticated Firefox WebDriver instances.
        Returns a function that can login as different users.
        """
        drivers = []

        def _create_authenticated_driver(username="exampleuser"):
            """
            Create and return an authenticated driver for the specified user.

            Args:
                username: Username to login with (default: "exampleuser")

            Returns:
                Authenticated WebDriver instance
            """
            driver = get_firefox_driver()
            drivers.append(driver)

            # Login as specified user
            driver.get(get_application_url())
            wait = WebDriverWait(driver, 10)

            username_field = wait.until(
                EC.presence_of_element_located((By.ID, "username"))
            )
            username_field.send_keys(username)

            password_field = driver.find_element(By.ID, "password")
            password_field.send_keys(TEST_USERS[username])

            login_button = driver.find_element(By.ID, "kc-login")
            login_button.click()

            # Wait for application to load
            wait.until(
                lambda d: "/adressbuch" in d.current_url and "/realms" not in d.current_url
            )
            toolbar_title = wait.until(
                EC.presence_of_element_located((By.CLASS_NAME, "v-toolbar-title__placeholder"))
            )
            assert "IMIS-Adressbuch" in toolbar_title.text

            print(f"Authenticated as {username}")
            return driver

        yield _create_authenticated_driver

        # Cleanup all drivers created by this factory
        for driver in drivers:
            driver.quit()
