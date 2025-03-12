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
import { mount } from "@vue/test-utils";
import UserTable from "@/components/User/UserTable.vue";
import global from "@/test/components/global";
import { test, expect } from "vitest";
import { HTTP } from "@/lib/http";

setActivePinia(createPinia());

const applicationStore = useApplicationStore();
const profileStore = useProfileStore();

// Mock HTTP request/response
vi.spyOn(HTTP, "get").mockResolvedValue({});
vi.spyOn(applicationStore, "searchRequest").mockResolvedValue({});

profileStore.setUserProfileMetadata({
  attributes: [
    {
      name: "username",
      displayName: "${username}",
      annotations: {
        defaultField: "true",
      },
    },
    {
      name: "firstName",
      displayName: "${firstName}",
      annotations: {
        defaultField: "true",
      },
    },
    {
      name: "lastName",
      displayName: "${lastName}",
      annotations: {
        defaultField: "true",
      },
    },
  ],
});

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
    headers: [
      { name: "username", default: true },
      { name: "firstName", default: true },
      { name: "lastName", default: true },
    ],
    users: users,
  },
  global: global,
});

test("Username is displayed in first column", async () => {
  wrapper.findAll("tbody tr").forEach((row, i) => {
    expect(row.findAll("td")[1].text()).toBe(users[i].attributes.username[0]);
  });
});

test("Missing attribute is rendered as empty string", () => {
  expect(wrapper.findAll("tbody tr")[1].findAll("td")[2].text()).toBe("");
});
