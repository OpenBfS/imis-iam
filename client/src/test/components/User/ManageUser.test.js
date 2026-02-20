/* Copyright (C) 2023 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY!
 */
import { beforeAll, describe, vi } from "vitest";
import { setActivePinia, createPinia } from "pinia";
import { useApplicationStore } from "@/stores/application";
import { useProfileStore } from "@/stores/profile";
import { useUserStore } from "@/stores/user";
import { HTTP } from "@/lib/http";
import { flushPromises, mount } from "@vue/test-utils";
import ManageUser from "@/components/User/ManageUser.vue";
import global from "@/test/components/global";
import { test, expect } from "vitest";
import i18n from "@/i18n";
import {
  networks,
  roles,
  runSharedTests,
  setupSharedTestEnvironment,
} from "@/test/sharedTests";

const { t } = i18n.global;

setActivePinia(createPinia());

// Mock HTTP requests/responses
vi.spyOn(HTTP, "get").mockResolvedValue({});

const positions = ["Leiter", "Vertreter"];

// Test data
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
const ownUser = {
  id: "2",
  role: "chief_editor",
  network: networks[0],
  attributes: {
    username: ["two"],
    firstName: [firstName],
    operationModeChangePhoneNumbers: ["+4912345678"],
  },
};
const userProfileMetadata = {
  attributes: [
    {
      name: "firstName",
      displayName: "firstname",
    },
    {
      name: "lastName",
      displayName: "lastname",
    },
    {
      name: "position",
      displayName: "position",
      validations: {
        options: {
          options: positions,
        },
      },
    },
    {
      name: "operationModeChangePhoneNumbers",
      displayName: "${profile.attributes.operationModeChangePhoneNumbers}",
      permissions: {
        view: ["admin", "user"],
        edit: ["admin", "user"],
      },
      annotations: {
        inputType: "html5-tel",
      },
      validations: {
        pattern: {
          pattern: "^\\+[1-9][0-9]{7,16}$",
          "error-message": "error.validPhone",
        },
      },
      multivalued: true,
    },
  ],
};

let wrapper;

const setupTestEnvironment = async (roleOfLoggedInUser) => {
  setupSharedTestEnvironment(user, "user", roleOfLoggedInUser);
  const profileStore = useProfileStore();
  profileStore.setUserData(ownUser);
  const userStore = useUserStore();
  userStore.setRoles(roles);
  profileStore.setUserProfileMetadata(userProfileMetadata);
  await flushPromises();
  wrapper = mount(ManageUser, {
    global: global,
    props: {
      index: 0,
    },
  });
};

describe("Test ManageUser", () => {
  beforeAll(async () => {
    await setupTestEnvironment("chief_editor");
  });

  async function testFieldInput(wrapper, name) {
    const applicationStore = useApplicationStore();
    const input = wrapper.get(`input[name='${name}']`);
    const newValue = "test";
    await input.setValue(newValue);
    expect(input.element.value).toBe(newValue);
    expect(
      applicationStore.managedItems[0].item.attributes[name]
    ).toStrictEqual([newValue]);
  }

  test("First name is displayed in respective input", async () => {
    expect(wrapper.get("input[name='firstName']").element.value).toBe(
      firstName
    );
  });

  test("Missing attribute is rendered as empty string", () => {
    expect(wrapper.get("input[name='lastName']").element.value).toBe("");
  });

  test("Input changes existing attribute", () => {
    const fieldName = "firstName";
    expect(user.attributes[fieldName]).toBeDefined();
    testFieldInput(wrapper, fieldName);
  });

  test("Input adds non-existing attribute", () => {
    const fieldName = "lastName";
    expect(user.attributes[fieldName]).toBeUndefined();
    testFieldInput(wrapper, "lastName");
  });

  test("Re-setting fields is removing validation errors", async () => {
    const errors = [
      {
        message: "must not be blank",
        messageParameters: ["role"],
      },
    ];
    wrapper.vm.handleValidationErrorFromServer(errors);
    const selectContainer =
      wrapper.get("input[name='role']").element.parentElement.parentElement
        .parentElement.parentElement.parentElement.parentElement;
    expect(selectContainer.textContent).not.toContain(errors[0].message);
    await wrapper.vm.form.validate();
    await flushPromises();

    // The value of the select element cannot be found by reading the value of the
    // input element (which is "").
    // It can be found in a <div> which is a sibling of the input.
    expect(
      wrapper.get("input[name='role']").element.parentElement.textContent,
    ).toContain(t(roles.filter((r) => r.name === user.role)[0].description));
    expect(selectContainer.textContent).toContain(errors[0].message);
    const editedUser = structuredClone(user);
    editedUser.role = null;
    wrapper.vm.resetForm(() => {});
    await flushPromises();
    expect(selectContainer.textContent).toContain(errors[0].message);
  });

  test("Reset button is en- and disabled correctly", async () => {
    await setupTestEnvironment("chief_editor");
    runSharedTests(wrapper);
  });

  // Reason for this test is to prevent what had to be fixed in 663e4dcaa03da9cafdeaca76f0554432ae923f51.
  test("Role should be translated even if user of application is no chief editor", async () => {
    await setupTestEnvironment("editor");
    expect(wrapper.text()).toContain(t("roleIamChiefEditor"));
  });
});
