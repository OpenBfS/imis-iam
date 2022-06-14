import store from "@/store";
import axios from "axios";

const HTTP = axios.create({
  baseURL: "/backend/realms/imis3",
});

HTTP.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response) {
      if (error.response.status === 404) {
        store.commit(
          "application/setHttpErrorMessage",
          `Page '${error.response.config.url}' not found`
        );
      } else {
        store.commit(
          "application/setHttpErrorMessage",
          error.response.statusText
        );
      }
      // Handle other type of errors.
    } else if (error.request) {
      store.commit("application/setHttpErrorMessage", error.request);
    } else {
      store.commit("application/setHttpErrorMessage", "Error" + error.message);
    }
    return Promise.reject(error);
  }
);
const ShibHTTP = axios.create({
  baseURL: "/",
});

export { HTTP, ShibHTTP };
