import { HTTP } from "../lib/http";
export const user = {
  namespaced: true,
  state: () => ({
    users: [],
  }),
  mutations: {
    setUserList: (state, data) => {
      state.users = data;
    },
  },
  actions: {
    //Load all users
    loadUsers({ commit }) {
      HTTP.get("/iamuser").then((response) => {
        if (response.status == 200) {
          commit("setUserList", response.data);
        }
      });
    },
  },
};
