<!--
 Copyright (C) 2022 by Bundesamt fuer Strahlenschutz
 Software engineering by Intevation GmbH

 This file is Free Software under the GNU GPL (v>=3)
 and comes with ABSOLUTELY NO WARRANTY!
 -->
<template>
  <v-card min-width="460px">
    <v-card-title>{{ $t("label.csv_options") }}</v-card-title>
    <v-divider></v-divider>
    <v-container class="mt-4">
      <v-row justify="center">
        <v-col cols="10">
          <v-combobox
            :no-data-text="$t('label.no_data_text')"
            :label="$t('label.field_seperator')"
            :items="fieldSeparators"
            v-model="csvOptions.fieldSeparator"
            item-title="name"
            item-value="value"
          >
          </v-combobox>
          <v-combobox
            :no-data-text="$t('label.no_data_text')"
            :label="$t('label.row_delimiter')"
            :items="rowDelimiters"
            item-title="name"
            item-value="value"
            v-model="csvOptions.rowDelimiter"
            persistent-hint
          >
          </v-combobox>
          <v-combobox
            :no-data-text="$t('label.no_data_text')"
            :label="$t('label.encoding')"
            :items="encoding"
            item-title="name"
            item-value="value"
            v-model="csvOptions.encoding"
            persistent-hint
          >
          </v-combobox>
          <v-select
            :no-data-text="$t('label.no_data_text')"
            :label="$t('label.quote_type')"
            :items="quoteTypes"
            item-title="name"
            item-value="value"
            v-model="csvOptions.quoteType"
            persistent-hint
          >
          </v-select>
        </v-col>
        <UIAlert
          v-if="hasRequestError"
          v-bind:isSuccessful="!hasRequestError"
          v-bind:message="applicationStore.httpErrorMessage"
        />
      </v-row>
    </v-container>
    <v-divider></v-divider>
    <v-card-actions>
      <v-spacer></v-spacer>
      <v-btn color="accent" @click="exportFile">
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
import { onMounted, ref } from "vue";
import { useI18n } from "vue-i18n";
import { useApplicationStore } from "@/stores/application.js";
import { useUserStore } from "@/stores/user.js";
import { useInstitutionStore } from "@/stores/institution.js";
import { useNotification } from "@/lib/use-notification.js";
import { createSearchQueryString, HTTP, paramsSerializer } from "@/lib/http.js";

const applicationStore = useApplicationStore();
const institutionStore = useInstitutionStore();
const userStore = useUserStore();
const { t } = useI18n();
const { hasRequestError } = useNotification();
// Use array of objects to enable translation of the itemes in <v-select> element
const fieldSeparators = [
  {
    name: t("label.semicolon"),
    value: ";",
  },
  {
    name: t("label.comma"),
    value: ",",
  },
  {
    name: t("label.space"),
    value: " ",
  },
  {
    name: t("label.period"),
    value: ".",
  },
];
const quoteTypes = [
  {
    name: t("label.doublequote"),
    value: '"',
  },
  {
    name: t("label.singlequote"),
    value: "'",
  },
];
const rowDelimiters = [
  {
    name: t("label.windows"),
    value: "\n\r",
  },
  {
    name: t("label.linux"),
    value: "\r",
  },
];
const encoding = ["iso-8859-15", "utf-16", "utf-8", "ascii"];
const csvOptions = ref({
  fieldSeparator: fieldSeparators[0],
  rowDelimiter: rowDelimiters[0],
  encoding: encoding[0],
  quoteType: quoteTypes[0],
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
      userStore.filterBy
    );
  } else if (applicationStore.listToExport === "institutions") {
    csvOptions.value["search"] = createSearchQueryString(
      undefined,
      institutionStore.filterBy
    );
  }
});

const exportRequest = (itemsName) => {
  const options = structuredClone(csvOptions.value);
  const params = {};
  const keys = Object.keys(options);
  for (const key of keys) {
    if (typeof options[key] === "object") {
      params[key] = encodeURIComponent(options[key].value);
    } else {
      params[key] = encodeURIComponent(options[key]);
    }
  }
  HTTP.get(`/export/${itemsName}`, {
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
