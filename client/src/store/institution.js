import { HTTP } from "../lib/http";
export const institution = {
  namespaced: true,
  state: () => ({
    //List of institutions
    institutions: [],
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
      HTTP.get("/institution").then((response) => {
        if (response.status == 200) {
          commit("setInstitutionList", response.data);
        }
      });
    },
    /**
     * Load institution by id
     * @param {*} id Id to load
     */
    loadInstitutionById({ commit }, id) {
      HTTP.get("/institution/" + id).then((response) => {
        if (response.status == 200) {
          commit("setInstitution", response.data);
        }
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
