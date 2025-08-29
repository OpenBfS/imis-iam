/* Copyright (C) 2022 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY!
 */
import js from "@eslint/js";
import pluginVue from "eslint-plugin-vue";
import prettier from "eslint-config-prettier";
import notice from "eslint-plugin-notice";
import globals from "globals";

export default [
  {
    name: "app/files-to-lint",
    files: ["**/*.{js,mjs,jsx,vue}"],
  },

  {
    name: "app/files-to-ignore",
    ignores: ["**/dist/**", "**/dist-ssr/**", "**/coverage/**", ".gitignore"],
  },

  js.configs.recommended,
  ...pluginVue.configs["flat/essential"],
  {
    rules: {
      "vue/multi-word-component-names": "off",
      "no-console": process.env.NODE_ENV === "production" ? "warn" : "off",
      "no-debugger": process.env.NODE_ENV === "production" ? "warn" : "off",
    },
    languageOptions: {
      ecmaVersion: 2024,
      sourceType: "module",
      globals: {
        ...globals.browser,
      },
    },
  },
  {
    name: "license-check",
    files: ["src/components/*.vue", "src/components/*/*.vue", "src/*.vue"],
    plugins: {
      notice,
    },
    rules: {
      "notice/notice": [
        "warn",
        {
          templateFile: "./src/license/vueTemplate.txt",
          messages: {
            whenFailedToMatch: "Missing or wrong license header",
          },
        },
      ],
    },
  },
  prettier,
];
