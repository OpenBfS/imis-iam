import Vuex from "vuex";
import { profile } from "./profile";
import { institution } from "./institution";

export default new Vuex.Store({
  modules: {
    institution,
    profile,
  },
});
