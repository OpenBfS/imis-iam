export const application = {
  namespaced: true,
  state: () => ({
    httpErrorMessage: "",
  }),
  mutations: {
    setHttpErrorMessage: (state, message) => {
      state.httpErrorMessage = message;
    },
  },
};
