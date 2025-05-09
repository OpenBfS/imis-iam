/* Copyright (C) 2022 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY!
 */
import { createRouter, createWebHashHistory } from "vue-router";
import Search from "@/components/Search/Search.vue";

const routes = [
  {
    path: "/",
    name: "search",
    component: Search,
  },

  {
    path: "/search",
    name: "Search",
    component: () => import("@/components/Search/Search.vue"),
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
