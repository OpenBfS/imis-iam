/* Copyright (C) 2024 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY!
 */
import { setActivePinia, createPinia } from "pinia";
import { test, expect, expectTypeOf } from "vitest";
import { areArraysDifferent, trimSpacesInObject } from "@/lib/form-helper.js";
import { useForm } from "@/lib/use-form";
import i18n from "@/i18n";
import { getExpInstitution } from "@/components/Institution/institution";
import { defineComponent } from "vue";
import { mount } from "@vue/test-utils";
import global from "@/test/components/global";

setActivePinia(createPinia());
const { t } = i18n.global;

test("Test handleValidationErrorFromServer", async () => {
  const TestComponent = defineComponent({
    setup() {
      return {
        ...useForm(),
      };
    },
  });

  const wrapper = mount(TestComponent, {
    global: global,
  });

  const errors = [
    {
      message: "must not be blank",
      messageParameters: ["role"],
    },
    {
      attribute: "email",
      message: "error.validEmail",
      messageParameters: ["email", "\\S+@\\S+\\.\\S+"],
    },
    {
      attribute: "email",
      message: "error-valid-email",
      messageParameters: ["email", "\\S+@\\S+\\.\\S+"],
    },
  ];
  await wrapper.vm.handleValidationErrorFromServer(errors);
  expect(Object.keys(wrapper.vm.clientAndServerRules)[0]).toBe("role");
  expectTypeOf(wrapper.vm.clientAndServerRules["role"]).toBeFunction();
  expect(wrapper.vm.clientAndServerRules["role"][0]()).toBe(errors[0].message);

  expect(Object.keys(wrapper.vm.clientAndServerRules)[1]).toBe("email");
  expectTypeOf(wrapper.vm.clientAndServerRules["email"]).toBeFunction();
  expect(wrapper.vm.clientAndServerRules["email"][0]()).toBe(
    t("error.validEmail")
  );

  expect(wrapper.vm.clientAndServerRules["email"][0]()).toBe(
    t("error.validEmail")
  );
});

test("Test function trimSpacesInObject", () => {
  const inst = getExpInstitution();
  inst["serviceBuildingPostalCode"] = " 12345";
  inst["serviceBuildingStreet"] = "Example Street 1 ";
  trimSpacesInObject(inst);
  expect(inst["serviceBuildingPostalCode"]).toBe("12345");
  expect(inst["serviceBuildingStreet"]).toBe("Example Street 1");
});

test("Test function areArraysDifferent", () => {
  expect(areArraysDifferent(["a", "b", "c"], ["a", "c", "b"])).toBeTruthy();
  expect(areArraysDifferent(["a", "b", "c"], ["a", "b", "d"])).toBeTruthy();
  expect(areArraysDifferent([1, 2, 3], [1, 3, 2])).toBeTruthy();
  expect(areArraysDifferent([1, 2, 3], [1, 2])).toBeTruthy();
  expect(
    areArraysDifferent(["a", "b", "c"], ["a", "c", "b"], false)
  ).toBeFalsy();
  expect(
    areArraysDifferent(["a", "b", "c"], ["a", "b", "d"], false)
  ).toBeTruthy();
  expect(areArraysDifferent([1, 2, 3], [1, 3, 2], false)).toBeFalsy();
  expect(areArraysDifferent([1, 2, 3], [1, 2], false)).toBeTruthy();
});
