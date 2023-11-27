/* Copyright (C) 2023 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY!
 */
import { vi } from "vitest";
import store from "@/store";
import { HTTP } from "@/lib/http";
import { mount } from "@vue/test-utils";
import ManageUser from "@/components/User/ManageUser.vue";
import global from "@/test/components/global";
import { test, expect } from "vitest";

// Mock HTTP requests/responses
vi.spyOn(store, "dispatch").mockResolvedValue({});
vi.spyOn(HTTP, "get").mockResolvedValue({});

// Test data
const firstName = "One";
const user = {
  id: "1",
  username: "one",
  firstName: firstName,
};
store.state.application.managedItem = user;

const wrapper = mount(ManageUser, {
  global: global,
});

test("First name is displayed in respective input", () => {
  expect(wrapper.get("input[name='firstname']").element.value).toBe(firstName);
});

test("Missing attribute is rendered as empty string", () => {
  expect(wrapper.get("input[name='lastname']").element.value).toBe("");
});

test("Input changes existing attribute", () => {
  const fieldName = "firstName";
  expect(user[fieldName]).toBeDefined();
  testFieldInput(fieldName);
});

test("Input adds non-existing attribute", () => {
  const fieldName = "lastName";
  expect(user[fieldName]).toBeUndefined();
  testFieldInput("lastName");
});

async function testFieldInput(name) {
  const input = wrapper.get(`input[name='${name.toLowerCase()}']`);
  const newValue = "test";
  await input.setValue(newValue);
  expect(input.element.value).toBe(newValue);
  expect(user[name]).toBe(newValue);
}
