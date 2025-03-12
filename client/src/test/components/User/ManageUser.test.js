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
import { useUserStore } from "@/stores/user";
import { HTTP } from "@/lib/http";
import { flushPromises, mount } from "@vue/test-utils";
import ManageUser from "@/components/User/ManageUser.vue";
import global from "@/test/components/global";
import { test, expect } from "vitest";
import { useForm } from "@/lib/use-form";
import i18n from "@/i18n";

const { handleValidationErrorFromServer } = useForm(i18n);

setActivePinia(createPinia());

const applicationStore = useApplicationStore();
const profileStore = useProfileStore();
const userStore = useUserStore();

// Mock HTTP requests/responses
vi.spyOn(HTTP, "get").mockResolvedValue({});

// Test data
const firstName = "One";
const user = {
  id: "1",
  role: "chief_editor",
  attributes: {
    username: ["one"],
    firstName: [firstName],
  },
};
const editedUser = structuredClone(user);
editedUser.role = null;
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
const roles = [
  { name: "chief_editor", description: "roleIamChiefEditor" },
  { name: "editor", description: "roleIamEditor" },
  { name: "user", description: "roleIamUser" },
];
applicationStore.managedItem = user;
userStore.setRoles(roles);
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

test("Re-setting fields is removing validation errors", async () => {
  const errors = [
    {
      message: "must not be blank",
      messageParameters: ["role"],
    },
  ];
  handleValidationErrorFromServer(errors);
  const selectContainer =
    wrapper.get("input[name='role']").element.parentElement.parentElement
      .parentElement.parentElement.parentElement.parentElement;
  expect(selectContainer.textContent).not.toContain(errors[0].message);
  await wrapper.vm.form.validate();

  // The value of the select element cannot be found by reading the value of the
  // input element (which is "").
  // It can be found in a <div> which is a sibling of the input.
  expect(
    wrapper.get("input[name='role']").element.parentElement.children[0]
      .textContent
  ).toBe(roles.filter((r) => r.name === user.role)[0].title);
  expect(selectContainer.textContent).toContain(errors[0].message);
  wrapper.vm.resetForm(user, editedUser);
  await flushPromises();
  expect(selectContainer.textContent).not.toContain(errors[0].message);
});
