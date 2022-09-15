/* Copyright (C) 2022 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY!
 */
import { createRouter, createWebHashHistory } from "vue-router";
import Home from "@/components/Main.vue";

const routes = [
  {
    path: "/",
    name: "home",
    component: Home,
  },

  {
    path: "/search",
    name: "Search",
    component: () => import("@/components/Search/Search.vue"),
  },
  {
    path: "/institutions",
    name: "Institution",
    component: () => import("@/components/Institution/Institutions.vue"),
  },
  {
    path: "/users",
    name: "User",
    component: () => import("@/components/User/Users.vue"),
  },
  {
    path: "/mailinglists",
    name: "MailingList",
    component: () => import("@/components/Mailing/MailingLists.vue"),
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
