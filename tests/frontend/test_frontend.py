#!/usr/bin/python3
"""
Frontend tests using Selenium WebDriver.
"""

import pytest
import time
import os
from pathlib import Path
from selenium.webdriver.common.by import By
from selenium.webdriver.common.keys import Keys
from selenium.webdriver.support.ui import WebDriverWait
from selenium.webdriver.support import expected_conditions as EC
from lib.selenium_helpers import (
    get_firefox_driver,
    get_application_url,
    get_keycloak_url,
    get_realm
)
from lib.db_helpers import delete_institution_from_db
from lib.auth import TEST_USERS

# Screenshot directory
SCREENSHOT_DIR = Path(__file__).parent / "screenshots"
SCREENSHOT_DIR.mkdir(exist_ok=True)


class TestFrontendLogin:
    """Test suite for frontend login functionality"""

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

    @pytest.mark.parametrize("username,password", [
        (username, password) for username, password in TEST_USERS.items()
    ])
    def test_login_and_access_profile(self, driver, username, password):
        """
        Test login to Adressbuch and access personal info for different test users.
        Tests all users defined in TEST_USERS.
        """
        wait = WebDriverWait(driver, 10)

        # Login
        driver.get(get_application_url())
        print(f"Navigated to: {driver.current_url}")

        driver.save_screenshot(f"{SCREENSHOT_DIR}/test_{username}_1_login_form.png")

        # Fill form
        username_field = wait.until(
            EC.presence_of_element_located((By.ID, "username"))
        )
        username_field.clear()
        username_field.send_keys(username)
        password_field = driver.find_element(By.ID, "password")
        password_field.clear()
        password_field.send_keys(password)
        print(f"Entered credentials for {username}")
        login_button = driver.find_element(By.ID, "kc-login")
        login_button.click()
        print(f"Clicked login button. Redirected to: {driver.current_url}")

        # Wait for application
        wait.until(
            lambda d: "/adressbuch" in d.current_url and "/realms" not in d.current_url
        )

        # Verify toolbar title is present
        toolbar_title = wait.until(
            EC.presence_of_element_located((By.CLASS_NAME, "v-toolbar-title__placeholder"))
        )
        assert "IMIS-Adressbuch" in toolbar_title.text
        print(f"Successfully logged in as {username}")

        driver.save_screenshot(f"{SCREENSHOT_DIR}/test_{username}_2_application.png")

        # Click the toolbar button
        menu_button = wait.until(
            EC.element_to_be_clickable((By.CSS_SELECTOR, "i.mdi-dots-vertical"))
        )
        menu_button.click()
        print("Clicked toolbar menu button")
        driver.save_screenshot(f"{SCREENSHOT_DIR}/test_{username}_3_toolbar_button.png")

        # Click "Profil" menu item
        # Wait for menu animation to complete
        time.sleep(0.5)
        profile_menu_item = wait.until(
            EC.element_to_be_clickable((By.XPATH, "//div[@class='v-list-item__content'][contains(., 'Profil')]"))
        )
        profile_menu_item.click()
        print("Clicked Profil menu item")

        # Verify profile dialog
        profile_card_title = wait.until(
            EC.presence_of_element_located((By.CSS_SELECTOR, ".v-card-title .text-h5"))
        )
        expected_title = f"IMIS-Nutzer {username}"
        assert expected_title == profile_card_title.text
        print(f"Profile dialog opened with title: {profile_card_title.text}")
        cancel_button = wait.until(
            EC.presence_of_element_located((By.XPATH, "//span[@class='v-btn__content'][contains(., 'Abbrechen')]"))
        )
        assert cancel_button.is_displayed()
        print("Cancel button verified")

        driver.save_screenshot(f"{SCREENSHOT_DIR}/test_{username}_4_profile_dialog.png")

    def test_institution_duplicate_address_warning(self, authenticated_driver_factory):
        """
        Test that creating a new institution with a duplicate address triggers the warning dialog.
        Login as chefredakteur, navigate to Institutionen tab, create new institution, and trigger duplicate address warning.
        """
        # Login as chefredakteur
        driver = authenticated_driver_factory("chefredakteur")
        wait = WebDriverWait(driver, 10)

        driver.save_screenshot(f"{SCREENSHOT_DIR}/test_institution_duplicate_1_logged_in.png")

        # Switch to "Institutionen" tab
        institutions_tab = wait.until(
            EC.element_to_be_clickable((By.XPATH, "//button[@role='tab' and contains(., 'Institutionen')]"))
        )
        institutions_tab.click()
        print("Switched to Institutionen tab")
        time.sleep(1)

        driver.save_screenshot(f"{SCREENSHOT_DIR}/test_institution_duplicate_2_institutions_tab.png")

        # Click "Institution hinzufügen" button to create a new institution
        # The button has the icon mdi-office-building-plus
        add_button = wait.until(
            EC.element_to_be_clickable((By.XPATH, "//button[.//i[contains(@class, 'mdi-office-building-plus')]]"))
        )
        add_button.click()
        print("Clicked 'Institution hinzufügen' button")
        time.sleep(1)

        driver.save_screenshot(f"{SCREENSHOT_DIR}/test_institution_duplicate_3_create_dialog.png")

        # Fill in the name first
        name_field = wait.until(
            EC.presence_of_element_located((By.XPATH, "//label[contains(., 'Name')]/following-sibling::input"))
        )
        unique_name = f"TestInstitution_{int(time.time())}"
        name_field.click()
        name_field.send_keys(unique_name)
        print(f"Entered institution name: {unique_name}")

        # Fill in the address with duplicate values (Institution 1's address)
        # Find the input fields by their labels
        ort_field = wait.until(
            EC.presence_of_element_located((By.XPATH, "//label[contains(., 'Ort des Dienstgebäudes')]/following-sibling::input"))
        )
        ort_field.click()
        ort_field.send_keys("ExampleLocation-1")
        plz_field = driver.find_element(By.XPATH, "//label[contains(., 'PLZ des Dienstgebäudes')]/following-sibling::input")
        plz_field.click()
        plz_field.send_keys("12345")
        strasse_field = driver.find_element(By.XPATH, "//label[contains(., 'Straße/Hausnummer des Dienstgebäudes')]/following-sibling::input")
        strasse_field.click()
        strasse_field.send_keys("Examplestreet 1")

        driver.save_screenshot(f"{SCREENSHOT_DIR}/test_institution_duplicate_4_changed_data.png")

        # click somewhere else to trigger duplicate check
        driver.find_element(By.TAG_NAME, "body").click()
        time.sleep(1)  # Wait for duplicate check
        driver.save_screenshot(f"{SCREENSHOT_DIR}/test_institution_duplicate_5_duplicate_warning.png")

        # Verify that the "Institution existiert bereits" popup appears
        duplicate_dialog_title = wait.until(
            EC.presence_of_element_located((By.XPATH, "//div[contains(@class, 'v-card-title') and contains(., 'Institution existiert bereits')]"))
        )
        assert duplicate_dialog_title.is_displayed()
        print("Duplicate institution warning dialog appeared")

        # Click "Ja, fortfahren" to confirm and continue despite duplicate warning
        confirm_button = wait.until(
            EC.element_to_be_clickable((By.XPATH, "//span[@class='v-btn__content'][contains(., 'Ja, fortfahren')]"))
        )
        confirm_button.click()
        print("Dismisses duplicate warning")
        time.sleep(0.5)

        driver.save_screenshot(f"{SCREENSHOT_DIR}/test_institution_duplicate_6_before_category.png")

        # Click on the input category field
        category_input = driver.find_element(By.NAME, "tags")
        print(f'category_input {category_input}')
        category_input.click()
        time.sleep(0.3)
        print("Opened category dropdown")

        # Select a category option - use the 7th child as detected by Selenium IDE
        category_option = wait.until(
            EC.presence_of_element_located((By.CSS_SELECTOR, ".v-list-item:nth-child(7)"))
        )
        print(f'category_option {category_option}')
        driver.execute_script("arguments[0].click();", category_option)
        print("Selected category")
        driver.save_screenshot(f"{SCREENSHOT_DIR}/test_institution_duplicate_6a_category_selected.png")
        time.sleep(0.5)

        # Submit the form by clicking the submit button
        save_button = wait.until(
            EC.element_to_be_clickable((By.XPATH, "//button[.//span[contains(., 'Erstellen')]]"))
        )
        save_button.click()
        print("Clicked save button to submit the form")
        time.sleep(1)  # Wait for save operation

        driver.save_screenshot(f"{SCREENSHOT_DIR}/test_institution_duplicate_7_after_save.png")

        # Wait for the create dialog to disappear
        wait.until(
            EC.invisibility_of_element_located((By.XPATH, "//div[contains(@class, 'v-card-title')]//span[contains(@class, 'text-h5')]"))
        )
        print("Create dialog closed, form submitted successfully")

        # Reopen the newly created institution to verify the values
        edit_button = wait.until(
            EC.element_to_be_clickable((By.XPATH, f"//tr[contains(., '{unique_name}')]//button[contains(@aria-label, 'edit') or .//i[contains(@class, 'mdi-pencil')]]"))
        )
        edit_button.click()
        print(f"Reopened {unique_name} for verification")
        time.sleep(1)

        driver.save_screenshot(f"{SCREENSHOT_DIR}/test_institution_duplicate_8_reopened.png")

        # Verify the name
        name_verify = wait.until(
            EC.presence_of_element_located((By.XPATH, "//label[contains(., 'Name')]/following-sibling::input"))
        )
        actual_name = name_verify.get_attribute("value")
        assert actual_name == unique_name
        print(f"Verified name: {actual_name}")

        # Get the address values
        ort_field = driver.find_element(By.XPATH, "//label[contains(., 'Ort des Dienstgebäudes')]/following-sibling::input")
        plz_field = driver.find_element(By.XPATH, "//label[contains(., 'PLZ des Dienstgebäudes')]/following-sibling::input")
        strasse_field = driver.find_element(By.XPATH, "//label[contains(., 'Straße/Hausnummer des Dienstgebäudes')]/following-sibling::input")
        actual_ort = ort_field.get_attribute("value")
        actual_plz = plz_field.get_attribute("value")
        actual_strasse = strasse_field.get_attribute("value")

        # Verify the address values
        assert actual_ort == "ExampleLocation-1"
        assert actual_plz == "12345"
        assert actual_strasse == "Examplestreet 1"
        print(f"Verified address values: Ort={actual_ort}, PLZ={actual_plz}, Straße={actual_strasse}")

        delete_institution_from_db(institution_name=unique_name)

    def test_institution_no_duplicate_check_for_existing(self, authenticated_driver_factory):
        """
        Test that the duplicate address check does NOT trigger when viewing an existing institution
        """
        # Login as exampleuser
        driver = authenticated_driver_factory("exampleuser")
        wait = WebDriverWait(driver, 10)

        driver.save_screenshot(f"{SCREENSHOT_DIR}/test_institution_no_duplicate_check_for_exampleuser_1_logged_in.png")

        # Switch to "Institutionen" tab
        institutions_tab = wait.until(
            EC.element_to_be_clickable((By.XPATH, "//button[@role='tab' and contains(., 'Institutionen')]"))
        )
        institutions_tab.click()
        print("Switched to Institutionen tab")
        time.sleep(1)

        driver.save_screenshot(f"{SCREENSHOT_DIR}/test_institution_no_duplicate_check_for_exampleuser_2_institutions_tab.png")

        # Click the view button
        view_button = wait.until(
            EC.element_to_be_clickable((By.XPATH, "//tr[contains(., 'inst_2')]//button[contains(@aria-label, 'edit') or .//i[contains(@class, 'mdi-information-outline')]]"))
        )
        institution_name = "inst_2"

        view_button.click()
        print(f"Clicked edit button for {institution_name}")
        time.sleep(1)

        driver.save_screenshot(f"{SCREENSHOT_DIR}/test_institution_no_duplicate_check_for_exampleuser_3_edit_dialog.png")

        # Click in the PLZ field
        plz_field = wait.until(
            EC.presence_of_element_located((By.XPATH, "//label[contains(., 'PLZ des Dienstgebäudes')]/following-sibling::input"))
        )
        plz_field.click()
        print("Clicked PLZ field")
        time.sleep(0.5)

        driver.save_screenshot(f"{SCREENSHOT_DIR}/test_institution_no_duplicate_check_for_exampleuser_4_plz_clicked.png")

        # click somewhere else
        driver.find_element(By.TAG_NAME, "body").click()
        time.sleep(1)  # Wait for duplicate check

        driver.save_screenshot(f"{SCREENSHOT_DIR}/test_institution_no_duplicate_check_for_exampleuser_5_after_blur.png")

        # Verify that the duplicate warning dialog does NOT appear
        duplicate_dialogs = driver.find_elements(By.XPATH, "//div[contains(@class, 'v-card-title') and contains(., 'Institution existiert bereits')]")

        assert len(duplicate_dialogs) == 0

    def test_edit_profile(self, authenticated_driver_factory):
        """
        Test editing user profile, searching by custom field, and verifying the changes.
        """
        # Login as exampleuser
        driver = authenticated_driver_factory("exampleuser")
        wait = WebDriverWait(driver, 10)

        driver.save_screenshot(f"{SCREENSHOT_DIR}/test_edit_profile_1_logged_in.png")

        # Open own profile
        menu_button = wait.until(
            EC.element_to_be_clickable((By.CSS_SELECTOR, "i.mdi-dots-vertical"))
        )
        menu_button.click()
        print("Clicked toolbar menu button")
        time.sleep(0.5)
        profile_menu_item = wait.until(
            EC.presence_of_element_located((By.XPATH, "//div[contains(@class, 'v-list-item') and .//div[contains(., 'Profil')]]"))
        )
        driver.execute_script("arguments[0].click();", profile_menu_item)
        print("Clicked Profil menu item to open own profile")
        time.sleep(1)

        driver.save_screenshot(f"{SCREENSHOT_DIR}/test_edit_profile_2_profile_opened.png")

        # Enter Titel
        unique_title = f"TestTitel_{int(time.time())}"
        titel_field = wait.until(
            EC.presence_of_element_located((By.XPATH, "//label[contains(., 'Titel')]/following-sibling::input"))
        )
        titel_field.click()
        titel_field.send_keys(Keys.CONTROL + "a")
        titel_field.send_keys(Keys.DELETE)
        titel_field.send_keys(unique_title)
        print(f"Entered Titel: {unique_title}")

        # Enter E-Mail 2
        email2_field = driver.find_element(By.XPATH, "//label[contains(., 'E-Mail 2')]/following-sibling::input")
        email2_field.click()
        email2_field.send_keys(Keys.CONTROL + "a")
        email2_field.send_keys(Keys.DELETE)
        email2_field.send_keys("test.email2@example.com")
        print("Entered E-Mail 2")

        # Enter BAW-E-Mail (ChipTextField - multi-value field)
        baw_email_field = driver.find_element(By.XPATH, "//label[contains(., 'BAW-E-Mail')]/..//input")
        baw_email_field.click()
        baw_email_field.send_keys(Keys.BACKSPACE)  # remove previous entry
        baw_email_field.send_keys("baw.test@example.com")
        baw_email_field.send_keys(Keys.ENTER)
        print("Entered BAW-E-Mail")

        # Enter BAW-Telefon (ChipTextField - multi-value field)
        baw_phone_field = driver.find_element(By.XPATH, "//label[contains(., 'BAW-Telefon')]/..//input")
        baw_phone_field.click()
        baw_phone_field.send_keys(Keys.BACKSPACE)  # remove previous entry
        baw_phone_field.send_keys("+49123456789")
        baw_phone_field.send_keys(Keys.ENTER)
        print("Entered BAW-Telefon")

        # Enter BAW-SMS (ChipTextField - multi-value field)
        baw_sms_field = driver.find_element(By.XPATH, "//label[contains(., 'BAW-SMS')]/..//input")
        baw_sms_field.click()
        baw_sms_field.send_keys(Keys.BACKSPACE)  # remove previous entry
        baw_sms_field.send_keys("+49987654321")
        baw_sms_field.send_keys(Keys.ENTER)
        print("Entered BAW-SMS")

        driver.save_screenshot(f"{SCREENSHOT_DIR}/test_edit_profile_3_fields_filled.png")

        # Modify institutions - scroll to element and use JavaScript click
        institutions_dropdown = driver.find_element(By.XPATH, "//div[contains(@class, 'v-card')]//label[contains(., 'Institutionen')]/following-sibling::div//input")
        # Scroll element into view
        driver.execute_script("arguments[0].scrollIntoView({block: 'center'});", institutions_dropdown)
        institutions_dropdown.click()
        time.sleep(0.3)
        print("Opened institutions dropdown")
        time.sleep(0.2)

        # Remove Institution 1
        inst1_options = driver.find_elements(By.XPATH, "//div[contains(@class, 'v-list-item')]//div[contains(., 'Institution 1')]")
        driver.execute_script("arguments[0].click();", inst1_options[0])
        print("Removed Institution 1")
        time.sleep(0.3)

        # Add Institution 2
        inst2_options = driver.find_elements(By.XPATH, "//div[contains(@class, 'v-list-item')]//div[contains(., 'Institution 2')]")
        driver.execute_script("arguments[0].click();", inst2_options[0])
        print("Added Institution 2")
        time.sleep(0.3)

        # Close dropdown by clicking elsewhere
        driver.find_element(By.TAG_NAME, "body").click()

        driver.save_screenshot(f"{SCREENSHOT_DIR}/test_edit_profile_4_institutions_modified.png")

        # Save
        save_button = wait.until(
            EC.element_to_be_clickable((By.XPATH, "//button[.//span[contains(., 'Speichern')]]"))
        )
        save_button.click()
        print("Clicked save button")
        time.sleep(1)

        driver.save_screenshot(f"{SCREENSHOT_DIR}/test_edit_profile_5_saved.png")

        # Open column selection to add Titel column
        column_selection_button = wait.until(
            EC.element_to_be_clickable((By.XPATH, "//button[@id='column-selection' or .//i[contains(@class, 'mdi-view-column')]]"))
        )
        column_selection_button.click()
        print("Opened column selection")
        time.sleep(0.5)

        driver.save_screenshot(f"{SCREENSHOT_DIR}/test_edit_profile_6_column_selection.png")

        time.sleep(0.5)
        # Enable Titel column by clicking the checkbox
        # It actually enables all columns
        # Then navigate up to find the parent list item
        titel_span = wait.until(
            EC.presence_of_element_located((By.XPATH, "//span[normalize-space(text())='Titel' and contains(@class, 'me-2')]"))
        )
        # Get the parent list item
        titel_item = titel_span.find_element(By.XPATH, "./ancestor::div[contains(@class, 'v-list-item')]")

        # Check if already checked by finding the icon
        icon = titel_item.find_element(By.XPATH, ".//i[contains(@class, 'mdi')]")
        is_checked = 'mdi-checkbox-marked' in icon.get_attribute('class')
        if not is_checked:
            # Click the checkbox input element directly (not the list item)
            checkbox_input = titel_item.find_element(By.XPATH, ".//input[@type='checkbox']")
            checkbox_input.click()
            print("Enabled Titel column")
            time.sleep(0.5)
        else:
            print("Titel column already enabled")

        driver.save_screenshot(f"{SCREENSHOT_DIR}/test_edit_profile_6_column_selection_clicked.png")

        # Close menu by clicking elsewhere
        driver.find_element(By.TAG_NAME, "body").click()

        # Wait for the table to update with the new column
        time.sleep(0.5)

        driver.save_screenshot(f"{SCREENSHOT_DIR}/test_edit_profile_7_titel_column_added.png")

        driver.save_screenshot(f"{SCREENSHOT_DIR}/test_edit_profile_8_search_results.png")

        # Get all rows
        result_rows = driver.find_elements(By.XPATH, "//tbody//tr[contains(@class, 'v-data-table__tr')]")
        print(f"Found {len(result_rows)} rows in table")

        # Didn't manage to use the filter, so just iterate
        # The Titel column is at index 4
        target_row = None
        for row in result_rows:
            cells = row.find_elements(By.TAG_NAME, "td")
            if len(cells) > 4:
                titel_cell = cells[4]
                if unique_title in titel_cell.text:
                    target_row = row
                    print(f"Found row with Titel: {unique_title}")
                    break
        else:
            raise AssertionError(f"Could not find row with Titel '{unique_title}' in table")

        # Open entry
        view_button = target_row.find_element(By.XPATH, ".//button[.//i[contains(@class, 'mdi-information-outline')]]")
        view_button.click()
        print("Opened entry from table row")
        time.sleep(1)

        driver.save_screenshot(f"{SCREENSHOT_DIR}/test_edit_profile_9_verification_dialog.png")

        # Verify values
        titel_verify = wait.until(
            EC.presence_of_element_located((By.XPATH, "//label[contains(., 'Titel')]/following-sibling::input"))
        )
        assert titel_verify.get_attribute("value") == unique_title
        print("Verified Titel")
        email2_verify = driver.find_element(By.XPATH, "//label[contains(., 'E-Mail 2')]/following-sibling::input")
        assert email2_verify.get_attribute("value") == "test.email2@example.com"
        print("Verified E-Mail 2")

        driver.save_screenshot(f"{SCREENSHOT_DIR}/test_edit_profile_10_verified.png")
