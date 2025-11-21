/* Copyright (C) 2023 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY!
 */
import Components from "unplugin-vue-components/vite";
import { defineConfig } from "vitest/config";
import { playwright } from "@vitest/browser-playwright";
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
    setupFiles: ["src/test/setup.js", "vitest-browser-vue"],
    alias: [{ find: "@", replacement: resolve(__dirname, "./src") }],
    browser: {
      provider: playwright(),
      enabled: true,
      headless: true,
      instances: [{ browser: "chromium" }, { browser: "firefox" }],
      viewport: {
        width: 1200,
        height: 896,
      },
    },
  },
  plugins: [vue(), Components()],
});
