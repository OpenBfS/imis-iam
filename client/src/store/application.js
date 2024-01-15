/* Copyright (C) 2022 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY!
 */
export const application = {
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
    showManageEventDialog: false,
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
  mutations: {
    setOwnAccount: (state, data) => {
      state.ownAccount = data;
    },
    setShowManageUserDialog: (state, data) => {
      state.showManageUserDialog = data;
    },
    setSearchString: (state, data) => {
      state.searchSting = data;
    },
    setSavedItem: (state, data) => {
      state.savedItem = data;
    },
    setProcessType: (state, data) => {
      state.processType = data;
    },
    setShowManageInstitutionDialog: (state, data) => {
      state.showManageInstitutionDialog = data;
    },
    setShowManageEventDialog: (state, data) => {
      state.showManageEventDialog = data;
    },
    setManagedItem: (state, item) => {
      state.managedItem = item;
    },
    setHttpErrorMessage: (state, message) => {
      state.httpErrorMessage = message;
    },
    setShowExportDialog: (state, message) => {
      state.showExportDialog = message;
    },
    setlistToExport: (state, message) => {
      state.listToExport = message;
    },
  },
};
