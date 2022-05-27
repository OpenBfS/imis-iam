import Vuex from "vuex";
import { profile } from "./profile";
import { institution } from "./institution";
import { user } from "./user";

export default new Vuex.Store({
  modules: {
    institution,
    profile,
    user,
  },
});
