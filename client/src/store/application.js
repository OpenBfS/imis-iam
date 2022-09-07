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
  }),
  mutations: {
    setShowManageUserDialog: (state, data) => {
      state.showManageUserDialog = data;
    },
    setSavedItem: (state, data) => {
      state.setSavedItem = data;
    },
    setProcessType: (state, data) => {
      state.processType = data;
    },
    setShowManageInstitutionDialog: (state, data) => {
      state.showManageInstitutionDialog = data;
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
