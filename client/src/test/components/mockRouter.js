/* Copyright (C) 2024 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY!
 */
import { createRouter, createWebHashHistory } from "vue-router";
import Archive from "@/components/Mailing/Archive.vue";

const routes = [
  // Prevent a warning about missing path "/"
  { path: "/", component: {} },
  { path: "/archive/:year", name: "Archive", component: Archive },
];

const router = createRouter({
  history: createWebHashHistory(),
  routes: routes,
});

export default router;
