/* Copyright (C) 2022 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY!
 */
import { defineStore } from "pinia";
import { HTTP } from "../lib/http.js";

export const useMailStore = defineStore("mail", {
  namespaced: true,
  state: () => ({
    mailTypes: [],
  }),
  actions: {
    setMailTypes(data) {
      this.mailTypes = data;
    },
    loadMailTypes() {
      return new Promise((resolve, reject) => {
        HTTP.get("iam/mail/type")
          .then((response) => {
            this.setMailTypes(response.data);
            resolve(response);
          })
          .catch((error) => reject(error));
      });
    },
  },
});
