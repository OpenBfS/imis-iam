import { createRouter, createWebHashHistory } from "vue-router";
import HomeView from "../views/HomeView.vue";
import InstitutionView from "../views/InstitutionView.vue";
import ProfileView from "../views/ProfileView.vue";
import UserView from "../views/UserView.vue";

const routes = [
  {
    path: "/",
    name: "home",
    component: HomeView,
  },
  {
    path: "/about",
    name: "about",
    // route level code-splitting
    // this generates a separate chunk (about.[hash].js) for this route
    // which is lazy-loaded when the route is visited.
    component: () =>
      import(/* webpackChunkName: "about" */ "../views/AboutView.vue"),
  },
  {
    path: "/profile",
    name: "Profile",
    component: ProfileView,
  },
  {
    path: "/institutions",
    name: "Institution",
    component: InstitutionView,
  },
  {
    path: "/users",
    name: "User",
    component: UserView,
  },
];

const router = createRouter({
  history: createWebHashHistory(),
  routes,
});

export default router;
