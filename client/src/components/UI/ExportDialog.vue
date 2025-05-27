<!--
 Copyright (C) 2022 by Bundesamt fuer Strahlenschutz
 Software engineering by Intevation GmbH

 This file is Free Software under the GNU GPL (v>=3)
 and comes with ABSOLUTELY NO WARRANTY!
 -->
<template>
  <v-card min-width="460px">
    <v-card-title>{{ $t("export.csvOptions") }}</v-card-title>
    <v-divider></v-divider>
    <v-container class="mt-4">
      <v-row justify="center">
        <v-col cols="10">
          <v-form v-model="valid" ref="form">
            <Combobox
              :label="$t('export.fieldSeparator')"
              :items="fieldSeparators"
              attribute="fieldSeparator"
              item-title="name"
              item-value="value"
              required
              @update:modelValue="csvOptions.fieldSeparator = $event"
            ></Combobox>
            <Combobox
              :label="$t('export.rowDelimiter')"
              :items="rowDelimiters"
              attribute="rowDelimiter"
              item-title="name"
              item-value="value"
              persistent-hint
              required
              @update:modelValue="csvOptions.rowDelimiter = $event"
            ></Combobox>
            <Combobox
              :label="$t('export.encoding')"
              :items="encoding"
              item-title="name"
              item-value="value"
              attribute="encoding"
              persistent-hint
              required
              @update:modelValue="csvOptions.encoding = $event"
            ></Combobox>
            <Combobox
              attribute="quoteType"
              :no-data-text="$t('label.noDataText')"
              :label="$t('export.quoteType')"
              :items="quoteTypes"
              item-title="name"
              item-value="value"
              persistent-hint
              required
              @update:modelValue="csvOptions.quoteType = $event"
            ></Combobox>
          </v-form>
        </v-col>
        <UIAlert
          v-if="hasRequestError || hasRequestError"
          v-bind:isSuccessful="!hasRequestError"
          v-bind:message="applicationStore.httpErrorMessage"
        />
      </v-row>
    </v-container>
    <v-divider></v-divider>
    <v-card-actions>
      <v-spacer></v-spacer>
      <v-btn color="accent" @click="exportFile" :disabled="!valid">
        {{ $t("label.export") }}
      </v-btn>
      <v-btn
        color="accent"
        @click="applicationStore.setShowExportDialog(false)"
      >
        {{ $t("button.cancel") }}
      </v-btn>
    </v-card-actions>
  </v-card>
</template>

<script setup>
import { onBeforeMount, onMounted, onUnmounted, provide, ref } from "vue";
import { useI18n } from "vue-i18n";
import { useApplicationStore } from "@/stores/application.js";
import { useUserStore } from "@/stores/user.js";
import { useInstitutionStore } from "@/stores/institution.js";
import { useNotification } from "@/lib/use-notification.js";
import { useForm } from "@/lib/use-form.js";
import { createSearchQueryString, HTTP, paramsSerializer } from "@/lib/http.js";
import Combobox from "../Form/Combobox.vue";

const applicationStore = useApplicationStore();
const institutionStore = useInstitutionStore();
const userStore = useUserStore();
const { t } = useI18n();
const { hasRequestError } = useNotification();
// Use array of objects to enable translation of the itemes in <v-select> element
const fieldSeparators = [
  {
    name: t("export.semicolon"),
    value: ";",
  },
  {
    name: t("export.comma"),
    value: ",",
  },
  {
    name: t("export.space"),
    value: " ",
  },
  {
    name: t("export.period"),
    value: ".",
  },
];
const quoteTypes = [
  {
    name: t("export.doublequote"),
    value: '"',
  },
  {
    name: t("export.singlequote"),
    value: "'",
  },
];
const rowDelimiters = [
  {
    name: t("export.windows"),
    value: "\r\n",
  },
  {
    name: t("export.linux"),
    value: "\n",
  },
];
const encoding = ["iso-8859-15", "utf-16", "utf-8", "ascii"];
const csvOptions = ref({
  fieldSeparator: fieldSeparators[0].value,
  rowDelimiter: rowDelimiters[0].value,
  encoding: encoding[0],
  quoteType: quoteTypes[0].value,
});
const { form, valid, validLength } = useForm();

provide("translationCategory", "export");

onBeforeMount(() => {
  applicationStore.setForm(form);
  applicationStore.initClientRules({
    fieldSeparator: [
      ...validLength(
        undefined,
        1,
        t("error.invalidLengthTooLong", {
          attribute: t("export.fieldSeparator"),
          max: 1,
        }),
      ),
    ],
    quoteType: [
      ...validLength(
        undefined,
        1,
        t("error.invalidLengthTooLong", {
          attribute: t("export.quoteType"),
          max: 1,
        }),
      ),
    ],
  });
});

onMounted(() => {
  if (
    applicationStore.listToExport === "users" &&
    userStore.selectedUsers.length > 0
  ) {
    csvOptions.value["id"] = userStore.selectedUsers;
  } else if (institutionStore.selectedInstitutions.length > 0) {
    csvOptions.value["id"] = institutionStore.selectedInstitutions;
  }
  if (applicationStore.listToExport === "users") {
    csvOptions.value["search"] = createSearchQueryString(
      undefined,
      userStore.filterBy,
    );
  } else if (applicationStore.listToExport === "institutions") {
    csvOptions.value["search"] = createSearchQueryString(
      undefined,
      institutionStore.filterBy,
    );
  }
  applicationStore.managedItem = csvOptions.value;
});

onUnmounted(() => {
  applicationStore.removeAllResetEventListeners();
});

const exportRequest = (itemsName) => {
  const options = structuredClone(csvOptions.value);
  const params = {};
  const keys = Object.keys(options);
  for (const key of keys) {
    if (typeof options[key] === "object") {
      params[key] = options[key]?.value;
    } else {
      params[key] = options[key];
    }
  }
  if (
    applicationStore.listToExport === "users" &&
    userStore.selectedTableColumns?.length > 0
  ) {
    params["attributes"] = userStore.selectedTableColumns;
  } else if (
    applicationStore.listToExport === "institutions" &&
    institutionStore.selectedTableColumns?.length > 0
  ) {
    params["attributes"] = institutionStore.selectedTableColumns;
  }
  HTTP.get(`/iam/export/${itemsName}`, {
    params,
    paramsSerializer,
    // responseEncoding and responseType are necessary so axios doesn't decode the response
    // what would lead to a wrong encoding of the download.
    responseEncoding: "binary",
    responseType: "arraybuffer",
  })
    .then((res) => {
      if (!res.statusText === "OK") throw new Error(res.statusText);
      const blob = new Blob([res.data]);
      const link = document.createElement("a");
      link.href = URL.createObjectURL(blob);
      link.download = "export.csv";
      link.click();
      URL.revokeObjectURL(link.href);
      applicationStore.showExportDialog = false;
    })
    .catch((error) => {
      hasRequestError.value = true;
      applicationStore.setHttpErrorMessage(error.message);
    });
};
const exportFile = () => {
  switch (applicationStore.listToExport) {
    case "users":
      exportRequest("user");
      break;
    case "institutions":
      exportRequest("institution");
      break;
  }
};
</script>
