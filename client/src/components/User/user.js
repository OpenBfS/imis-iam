/* Copyright (C) 2022 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY!
 */
import i18n from "@/i18n";
import { useApplicationStore } from "@/stores/application.js";
import { useProfileStore } from "@/stores/profile.js";
import { useUserStore } from "@/stores/user.js";

const expUser = {
  attributes: {},
  institutions: [],
  role: null,
  enabled: false,
};

function getExpUser() {
  return structuredClone(expUser);
}

function handleDisplayName(displayName) {
  const { t } = i18n.global;
  if (displayName.startsWith("${profile.attributes.") && displayName.endsWith("}")) {
    return t(`user.${displayName.replace("${profile.attributes.", "").slice(0, -1)}`);
  }
  return displayName;
}

function updateUser(
  user,
  resetNotification,
  isServerValidationError,
  handleValidationErrorFromServer,
  hasRequestError
) {
  const applicationStore = useApplicationStore();
  const profileStore = useProfileStore();
  const userStore = useUserStore();
  if (resetNotification) resetNotification();
  return new Promise((resolve) => {
    userStore
      .updateUser(user)
      .then(() => {
        // Update current user Profile and thus the data in App bar.
        if (profileStore.userData.id === user.id) {
          profileStore.loadProfile();
        }
        applicationStore.setOwnAccount(false);
        applicationStore.setShowManageUserDialog(false);
        applicationStore.searchRequest(["users"]);
        resolve({ status: 200 });
      })
      .catch((error) => {
        isServerValidationError(error)
          ? handleValidationErrorFromServer(error.response.data)
          : (hasRequestError.value = true);
        resolve(error);
      });
  });
}

export { getExpUser, handleDisplayName, updateUser };
