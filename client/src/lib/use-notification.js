/* Copyright (C) 2022 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY!
 */
import { ref } from "vue";
import { useApplicationStore } from "@/stores/application.js";

export function useNotification() {
  const hasRequestError = ref(false);
  const hasLoadingError = ref(false);

  const resetNotification = () => {
    const applicationStore = useApplicationStore();
    applicationStore.setHttpErrorMessage("");
    hasRequestError.value = false;
    hasLoadingError.value = false;
  };

  return {
    hasRequestError,
    hasLoadingError,
    resetNotification,
  };
}
