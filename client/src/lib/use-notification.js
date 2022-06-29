/* Copyright (C) 2022 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY!
 */

import { ref } from "vue";
import store from "@/store";

export function useNotification() {
  const hasRequestError = ref(false);
  const hasLoadingError = ref(false);

  const resetNotification = () => {
    store.commit("application/setHttpErrorMessage", "");
    hasRequestError.value = false;
    hasLoadingError.value = false;
  };

  return {
    hasRequestError,
    hasLoadingError,
    resetNotification,
  };
}
