/* Copyright (C) 2024 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY!
 */
import { setActivePinia, createPinia } from "pinia";
import { useApplicationStore } from "@/stores/application";
import { handleError } from "@/lib/http";
import { test, expect } from "vitest";

setActivePinia(createPinia());
const applicationStore = useApplicationStore();

test("Test error handling", () => {
  const request = "/get/something";
  const message = "Something went wrong";
  let error = {
    response: {
      data: [
        {
          message: "error.valid_email",
        },
      ],
      statusText: "Bad Request",
      status: 400,
    },
    request: request,
    message: message,
  };
  handleError(error);
  expect(applicationStore.httpErrorMessage).toBe("");

  error.response.data[0].message = undefined;
  handleError(error);
  expect(applicationStore.httpErrorMessage).toBe(
    JSON.stringify(error.response.data)
  );

  error.response.data = [1, 2];
  handleError(error);
  expect(applicationStore.httpErrorMessage).toBe("[1,2]");

  error.response.data = undefined;
  error.response.statusText = "Internal Server Error";
  handleError(error);
  expect(applicationStore.httpErrorMessage).toBe("Internal Server Error");

  error.response = undefined;
  handleError(error);
  expect(applicationStore.httpErrorMessage).toBe(request);

  error.request = undefined;
  handleError(error);
  expect(applicationStore.httpErrorMessage).toBe("Error" + message);
});
