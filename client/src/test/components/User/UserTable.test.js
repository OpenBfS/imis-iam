/* Copyright (C) 2023 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY!
 */
import { createVuetify } from "vuetify";
import * as components from "vuetify/components";
import * as directives from "vuetify/directives";
import i18n from "@/i18n";
import { vi } from "vitest";
import store from "@/store";
import { mount } from "@vue/test-utils";
import UserTable from "@/components/User/UserTable.vue";
import { test, expect } from "vitest";

// Init plugins
const vuetify = createVuetify({
  components,
  directives,
});

// Mock HTTP request/response
vi.spyOn(store, "dispatch").mockResolvedValue({});

// Test data
const users = [
  {
    id: "1",
    username: "one",
    firstName: "One",
    lastName: "Two",
  },
  {
    id: "2",
    username: "two",
    lastName: "Three",
  },
];

// Init component
const wrapper = mount(UserTable, {
  props: {
    users: users,
  },
  global: {
    plugins: [vuetify, store, i18n],
  },
});

test("Username is displayed in first column", () => {
  wrapper.findAll("tr").forEach((row, i) => {
    expect(row.find("td").text()).toBe(users[i].username);
  });
});

test("Missing attribute is rendered as empty string", () => {
  expect(wrapper.findAll("tr")[1].findAll("td")[1].text()).toBe("");
});
