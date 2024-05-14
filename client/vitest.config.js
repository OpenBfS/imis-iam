/* Copyright (C) 2023 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY!
 */
import { defineConfig } from "vitest/config";
import { resolve } from "path";
import vue from "@vitejs/plugin-vue";

export default defineConfig({
  test: {
    environment: "jsdom",
    server: {
      deps: {
        inline: ["vuetify"],
      },
    },
    setupFiles: ["src/test/setup.js"],
    alias: [{ find: "@", replacement: resolve(__dirname, "./src") }],
  },
  plugins: [vue()],
});
