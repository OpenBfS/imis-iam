/* Copyright (C) 2025 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY!
 */
import { beforeAll, describe, expect, test, vi } from "vitest";
import { setActivePinia, createPinia } from "pinia";
import { useProfileStore } from "@/stores/profile";
import { useUserStore } from "@/stores/user";
import { HTTP } from "@/lib/http";
import { flushPromises, mount } from "@vue/test-utils";
import EditTags from "@/components/UI/EditTags.vue";
import global from "@/test/components/global";
import { updateUser } from "@/components/User/user.js";
import i18n from "@/i18n";

const { t } = i18n.global;

// Mock the updateUser function
vi.mock("@/components/User/user.js", () => ({
  updateUser: vi.fn(),
}));

setActivePinia(createPinia());

// Mock HTTP requests/responses
vi.spyOn(HTTP, "get").mockResolvedValue({});

const userTags = ["tag1", "tag2", "tag3"];

// Test data - user without any tags
const userWithoutTags = {
  id: "1",
  role: "user",
  attributes: {
    username: ["testuser"],
    firstName: ["Test"],
    lastName: ["User"],
    // No tags attribute
  },
};

const userProfileMetadata = {
  attributes: [
    {
      name: "username",
      displayName: "${username}",
    },
    {
      name: "firstName",
      displayName: "${firstName}",
    },
    {
      name: "lastName",
      displayName: "${lastName}",
    },
    {
      name: "tags",
      displayName: "Tags",
      required: {
        roles: ["user"],
      },
      validations: {
        options: {
          options: userTags,
        },
      },
    },
  ],
};

let wrapper;

const setupTestEnvironment = async () => {
  const profileStore = useProfileStore();
  const userStore = useUserStore();

  profileStore.setUserProfileMetadata(userProfileMetadata);
  userStore.setFoundUsers([userWithoutTags]);
  userStore.selectedUsers = [userWithoutTags.id];

  await flushPromises();

  // Create a container for the teleported dialog content
  const el = document.createElement("div");
  el.id = "app";
  document.body.appendChild(el);

  wrapper = mount(EditTags, {
    props: {
      isActive: true,
      type: "users",
      close: vi.fn(),
    },
    global: global,
    attachTo: el,
  });

  await flushPromises();

  // Manually set the userTagsAttribute that would be loaded by loadUserTags
  // This simulates what happens when onUpdated runs and loads the user tags
  const tagsAttribute = userProfileMetadata.attributes.find(
    attr => attr.name === "tags"
  );
  wrapper.vm.userTagsAttribute = tagsAttribute;
  wrapper.vm.userTags = tagsAttribute.validations.options.options;

  await flushPromises();
};

describe("Test EditTags - Add tag to user without tags", () => {
  beforeAll(async () => {
    await setupTestEnvironment();
  });

  test("User initially has no tags", () => {
    const userStore = useUserStore();
    const user = userStore.foundUsers.find(u => u.id === userWithoutTags.id);
    expect(user.attributes.tags).toBeUndefined();
  });

  test("Add a tag to user without tags", async () => {
    // Mock updateUser to return success
    updateUser.mockReturnValue({ response: 200 });

    // Wait for the dialog to be fully rendered
    await wrapper.vm.$nextTick();
    await flushPromises();

    // Select a tag to add
    const tagToAdd = userTags[0];

    // Set the selectedTags directly via the component's exposed values
    wrapper.vm.selectedTags = [tagToAdd];
    await flushPromises();

    // Click the "Add" button - need to find in document.body as dialog is teleported
    const allButtons = document.body.querySelectorAll("button");
    const addButtonElement = Array.from(allButtons).find(
      btn => btn.textContent?.trim() === t("button.add")
    );
    expect(addButtonElement).toBeDefined();
    addButtonElement.click();
    await flushPromises();

    // Verify that updateUser was called
    expect(updateUser).toHaveBeenCalled();

    // Verify the user object passed to updateUser has the tag
    const callArgs = updateUser.mock.calls[0];
    const updatedUser = callArgs[0];
    expect(updatedUser.attributes.tags).toBeDefined();
    expect(updatedUser.attributes.tags).toContain(tagToAdd);
    expect(updatedUser.attributes.tags.length).toBe(1);
  });
});
