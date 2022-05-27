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
  }),
  mutations: {
    setInstitutionList: (state, data) => {
      state.institutions = data;
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
  },
  actions: {
    createInstitution(context, then) {
      HTTP.post("/institution", context.state.newInstitution).then(() => {
        if (then) {
          then();
        }
      });
    },
    /**
     * Load all institutions
     */
    loadInstitutions({ commit }) {
      HTTP.get("/institution")
        .then((response) => {
          commit("setInstitutionList", response.data);
        })
        .catch((error) => console.log(error)); // TODO: Handle http error in component
    },

    loadInstitutionNames({ commit }) {
      HTTP.get("/institution")
        .then((response) => {
          commit("setInstitutionNames", response.data);
        })
        .catch((error) => console.log(error)); // TODO: Handle http error in component
    },

    loadInstitutionById({ commit }, id) {
      return new Promise((resolve, reject) => {
        HTTP.get("/institution/" + id)
          .then((response) => {
            commit("setInstitution", response.data);
            resolve(response);
          })
          .catch((error) => reject(error)); // TODO: Handle http error in component
      });
    },

    /**
     * Store the institution
     * @param {Function} then Function, called after saving
     */
    storeInstitution(context, then) {
      HTTP.put("/institution", context.state.institution).then(() => {
        if (then) {
          then();
        }
      });
    },
  },
};
