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
    showImportDialog: false,
    listToImport: "",
  }),
  mutations: {
    setHttpErrorMessage: (state, message) => {
      state.httpErrorMessage = message;
    },
    setShowImportDialog: (state, message) => {
      state.showImportDialog = message;
    },

    setListToImport: (state, message) => {
      state.listToImport = message;
    },
  },
};
