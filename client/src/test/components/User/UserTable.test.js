/* Copyright (C) 2023 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY!
 */
import { createVuetify } from "vuetify";
import * as components from "vuetify/components";
import * as directives from "vuetify/directives";
import { createI18n } from "vue-i18n";
import de from "@/locales/de";
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

const i18n = createI18n({
  legacy: false,
  globalInjection: true,
  locale: "de",
  messages: {
    de: de,
  },
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
    firstName: "Two",
    lastName: "Three",
  },
];

test("Username is displayed in first column", () => {
  const wrapper = mount(UserTable, {
    props: {
      users: users,
    },
    global: {
      plugins: [vuetify, store, i18n],
    },
  });
  wrapper.findAll("tr").forEach((row, i) => {
    expect(row.find("td").text()).toBe(users[i].username);
  });
});
