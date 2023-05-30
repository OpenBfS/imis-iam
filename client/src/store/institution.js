/* Copyright (C) 2022 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY!
 */
import { Promise } from "core-js";
import { HTTP } from "../lib/http";
export const institution = {
  namespaced: true,
  state: () => ({
    //List of institutions
    institutions: [],
    institutionNames: [],
    //Institution currently edited
    institution: {},
    //Institution to be created
    newInstitution: {
      name: "",
    },
    foundInstitutions: [],
  }),
  mutations: {
    //Convert current institution attributes to string arrays
    convertCurrentInstitutionAttributes: (state) => {
      let attributes = state.institution.attributes;
      for (let attribute in attributes) {
        if (typeof attributes[attribute] == "string") {
          attributes[attribute] = attributes[attribute].split(", ");
        }
      }
    },
    setInstitutionList: (state, data) => {
      state.institutions = data;
    },
    setFoundInstitutions: (state, data) => {
      state.foundInstitutions = data;
    },
    setInstitutionNames: (state, data) => {
      var names = [];
      data.forEach((institution) => {
        names.push(institution.name);
      });
      state.institutionNames = names;
    },
    setInstitution: (state, data) => {
      state.institution = data;
    },
    updateInstitutionEntity: (state, data) => {
      state.institutions.forEach((element, index) => {
        if (element.id === data.id) {
          state.institutions[index] = data;
        }
      });
    },
  },
  actions: {
    loadInstitutions({ commit }, searchString) {
      return new Promise((resolve, reject) => {
        HTTP.get("/institution", {
          params: { search: searchString },
        })
          .then((response) => {
            commit("setInstitutionList", response.data);
            resolve(response);
          })
          .catch((error) => reject(error));
      });
    },
    updateInstitution({ commit }, institution) {
      return new Promise((resolve, reject) => {
        HTTP.put("/institution", institution)
          .then((response) => {
            commit("updateInstitutionEntity", response.data);
            resolve(response);
          })
          .catch((error) => reject(error));
      });
    },
  },
};
