import { HTTP } from "../lib/http";
import { Promise } from "core-js";
export const user = {
  namespaced: true,
  state: () => ({
    currentUser: {},
    newUser: {
      username: "",
      firstName: "",
      lastName: "",
      email: "",
      groups: [],
    },
    users: [],
  }),
  mutations: {
    resetNewUser: (state) => {
      state.newUser = {
        username: "",
        firstName: "",
        lastName: "",
        email: "",
      };
    },
    setNewUser: (state, data) => {
      if (data["id"] != null) {
        delete data["id"];
      }
      data.username = data.username + "Copy";
      state.newUser = data;
    },
    setUser: (state, data) => {
      state.currentUser = data;
    },
    setUserList: (state, data) => {
      state.users = data;
    },
  },
  actions: {
    copyUser({ commit, state }) {
      commit("setNewUser", state.currentUser);
    },
    createUser({ commit, state }) {
      return new Promise((resolve, reject) => {
        HTTP.post("/iamuser", state.newUser)
          .then((response) => {
            commit("resetNewUser");
            resolve(response);
          })
          .catch((error) => reject(error));
      });
    },
    //Load all users
    loadUsers({ commit }) {
      HTTP.get("/iamuser").then((response) => {
        if (response.status == 200) {
          commit("setUserList", response.data);
        }
      });
    },
    //Load user by id
    loadUserById({ commit }, id) {
      return new Promise((resolve, reject) => {
        HTTP.get("/iamuser/" + id)
          .then((response) => {
            commit("setUser", response.data);
            resolve(response);
          })
          .catch((error) => reject(error));
      });
    },
    storeUser(context) {
      return new Promise((resolve, reject) => {
        HTTP.put("/iamuser", context.state.currentUser)
          .then((response) => {
            resolve(response);
          })
          .catch((error) => reject(error));
      });
    },
  },
};
