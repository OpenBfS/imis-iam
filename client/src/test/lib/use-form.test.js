/* Copyright (C) 2024 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY!
 */
import { setActivePinia, createPinia } from "pinia";
import { useApplicationStore } from "@/stores/application";
import { test, expect, expectTypeOf } from "vitest";
import { useForm } from "@/lib/use-form";
import i18n from "@/i18n";

const { handleValidationErrorFromServer } = useForm(i18n);
setActivePinia(createPinia());
const applicationStore = useApplicationStore();
const { t } = i18n.global;

test("Test handleValidationErrorFromServer", async () => {
  const errors = [
    {
      message: "must not be blank",
      messageParameters: ["role"],
    },
    {
      attribute: "email",
      message: "error.valid_email",
      messageParameters: ["email", "\\S+@\\S+\\.\\S+"],
    },
  ];
  await handleValidationErrorFromServer(errors);
  expect(Object.keys(applicationStore.clientAndServerRules)[0]).toBe("role");
  expectTypeOf(applicationStore.clientAndServerRules["role"]).toBeFunction();
  expect(applicationStore.clientAndServerRules["role"][0]()).toBe(
    errors[0].message
  );

  expect(Object.keys(applicationStore.clientAndServerRules)[1]).toBe("email");
  expectTypeOf(applicationStore.clientAndServerRules["email"]).toBeFunction();
  expect(applicationStore.clientAndServerRules["email"][0]()).toBe(
    t("error.valid_email")
  );
});
