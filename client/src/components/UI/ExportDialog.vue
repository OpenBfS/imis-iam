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
        <v-col justify="center" cols="10">
          <v-select
            :no-data-text="$t('label.no_data_text')"
            :label="$t('label.field_seperator')"
            :items="fieldSeparators"
            v-model="csvOptions.fieldSeparator"
            item-title="name"
            item-value="value"
          >
          </v-select>
          <v-select
            :no-data-text="$t('label.no_data_text')"
            :label="$t('label.row_delimiter')"
            :items="rowDelimiters"
            item-title="name"
            item-value="value"
            v-model="csvOptions.rowDelimiter"
            persistent-hint
          >
          </v-select>
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
          v-bind:message="$store.state.application.httpErrorMessage"
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
        @click="$store.commit('application/setShowExportDialog', false)"
      >
        {{ $t("button.cancel") }}
      </v-btn>
    </v-card-actions>
  </v-card>
</template>

<script setup>
import { ref } from "vue";
import { useI18n } from "vue-i18n";
import { useStore } from "vuex";
import { useNotification } from "@/lib/use-notification";

const store = useStore();
const { t } = useI18n();
const { hasRequestError } = useNotification();
// Use array of objects to enable translation of the itemes in <v-select> element
const fieldSeparators = [
  {
    name: t("label.comma"),
    value: "comma",
  },
  {
    name: t("label.semicolon"),
    value: "semicolon",
  },
  {
    name: t("label.space"),
    value: "space",
  },
  {
    name: t("label.period"),
    value: "period",
  },
];
const quoteTypes = [
  {
    name: t("label.doublequote"),
    value: "doublequote",
  },
  {
    name: t("label.singlequote"),
    value: "singlequote",
  },
];
const rowDelimiters = [
  {
    name: t("label.linux"),
    value: "linux",
  },
  {
    name: t("label.windows"),
    value: "windows",
  },
];
const encoding = ["utf-8", "utf-16", "ascii"];
const csvOptions = ref({
  fieldSeparator: fieldSeparators[0].value,
  rowDelimiter: rowDelimiters[0].value,
  encoding: encoding[0],
  quoteType: quoteTypes[0].value,
});

const exportRequest = (itemsName) => {
  fetch(
    "/backend/realms/imis3/export/" +
      itemsName +
      "?" +
      new URLSearchParams(csvOptions.value).toString()
  )
    .then((res) => {
      if (!res.ok) throw new Error(res.statusText);
      return res.blob();
    })
    .then((data) => {
      const blob = new Blob([data]);
      const link = document.createElement("a");
      link.href = URL.createObjectURL(blob);
      link.download = "export.csv";
      link.click();
      URL.revokeObjectURL(link.href);
      store.state.application.showExportDialog = false;
    })
    .catch((error) => {
      hasRequestError.value = true;
      store.commit("application/setHttpErrorMessage", error.message);
    });
};
const exportFile = () => {
  switch (store.state.application.listToExport) {
    case "users":
      exportRequest("user");
      break;
    case "institutions":
      exportRequest("institution");
      break;
  }
};
</script>
