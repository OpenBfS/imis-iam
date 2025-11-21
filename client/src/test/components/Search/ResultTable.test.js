/* Copyright (C) 2025 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY!
 */
import { expect, test, vi } from "vitest";
import { page, userEvent } from "vitest/browser";
import { setActivePinia, createPinia } from "pinia";
import { useApplicationStore } from "@/stores/application";
import { useProfileStore } from "@/stores/profile";
import { useUserStore } from "@/stores/user";
import ResultTable from "@/components/Search/ResultTable.vue";
import global from "@/test/components/global";
import { HTTP } from "@/lib/http";
import i18n from "@/i18n";
import { networks, roles } from "@/test/sharedTests";
import { useInstitutionStore } from "@/stores/institution";
import {
  createHeaders,
  initSelectedColumns,
} from "@/components/Search/searchTable";

setActivePinia(createPinia());

const { t } = i18n.global;
const applicationStore = useApplicationStore();
const profileStore = useProfileStore();
const userStore = useUserStore();
userStore.setRoles(roles);

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
    {
      name: "tags",
      displayName: "${profile.attributes.tags}",
      validations: {
        options: {
          options: [
            "AK UN",
            "IBG",
            "IBG nachrichtlich",
            "IMIS-Information",
            "Kleine Lagen",
            "Landesadministration IMIS-DokPool",
            "Messanleitung",
            "REI-Nutzer",
            "REI/REA informell",
            "Schulungsangebote",
          ],
        },
      },
      annotations: {
        inputType: "multiselect",
      },
      permissions: {
        view: ["admin", "user"],
        edit: ["admin", "user"],
      },
      multivalued: true,
    },
  ],
});

// Test data
const users = [
  {
    id: "1",
    role: "user",
    attributes: {
      username: ["one"],
      firstName: ["One"],
      lastName: ["Two"],
    },
  },
  {
    id: "2",
    role: "editor",
    attributes: {
      username: ["two"],
      lastName: ["Three"],
    },
  },
];
userStore.foundUsers = users;

const columns = profileStore.attributes
  ? [
      { name: "username", default: true },
      { name: "firstName", default: true },
      { name: "lastName", default: true },
      { name: "tags", default: true },
      { name: "institutions", default: true },
      { name: "role", default: true },
    ]
  : [];
userStore.tableHeaders = createHeaders(columns, "users");
initSelectedColumns("users");

test("Navigating through filters", async () => {
  const institutionStore = useInstitutionStore();

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
  const institution2 = {
    id: 2,
    name: "Institution 2",
    serviceBuildingStreet: "Examplestreet 2",
    serviceBuildingPostalCode: "98765",
    serviceBuildingLocation: "ExampleLocation-1",
    serviceBuildingState: "berlin",
    phoneNumbers: ["+49123456789", "+49987654321"],
    network: networks[1],
  };
  institutionStore.institutionTags = [{ name: "ExampleTag" }];
  institutionStore.addInstitution(institution);
  institutionStore.addInstitution(institution2);
  const screen = page.render(ResultTable, {
    baseElement: document.body,
    global: global,
  });
  const columns = [
    { name: "username", default: true },
    { name: "firstName", default: true },
    { name: "lastName", default: true },
    { name: "tags", default: true },
    { name: "institutions", default: true },
    { name: "role", default: true },
  ];
  userStore.tableHeaders = createHeaders(columns, "users");
  initSelectedColumns("users");

  const tag = "AK UN";

  const lastNameField = screen.getByRole("textbox", {
    name: t("user.lastName"),
  });
  await expect.element(lastNameField).toBeInTheDocument();
  await lastNameField.click();
  await expect(lastNameField).toHaveFocus();

  const institutionSelect = screen.getByRole("combobox", {
    name: t("institutions"),
  });
  await expect.element(institutionSelect).toBeInTheDocument();

  const tagSelect = screen.getByRole("combobox", {
    name: t("user.tags"),
  });
  await expect.element(tagSelect).toBeInTheDocument();

  // This should have no effect since we are in a simple text field (lastName)
  await userEvent.keyboard("[ArrowDown]");
  await userEvent.keyboard("[ArrowDown]");

  await expect
    .poll(() => page.elementLocator(document.body).getByText(tag).query())
    .not.toBeInTheDocument();

  await userEvent.keyboard("[Tab]");
  await userEvent.keyboard("[ArrowDown]");

  // The list of options is attached to the DOM after opening the select so we cannot use
  // the same locators as above.
  await expect
    .element(page.elementLocator(document.body))
    .toContainElement(
      page.elementLocator(document.body).getByText(tag).element()
    );

  await userEvent.keyboard("[Tab]");

  await expect
    .poll(() => page.elementLocator(document.body).getByText(tag).query(), {
      timeout: 2000,
    })
    .not.toBeInViewport();

  await userEvent.keyboard("{ArrowDown}");

  await expect
    .element(page.elementLocator(document.body))
    .toContainElement(
      page.elementLocator(document.body).getByText("Institution 1").element()
    );
});
