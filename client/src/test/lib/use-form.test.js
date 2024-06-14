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

test("Test handleValidationErrorFromServer", async () => {
  const attribute = "role";
  const errors = [
    {
      message: "must not be blank",
      messageParameters: [attribute],
    },
  ];
  await handleValidationErrorFromServer(errors);
  expect(Object.keys(applicationStore.clientAndServerRules)[0]).toBe(attribute);
  expectTypeOf(applicationStore.clientAndServerRules[attribute]).toBeFunction();
  expect(applicationStore.clientAndServerRules[attribute][0]()).toBe(
    errors[0].message
  );
});
