/* Copyright (C) 2022 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY!
 */
import { defineStore } from "pinia";
import { useInstitutionStore } from "@/stores/institution.js";
import { useUserStore } from "@/stores/user.js";
import { HTTP } from "@/lib/http";
import { useProfileStore } from "./profile";

export const PROCESS_TYPE = {
  ADD: "add",
  COPY: "copy",
  EDIT: "edit",
  // When the user opens the edit dialog via the profile button in the app bar at the top
  // the endpoint /user/profile has to be used to save additional fields.
  EDIT_PROFILE: "edit_profile",
  SHOW: "show",
};

export const useApplicationStore = defineStore("application", {
  state: () => ({
    httpErrorMessage: "",
    showExportDialog: false,
    listToExport: "",
    isAllowedToManageUser: false,
    // managedItems: [{item, originalItem}]
    managedItems: [],
    managedItem: {},
    // Original unedited item so we can reset it when necessary
    savedItem: {},
    showManageEventDialog: false,
    showManageUserDialog: false,
    showSessionExpiredDialog: false,
    showInfoDialog: false,
    processType: "",
    footerLinks: {
      contact: import.meta.env.VITE_FOOTER_CONTACT,
      help: import.meta.env.VITE_FOOTER_HELP,
      accessibility: import.meta.env.VITE_FOOTER_ACCESSIBILITY,
      privacy: import.meta.env.VITE_FOOTER_PRIVACY,
    },
    reportMail: import.meta.env.VITE_REPORT_MAIL,
    ownAccount: false,
    isLoading: false,
    networks: [],
    abortControllers: {},
    touchedEdge: undefined,
  }),
  actions: {
    setOwnAccount(data) {
      this.ownAccount = data;
    },
    setShowManageEventDialog(data) {
      this.showManageEventDialog = data;
    },
    setShowSessionExpiredDialog(data) {
      this.showSessionExpiredDialog = data;
    },
    setSavedItem(data) {
      this.savedItem = data;
    },
    setProcessType(data) {
      this.processType = data;
    },
    addManagedItem(item) {
      const index = this.managedItems.findIndex(
        (i) => i?.item.id === item.item.id
      );
      if (item.item.id && index !== -1) {
        // Do not open the same managed item in multiple dialogs
        this.raiseManagedItem(index);
      } else {
        // Leave one zIndex out so we can show a window placeholder at this index
        item.zIndex = this.managedItems.length + 1;
        this.managedItems.push(item);
      }
    },
    removeManagedItem(index) {
      this.managedItems[index] = undefined;
      if (this.managedItems.slice(index).every((i) => i === undefined)) {
        this.managedItems.splice(index, 1);
      }

      // Clean up all undefined items at the end of the array. Keep undefined items that
      // are placed before the last defined one to prevent interferences of the dialogs.
      let i = this.managedItems.length - 1;
      while (i >= 0) {
        if (this.managedItems[i] === undefined) {
          this.managedItems.pop();
        } else {
          break;
        }
        i--;
      }
    },
    raiseManagedItem(index) {
      const oldIndex = structuredClone(this.managedItems[index]).zIndex;
      this.managedItems.forEach((item) => {
        if (item && item.zIndex > oldIndex) item.zIndex--;
      });
      this.managedItems[index].zIndex = this.managedItems.length;
    },
    setManagedItem(item) {
      this.managedItem = item;
    },
    setHttpErrorMessage(message) {
      this.httpErrorMessage = message;
    },
    setShowExportDialog(message) {
      this.showExportDialog = message;
    },
    setlistToExport(message) {
      this.listToExport = message;
    },

    /**
     *
     * @param {*} listOfTypes Array of strings with types to load, e.g. ["users", "institutions"]
     * @returns Promise
     */
    searchRequest(listOfTypes) {
      this.isLoading = true;
      const userStore = useUserStore();
      const institutionStore = useInstitutionStore();
      return new Promise((resolve, reject) => {
        const promises = [];
        if (listOfTypes.includes("users")) {
          this.abortControllers.users?.abort();
          const userController = new AbortController();
          this.abortControllers.users = userController;
          promises.push(
            userStore.loadUsers(undefined, undefined, userController)
          );
        }
        if (listOfTypes.includes("institutions")) {
          this.abortControllers.institutions?.abort();
          const institutionController = new AbortController();
          this.abortControllers.institutions = institutionController;
          promises.push(
            institutionStore.loadInstitutions(
              undefined,
              undefined,
              institutionController
            )
          );
        }
        Promise.all(promises)
          .then(() => resolve())
          .catch((error) => {
            reject(error);
          })
          .finally(() => (this.isLoading = false));
      });
    },
    setNetworks(newNetworks) {
      this.networks = newNetworks;
    },
    loadNetworks() {
      return new Promise((resolve, reject) => {
        HTTP.get("iam/networks/")
          .then((response) => {
            this.setNetworks(response.data);
            resolve(response);
          })
          .catch((error) => reject(error));
      });
    },
    // Load networks if there is no network with name networkName. Only necessary if a
    // user/institution with a new network was saved.
    loadNetworksIfNotContains(networkName) {
      if (!this.networks.find((network) => network.name === networkName)) {
        this.loadNetworks();
      }
    },
    openUserEditForm(user, processType = PROCESS_TYPE.EDIT) {
      const profileStore = useProfileStore();
      const ownUsername = profileStore.getOwnUsername;
      const isOwnAccount =
        user.attributes?.username &&
        ownUsername === user.attributes.username[0];
      const u = structuredClone(isOwnAccount ? profileStore.userData : user);
      const keys = Object.keys(u.attributes);
      keys.forEach((key) => {
        if (
          Array.isArray(u.attributes[key]) &&
          u.attributes[key].length === 0
        ) {
          delete u.attributes[key];
        }
      });
      this.setOwnAccount(isOwnAccount);
      this.addManagedItem({
        item: u,
        originalItem: structuredClone(u),
        type: "user",
        processType,
      });
    },
    openInstitutionEditForm(institution, processType = PROCESS_TYPE.EDIT) {
      this.addManagedItem({
        item: institution,
        originalItem: structuredClone(institution),
        type: "institution",
        processType,
      });
    },
  },
});
