import { HTTP } from "../lib/http";
export const profile ={
    state: () => ({
        profile: {}
    }),
    mutations: {
        setUser: (state, data) => {
            state.profile = data;
        }
    },
    actions: {
        loadProfile({ commit }) {
            HTTP.get("/iamuser/profile").then(response => {
                const { username, firstname, lastname, emails } = response.data;
                commit("setProfile", {
                    username: username,
                    firstname: firstname,
                    lastname: lastname,
                    emails: emails
                  });
            });
        }
    }
}