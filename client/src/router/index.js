/* Copyright (C) 2022 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY!
 */
import { createRouter, createWebHashHistory } from "vue-router";
import Home from "@/components/Main.vue";
import Institution from "@/components/Institution/Institutions.vue";
import UserView from "@/components/User/Users.vue";
import MailingList from "@/components/Mailing/MailingLists.vue";
import Search from "@/components/Search/Search.vue";

const routes = [
  {
    path: "/",
    name: "home",
    component: Home,
  },

  {
    path: "/search",
    name: "Search",
    component: Search,
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
    path: "/mailinglists",
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
