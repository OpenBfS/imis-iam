/* Copyright (C) 2022 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY!
 */
import i18n from "@/i18n";
import { trimSpacesInObject } from "@/lib/form-helper.js";
import { useApplicationStore } from "@/stores/application.js";
import { useProfileStore } from "@/stores/profile.js";
import { useUserStore } from "@/stores/user.js";
import { toRaw } from "vue";

const expUser = {
  attributes: {},
  institutions: [],
  role: null,
  network: null,
  enabled: false,
  hiddenInAddressbook: false,
  retired: false,
};

function getExpUser() {
  const profileStore = useProfileStore();
  const userStore = useUserStore();
  const user = structuredClone(expUser);
  user.network = toRaw(profileStore.userData.network);
  user.role = toRaw(userStore.roles)?.find(
    (role) => role.name === "user"
  )?.name;
  return user;
}

function handleDisplayName(displayName) {
  const { t } = i18n.global;
  if (
    displayName.startsWith("${profile.attributes.") &&
    displayName.endsWith("}")
  ) {
    return t(
      `user.${displayName.replace("${profile.attributes.", "").slice(0, -1)}`
    );
  }
  return displayName;
}

function updateUser(
  user,
  resetNotification,
  isServerValidationError,
  handleValidationErrorFromServer,
  hasRequestError,
  managedItemIndex
) {
  const profileStore = useProfileStore();
  const userStore = useUserStore();
  if (resetNotification) resetNotification();
  return new Promise((resolve) => {
    userStore
      .updateUser(trimSpacesInObject(user))
      .then(() => {
        // Update current user Profile and thus the data in App bar.
        if (profileStore.userData.id === user.id) {
          profileStore.loadProfile();
        }
        finishUserDialog(user, managedItemIndex);
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

function finishUserDialog(newUser, managedItemIndex) {
  const applicationStore = useApplicationStore();
  applicationStore.searchRequest(["users"]);
  applicationStore.loadNetworksIfNotContains(newUser.network);
  if (managedItemIndex !== undefined)
    applicationStore.removeManagedItem(managedItemIndex);
}

export { finishUserDialog, getExpUser, handleDisplayName, updateUser };
