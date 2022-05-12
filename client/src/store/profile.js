import { HTTP } from "../lib/http";
export const profile = {
  state: () => ({
    username: "",
    firstname: "",
    lastname: "",
    email: "",
  }),
  mutations: {
    setProfile: (state, data) => {
      state.username = data.username;
      state.firstname = data.firstname;
      state.lastname = data.lastname;
      state.email = data.email;
    },
  },
  actions: {
    loadProfile({ commit }) {
      HTTP.get("/iamuser/profile").then((response) => {
        const { username, firstName, lastName, email } = response.data;
        commit("setProfile", {
          username: username,
          firstname: firstName,
          lastname: lastName,
          email: email,
        });
      });
    },
  },
};
