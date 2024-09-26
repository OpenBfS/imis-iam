/* Copyright (C) 2022 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY!
 */
import { defineStore } from "pinia";
import { useInstitutionStore } from "@/stores/institution.js";
import { useUserStore } from "@/stores/user.js";
import { nextTick } from "vue";

export const useApplicationStore = defineStore("application", {
  namespaced: true,
  state: () => ({
    httpErrorMessage: "",
    showExportDialog: false,
    listToExport: "",
    isAllowedToManageUser: false,
    managedItem: {},
    clientAndServerRules: {},
    clientRules: {},
    // Object with fake rules. It contains maximal one rule per input field.
    // These rules always return a message so they always lead to an
    // error message for the attribute. This way we can use Vuetify's internal
    // mechanism to show error messages. We use it to show validation errors
    // coming from keycloak.
    serverValidationRules: {},
    attributesOfFieldsThatChanged: [],
    form: undefined,
    savedItem: {},
    showManageEventDialog: false,
    showManageUserDialog: false,
    showManageInstitutionDialog: false,
    processType: "",
    footerLinks: {
      contact: import.meta.env.VITE_FOOTER_CONTACT,
      help: import.meta.env.VITE_FOOTER_HELP,
      accessibility: import.meta.env.VITE_FOOTER_ACCESSIBILITY,
      privacy: import.meta.env.VITE_FOOTER_PRIVACY,
      socialMedia: import.meta.env.VITE_FOOTER_SOCIALMEDIA,
    },
    reportMail: import.meta.env.VITE_REPORT_MAIL,
    searchString: "",
    ownAccount: false,
    isLoading: false,
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
    submitChangeInField(attribute) {
      this.attributesOfFieldsThatChanged = [
        ...this.attributesOfFieldsThatChanged,
        attribute,
      ];
    },
    removeChangeInField(attribute) {
      const index = this.attributesOfFieldsThatChanged.findIndex(
        (a) => a === attribute
      );
      if (index !== -1) {
        this.attributesOfFieldsThatChanged =
          this.attributesOfFieldsThatChanged.toSpliced(index, 1);
      }
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
          promises.push(userStore.loadUsers(this.searchString));
        }
        if (listOfTypes.includes("institutions")) {
          promises.push(institutionStore.loadInstitutions(this.searchString));
        }
        Promise.all(promises)
          .then(() => resolve())
          .catch((error) => {
            reject(error);
          })
          .finally(() => (this.isLoading = false));
      });
    },
    setForm(newForm) {
      this.form = newForm;
    },
    async clearValidationError(attribute) {
      if (this.serverValidationRules[attribute]) {
        delete this.serverValidationRules[attribute];
        this.aggregateRulesForSingleAttribute(attribute);
        await nextTick();
        this.form.validate();
      }
    },
    initClientRules(rules) {
      this.clientRules = {};
      this.serverValidationRules = {};
      this.clientAndServerRules = {};
      this.clientRules = rules;
      this.clientAndServerRules = Object.assign({}, rules);
    },
    aggregateRulesForSingleAttribute(attribute) {
      delete this.clientAndServerRules[attribute];
      this.clientAndServerRules[attribute] = [];
      if (this.clientRules[attribute]) {
        this.clientAndServerRules[attribute].push(
          ...this.clientRules[attribute]
        );
      }
      if (this.serverValidationRules[attribute]) {
        this.clientAndServerRules[attribute].push(
          this.serverValidationRules[attribute]
        );
      }
    },
  },
});
