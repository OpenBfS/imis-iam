/* Copyright (C) 2023 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY!
 */
import { vi } from "vitest";
import { setActivePinia, createPinia } from "pinia";
import { useApplicationStore } from "@/stores/application";
import { useProfileStore } from "@/stores/profile";
import { HTTP } from "@/lib/http";
import { mount } from "@vue/test-utils";
import ManageUser from "@/components/User/ManageUser.vue";
import global from "@/test/components/global";
import { test, expect } from "vitest";

setActivePinia(createPinia());

const applicationStore = useApplicationStore();
const profileStore = useProfileStore();

// Mock HTTP requests/responses
vi.spyOn(HTTP, "get").mockResolvedValue({});

// Test data
const firstName = "One";
const user = {
  id: "1",
  attributes: {
    username: ["one"],
    firstName: [firstName],
  },
};
const userProfileMetadata = {
  attributes: [
    {
      name: "position",
      displayName: "position",
      validations: {
        options: {
          options: [],
        },
      },
    },
    {
      name: "firstName",
      displayName: "firstname",
    },
    {
      name: "lastName",
      displayName: "lastname",
    },
  ],
};
applicationStore.managedItem = user;
profileStore.setUserData({ role: "chief_editor" });
profileStore.setUserProfileMetadata(userProfileMetadata);

const wrapper = mount(ManageUser, {
  global: global,
});

test("First name is displayed in respective input", () => {
  expect(wrapper.get("input[name='firstName']").element.value).toBe(firstName);
});

test("Missing attribute is rendered as empty string", () => {
  expect(wrapper.get("input[name='lastName']").element.value).toBe("");
});

test("Input changes existing attribute", () => {
  const fieldName = "firstName";
  expect(user.attributes[fieldName]).toBeDefined();
  testFieldInput(fieldName);
});

test("Input adds non-existing attribute", () => {
  const fieldName = "lastName";
  expect(user.attributes[fieldName]).toBeUndefined();
  testFieldInput("lastName");
});

async function testFieldInput(name) {
  const input = wrapper.get(`input[name='${name}']`);
  const newValue = "test";
  await input.setValue(newValue);
  expect(input.element.value).toBe(newValue);
  expect(user.attributes[name]).toStrictEqual([newValue]);
}
