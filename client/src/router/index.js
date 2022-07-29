import { createRouter, createWebHashHistory } from "vue-router";
import Home from "@/components/Main.vue";
import Institution from "@/components/Institutions.vue";
import UserView from "@/components/User/Users.vue";
import MailingList from "@/components/Mailing/MailingList.vue";

const routes = [
  {
    path: "/",
    name: "home",
    component: Home,
  },
  {
    path: "/institutions",
    name: "Institution",
    component: Institution,
  },
  {
    path: "/users",
    name: "User",
    component: UserView,
  },
  {
    path: "/mailinglist",
    name: "MailingList",
    component: MailingList,
  },
  {
    path: "/archive/:year",
    name: "Archive",
    component: () => import("@/components/Mailing/Archive.vue"),
  },
];

const router = createRouter({
  history: createWebHashHistory(),
  routes,
});

export default router;
