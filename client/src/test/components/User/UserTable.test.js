/* Copyright (C) 2023 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY!
 */
import { vi } from "vitest";
import store from "@/store";
import { mount } from "@vue/test-utils";
import UserTable from "@/components/User/UserTable.vue";
import global from "@/test/components/global";
import { test, expect } from "vitest";

// Mock HTTP request/response
vi.spyOn(store, "dispatch").mockResolvedValue({});

// Test data
const users = [
  {
    id: "1",
    attributes: {
      username: ["one"],
      firstName: ["One"],
      lastName: ["Two"],
    },
  },
  {
    id: "2",
    attributes: {
      username: ["two"],
      lastName: ["Three"],
    },
  },
];

// Init component
const wrapper = mount(UserTable, {
  props: {
    users: users,
  },
  global: global,
});

test("Username is displayed in first column", () => {
  wrapper.findAll("tr").forEach((row, i) => {
    expect(row.get("td").text()).toBe(users[i].attributes.username[0]);
  });
});

test("Missing attribute is rendered as empty string", () => {
  expect(wrapper.findAll("tr")[1].findAll("td")[1].text()).toBe("");
});
