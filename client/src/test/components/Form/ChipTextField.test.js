/* Copyright (C) 2025 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY!
 */
import { vi } from "vitest";
import { setActivePinia, createPinia } from "pinia";
import { flushPromises, mount } from "@vue/test-utils";
import ChipTextField from "@/components/Form/ChipTextField.vue";
import global from "@/test/components/global";
import { test, expect } from "vitest";
import i18n from "@/i18n";
import { useApplicationStore } from "@/stores/application";
import { networks, setupSharedTestEnvironment } from "@/test/sharedTests";
import { ref } from "vue";

const { t } = i18n.global;

setActivePinia(createPinia());
const applicationStore = useApplicationStore();

const firstName = "One";
const user = {
  id: "1",
  role: "chief_editor",
  network: networks[0],
  attributes: {
    username: ["one"],
    firstName: [firstName],
    operationModeChangePhoneNumbers: ["+4912345678"],
  },
};
setupSharedTestEnvironment(user, "user", "chief_editor");

const errorMessage = "Does not match";
const rulesToSpyOn = {
  propRule: (v) => v == "" || v !== "a" || errorMessage,
  clientRule: () => true,
};
const propRuleSpy = vi.spyOn(rulesToSpyOn, "propRule");
const clientRuleSpy = vi.spyOn(rulesToSpyOn, "clientRule");

applicationStore.managedItems;

let changedAttributes = ref([]),
  submitChange = () => {},
  removeChange = () => {},
  addResetEventListener = () => {},
  onUpdateModelValue = () => {},
  clientAndServerRules = ref({ test: [rulesToSpyOn.clientRule] }),
  form;

const wrapper = mount(ChipTextField, {
  global: {
    provide: {
      managedItemIndex: 0,
      useForm: {
        changedAttributes,
        submitChange,
        removeChange,
        addResetEventListener,
        onUpdateModelValue,
        clientAndServerRules,
        form,
      },
    },
    ...global,
  },
  props: {
    attribute: "test",
    clearable: true,
    rules: [rulesToSpyOn.propRule],
  },
});
const inputField = wrapper.find("input");
const button = wrapper.find("button");

test("Test ChipTextField", async () => {
  let chip = wrapper.findComponent("[data-test='chip']");
  // An object with zero keys means that no chip was found
  expect(Object.keys(chip)).toHaveLength(0);

  // Don't allow to add empty values so the button should be disabled (represented by an empty string
  // in "disabled")
  expect(button.attributes().disabled).toBe("");

  await inputField.setValue("a");
  await flushPromises();
  expect(wrapper.html()).toContain(errorMessage);

  await inputField.setValue("b");
  await flushPromises();
  // The button should not be disabled anymore when the input value is valid
  expect(button.attributes().disabled).toBeUndefined();

  const callCountPropRule = propRuleSpy.mock.calls.length;
  const callCountClientRule = clientRuleSpy.mock.calls.length;
  await button.trigger("click");
  // A chip with content "b" should be added
  chip = wrapper.findComponent("[data-test='chip']");
  expect(Object.keys(chip)).not.toHaveLength(0);
  expect(chip.text()).toBe("b");
  // The input value was added as a chip so the input field should be empty again and thus the button disabled again
  expect(button.attributes().disabled).toBe("");
  // Rules have to be called twice since click because first the rules are called directly inside addEntry()
  // of the ChipTextField and the second time when the value is reset to an empty string because Vuetify
  // automatically calls rules when the input is changed.
  expect(propRuleSpy.mock.calls.length).toBeGreaterThan(callCountPropRule + 1);
  expect(clientRuleSpy.mock.calls.length).toBeGreaterThan(
    callCountClientRule + 1,
  );

  await inputField.setValue("b");
  await flushPromises();
  // Show an error and disable the button to add new values because the value is a duplicate
  expect(wrapper.html()).toContain(t("error.onlyUniqueValues"));
  expect(button.attributes().disabled).toBe("");
});
