/* Copyright (C) 2022 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY!
 */
import { defineStore } from "pinia";

export const useApplicationStore = defineStore("application", {
  namespaced: true,
  state: () => ({
    httpErrorMessage: "",
    showExportDialog: false,
    listToExport: "",
    isAllowedToManageUser: false,
    managedItem: {},
    savedItem: {},
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
    searchSting: "",
    ownAccount: false,
  }),
  actions: {
    setOwnAccount(data) {
      this.$patch({
        ownAccount: data,
      });
    },
    setShowManageUserDialog(data) {
      this.$patch({
        showManageUserDialog: data,
      });
    },
    setSearchString(data) {
      this.$patch({
        searchSting: data,
      });
    },
    setSavedItem(data) {
      this.$patch({
        savedItem: data,
      });
    },
    setProcessType(data) {
      this.$patch({
        processType: data,
      });
    },
    setShowManageInstitutionDialog(data) {
      this.$patch({
        showManageInstitutionDialog: data,
      });
    },
    setManagedItem(item) {
      this.$patch({
        managedItem: item,
      });
    },
    setHttpErrorMessage(message) {
      this.$patch({
        httpErrorMessage: message,
      });
    },
    setShowExportDialog(message) {
      this.$patch({
        showExportDialog: message,
      });
    },
    setlistToExport(message) {
      this.$patch({
        listToExport: message,
      });
    },
  },
});
