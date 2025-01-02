/* Copyright (C) 2024 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY!
 */
import Components from "unplugin-vue-components/vite";
import { defineConfig } from "vite";
import { resolve } from "path";
import Vue from "@vitejs/plugin-vue";
import Vuetify from "vite-plugin-vuetify";

export default defineConfig({
  plugins: [
    Vue(),
    Vuetify(),
    Components(),
  ],
  define: { "process.env": {} },
  resolve: {
    alias: {
      "@": resolve(__dirname, "./src"),
    },
    extensions: [".js", ".json", ".jsx", ".mjs", ".ts", ".tsx", ".vue"],
  },
});
