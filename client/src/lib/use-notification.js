import { ref } from "vue";
import store from "@/store";

export function useNotification() {
  const hasRequestError = ref(false);

  const resetNotification = () => {
    store.commit("application/setHttpErrorMessage", "");
    hasRequestError.value = false;
  };

  return {
    hasRequestError,
    resetNotification,
  };
}
