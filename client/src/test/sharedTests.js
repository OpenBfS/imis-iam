/* Copyright (C) 2025 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY!
 */
import { expect } from "vitest";
import i18n from "@/i18n";
import ChipTextField from "@/components/Form/ChipTextField.vue";
import { setActivePinia, createPinia } from "pinia";
import { useApplicationStore } from "@/stores/application";
import { useProfileStore } from "@/stores/profile";

const { t } = i18n.global;

const roles = [
  { name: "chief_editor", description: "roleIamChiefEditor" },
  { name: "editor", description: "roleIamEditor" },
  { name: "user", description: "roleIamUser" },
];
const networks = ["01", "02"];

const setupSharedTestEnvironment = async (item) => {
  setActivePinia(createPinia());
  const applicationStore = useApplicationStore();
  applicationStore.processType = "edit";
  applicationStore.managedItem = structuredClone(item);
  applicationStore.savedItem = structuredClone(item);
  const profileStore = useProfileStore();
  profileStore.setUserData({ role: "chief_editor" });
};

// These tests can be run for the components ManageUser and ManageInstitution
const runSharedTests = async (wrapper) => {
  let resetButton;
  const buttons = wrapper.findAll("button");
  for (let i = 0; i < buttons.length; i++) {
    if (buttons[i].text() === t("button.reset")) {
      resetButton = buttons[i];
    }
  }

  // In ManageUser.test.js we can ensure that there is only one ChipTextField by adjusting the userProfileMetadata.
  // ManageInstitution has static ChipTextFields so we have to search for one where we can add a phone number.
  // And we cannot use just any ChipTextField because we need to enter a valid value so it will be added as a chip.
  // When we search one that contains "phone" in the label we know that we can add a valid phone number.
  const allChipTextFields = wrapper.findAllComponents(ChipTextField);
  const chipTextField = allChipTextFields.find((field) =>
    field.text().includes(t("institution.phone"))
  );
  const plusButton = chipTextField.find('[data-test="plusButton"]');

  // Some functions for more convenience and less code duplication
  const isResetButtonEnabled = () => {
    return resetButton.attributes().disabled === undefined;
  };

  const expectEnabled = () => {
    expect(isResetButtonEnabled(resetButton)).toBeTruthy();
  };

  const expectDisabled = () => {
    expect(isResetButtonEnabled(resetButton)).toBeFalsy();
  };

  const numberOfChips = () => {
    return chipTextField.findAllComponents("[data-test='chip']").length;
  };

  const reset = async () => {
    await resetButton.trigger("click");
    // After the form was resetted the reset button should be disabled again
    expectDisabled();
  };

  // The reset button should be disabled when the form was opened and nothing was edited yet
  expectDisabled();

  let chips = chipTextField.findAllComponents("[data-test='chip']");
  const closeChipButton = chips[0].find("button");
  await closeChipButton.trigger("click");
  // Form should allow to reset the form if an existing chip from the ChipTextField is removed
  expectEnabled();

  await reset();

  const inputField = chipTextField.find("input");
  await inputField.setValue("a");
  // Form should allow to reset the form if text was entered in a ChipTextField but it was not added
  // as a chip yet
  expectEnabled();

  await reset();

  const validPhoneNumber = "+4912121212";
  await inputField.setValue(validPhoneNumber);
  await plusButton.trigger("click");
  expect(numberOfChips()).toBe(2);
  expectEnabled();

  await inputField.setValue("b");
  // Form should allow to reset the form if a new chip was added and some text was entered into the ChipTextField
  expectEnabled();

  // The reason why we test the network field is because it's contained by ManageUser _and_ ManageInstitution
  const positionInputWrapper = wrapper.get("input[name='network']");
  expect(positionInputWrapper.element.value).toBe(networks[0]);
  await positionInputWrapper.setValue(networks[1]);
  expect(positionInputWrapper.element.value).toBe(networks[1]);
  // Form should allow to reset the form if the value of a select was changed
  expectEnabled();

  await reset();
};

export { networks, roles, runSharedTests, setupSharedTestEnvironment };
