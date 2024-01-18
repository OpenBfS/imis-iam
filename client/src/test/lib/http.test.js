/* Copyright (C) 2024 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY!
 */
import store from "@/store";
import { handleError } from "@/lib/http";
import { test, expect } from "vitest";

test("Test error handling", () => {
  const request = "/get/something";
  const message = "Something went wrong";
  let error = {
    response: {
      data: [1, 2],
      statusText: "Internal Server Error",
    },
    request: request,
    message: message
  };
  handleError(error);
  expect(store.state.application.httpErrorMessage).toMatchObject([1, 2]);
  error.response.data = undefined;
  handleError(error);
  expect(store.state.application.httpErrorMessage).toBe(
    "Internal Server Error"
  );
  error.response = undefined;
  handleError(error);
  expect(store.state.application.httpErrorMessage).toBe(request);
  error.request = undefined;
  handleError(error)
  expect(store.state.application.httpErrorMessage).toBe("Error" + message);
});
