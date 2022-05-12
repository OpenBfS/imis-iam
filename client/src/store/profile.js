import { HTTP } from "../lib/http";
export const profile = {
  namespaced: true,
  state: () => ({
    userData: {},
  }),
  mutations: {
    setUserData: (state, data) => {
      state.userData = data;
    },
  },
  actions: {
    loadProfile({ commit }) {
      HTTP.get("/iamuser/profile").then((response) => {
        commit("setUserData", response.data);
      });
    },
  },
};
