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
        if (response.status == 200) {
          commit("setUserData", response.data);
        }
      });
    },
    storeProfile(context) {
      HTTP.put("/iamuser/profile", context.state.userData);
    },
  },
};
