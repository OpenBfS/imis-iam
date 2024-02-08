/* Copyright (C) 2022 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY!
 */
import { defineStore } from "pinia";
import { HTTP } from "../lib/http";

export const useMailStore = defineStore("mail", {
  namespaced: true,
  state: () => ({
    mailTypes: [],
    mailingLists: [],
  }),
  actions: {
    setMailTypes(data) {
      this.mailTypes = data;
    },
    setMailinglists(data) {
      this.mailingLists = data;
    },
    loadMailTypes() {
      return new Promise((resolve, reject) => {
        HTTP.get("mail/type")
          .then((response) => {
            this.setMailTypes(response.data);
            resolve(response);
          })
          .catch((error) => reject(error));
      });
    },
    loadMailinglists() {
      return new Promise((resolve, reject) => {
        HTTP.get("mail/list")
          .then((response) => {
            this.setMailinglists(response.data);
            resolve(response);
          })
          .catch((error) => reject(error));
      });
    },
  },
});
