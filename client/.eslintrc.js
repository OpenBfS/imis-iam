/* Copyright (C) 2022 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY!
 */
module.exports = {
  root: true,
  env: {
    "vue/setup-compiler-macros": true,
    node: true,
  },
  extends: [
    "plugin:vue/vue3-essential",
    "eslint:recommended",
    "plugin:prettier/recommended",
  ],
  parserOptions: {
    parser: "@babel/eslint-parser",
  },
  plugins: ["notice"],
  rules: {
    "vue/script-setup-uses-vars": "error",
    "vue/multi-word-component-names": "off",
    "no-console": process.env.NODE_ENV === "production" ? "warn" : "off",
    "no-debugger": process.env.NODE_ENV === "production" ? "warn" : "off",
    "notice/notice": [
      "warn",
      {
        templateFile: "./src/license/jsTemplate.txt",
        messages: {
          whenFailedToMatch: "Missing or wrong license header",
        },
      },
    ],
  },
  overrides: [
    {
      files: ["src/components/*.vue", "src/components/*/*.vue", "src/*.vue"],
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
  ],
};
