/* Copyright (C) 2024 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY!
 */
import { vi } from "vitest";
import { HTTP } from "@/lib/http";
import { mount } from "@vue/test-utils";
import Archive from "@/components/Mailing/Archive.vue";
import global from "@/test/components/global";
import { describe, test, expect } from "vitest";
import router from "../mockRouter.js";
import { unref } from "vue";
import { flushPromises } from "@vue/test-utils";
import { setActivePinia, createPinia } from "pinia";

setActivePinia(createPinia());

describe("Testing Archive", async () => {
  const currentYear = new Date().getFullYear();
  const mails = [
    {
      archived: true,
      expiryDate: 1706655600000,
      id: 1,
      recipient: 1,
      sendDate: 1706439965366,
      sender: "Example Chefredakteur <example@chefredakteur.example>",
      subject: "subject",
      text: "body",
      type: 1,
    },
  ];
  let query = "";
  vi.spyOn(HTTP, "get").mockImplementation((queryParameters) => {
    query = queryParameters;
    return Promise.resolve({
      data: mails,
    });
  });
  router.push(`/archive/${currentYear}`);
  await router.isReady();
  const wrapper = mount(Archive, {
    global: {
      ...global,
      mocks: {
        $d: function () {
          return "";
        },
      },
    },
  });

  // Resolve the promise of the mocked funktion immediately.
  await flushPromises();

  expect(unref(router.currentRoute).params.year).toBe(`${currentYear}`);
  test("Display email correctly", async () => {
    expect(wrapper.html()).toContain("Example Chefredakteur");
  });
  test("Create correct filter query", async () => {
    expect(query).toBe(
      `mail?archived=true&start=${currentYear}-01-01T00:00:00.000Z&end=${currentYear}-12-31T23:59:59.999Z`
    );
  });
});
