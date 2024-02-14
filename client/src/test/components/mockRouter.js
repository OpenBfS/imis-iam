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
