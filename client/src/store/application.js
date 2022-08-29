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
  }),
  mutations: {
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
