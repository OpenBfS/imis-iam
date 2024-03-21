/* Copyright (C) 2022 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY!
 */
import { defineStore } from "pinia";
import { useInstitutionStore } from "@/stores/institution";
import { useUserStore } from "@/stores/user";

export const useApplicationStore = defineStore("application", {
  namespaced: true,
  state: () => ({
    httpErrorMessage: "",
    showExportDialog: false,
    listToExport: "",
    isAllowedToManageUser: false,
    managedItem: {},
    savedItem: {},
    showManageEventDialog: false,
    showManageUserDialog: false,
    showManageInstitutionDialog: false,
    processType: "",
    footerLinks: {
      contact: process.env.VUE_APP_FOOTER_CONTACT,
      help: process.env.VUE_APP_FOOTER_HELP,
      accessibility: process.env.VUE_APP_FOOTER_ACCESSIBILITY,
      privacy: process.env.VUE_APP_FOOTER_PRIVACY,
      socialMedia: process.env.VUE_APP_FOOTER_SOCIALMEDIA,
    },
    reportMail: process.env.VUE_APP_REPORT_MAIL,
    searchString: "",
    ownAccount: false,
  }),
  actions: {
    setOwnAccount(data) {
      this.ownAccount = data;
    },
    setShowManageUserDialog(data) {
      this.showManageUserDialog = data;
    },
    setShowManageEventDialog(data) {
      this.showManageEventDialog = data;
    },
    setSearchString(data) {
      this.searchString = data;
    },
    setSavedItem(data) {
      this.savedItem = data;
    },
    setProcessType(data) {
      this.processType = data;
    },
    setShowManageInstitutionDialog(data) {
      this.showManageInstitutionDialog = data;
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
     * @param {*} force Force search request even if the searchString is empty
     * @returns Promise
     */
    searchRequest(listOfTypes, force = false) {
      const userStore = useUserStore();
      const institutionStore = useInstitutionStore();
      return new Promise((resolve, reject) => {
        const promises = [];
        if (force || this.searchString?.length > 0) {
          if (listOfTypes.includes("users")) {
            promises.push(userStore.loadUsers(this.searchString));
          }
          if (listOfTypes.includes("institutions")) {
            promises.push(institutionStore.loadInstitutions(this.searchString));
          }
        }
        Promise.all(promises)
          .then(() => resolve())
          .catch((error) => {
            reject(error);
          });
      });
    },
  },
});
