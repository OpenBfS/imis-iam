/* Copyright (C) 2025 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY!
 */
import { describe, vi } from "vitest";
import { useApplicationStore } from "@/stores/application";
import { HTTP } from "@/lib/http";
import { flushPromises, mount } from "@vue/test-utils";
import ManageInstitution from "@/components/Institution/ManageInstitution.vue";
import global from "@/test/components/global";
import { test } from "vitest";
import {
  networks,
  runSharedTests,
  setupSharedTestEnvironment,
} from "@/test/sharedTests";

// Mock HTTP requests/responses
vi.spyOn(HTTP, "get").mockResolvedValue({});

// Test data
const institution = {
  id: 1,
  name: "Institution 1",
  serviceBuildingStreet: "Examplestreet 1",
  serviceBuildingPostalCode: "12345",
  serviceBuildingLocation: "ExampleLocation-1",
  serviceBuildingState: "berlin",
  phoneNumbers: ["+49123456789"],
  network: networks[0],
};

describe("Test ManageInstitution", () => {
  test("Reset button is en- and disabled correctly", async () => {
    setupSharedTestEnvironment(institution, "institution", "chief_editor");
    const applicationStore = useApplicationStore();
    applicationStore.setNetworks(networks);
    const wrapper = mount(ManageInstitution, {
      global: global,
      props: {
        index: 0,
      },
    });
    await flushPromises();
    runSharedTests(wrapper);
  });
});
