<!--
/* Copyright (C) 2022 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY!
 */
 -->
<template>
  <v-dialog v-model="show">
    <v-card min-width="460px">
      <v-card-title>{{ $t("label.csv_options") }}</v-card-title>
      <v-divider></v-divider>
      <v-container class="mt-4">
        <v-row justify="center">
          <v-col justify="center" cols="10">
            <v-select
              dense
              clearable
              :label="$t('label.field_seperator')"
              :items="['semicolon', 'comma', 'space', 'peroid']"
              v-model="csvOptions.fieldSeperator"
              persistent-hint
            >
            </v-select>
            <v-select
              dense
              clearable
              :label="$t('label.row_delimiter')"
              :items="['linux', 'windows']"
              v-model="csvOptions.rowDelimiter"
              persistent-hint
            >
            </v-select>
            <v-select
              dense
              clearable
              :label="$t('label.encoding')"
              :items="['utf-8', 'utf-16', 'ascii']"
              v-model="csvOptions.encoding"
              persistent-hint
            >
            </v-select>
            <v-select
              dense
              clearable
              :label="$t('label.quote_type')"
              :items="['douplequotes', 'singlequotes']"
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
        <v-btn color="accent" @click="importFile">
          {{ $t("label.import") }}
        </v-btn>
        <v-btn
          color="accent"
          @click="$store.commit('application/setShowImportDialog', false)"
        >
          {{ $t("button.cancel") }}
        </v-btn>
      </v-card-actions>
    </v-card>
  </v-dialog>
</template>

<script>
import { ref, defineAsyncComponent } from "vue";
import { useI18n } from "vue-i18n";
import { useStore } from "vuex";
import { HTTP } from "@/lib/http";
import { useNotification } from "@/lib/use-notification";

export default {
  components: {
    UIAlert: defineAsyncComponent(() => import("@/components/UI/UIAlert.vue")),
  },
  setup() {
    const show = true;
    const store = useStore();
    const { t } = useI18n();
    const { hasRequestError } = useNotification();
    const csvOptions = ref({
      fieldSeperator: "",
      rowDelimiter: "",
      encoding: "",
      quoteType: "",
    });
    //["semicolon", "comma", "space", "dot"];
    const getFieldSeprators = () => {
      return [
        t("label.semicolon"),
        t("label.comma"),
        t("label.space"),
        t("label.dot"),
      ];
    };
    const getQuoteType = () => {
      return [t("label.doublequotes"), t("label.singlequotes")];
    };
    const getRowDelimiter = () => {
      return ["Linux", "Windows"];
    };
    const getEncoding = () => {
      return ["utf-8", "ascii", "base64"];
    };
    const importRequest = (itemsName) => {
      let payload = "";
      if (csvOptions.value && csvOptions.value.fieldSeperator !== "") {
        payload += "fieldSeparator:" + csvOptions.value.fieldSeperator;
      }
      if (csvOptions.value && csvOptions.value.rowDelimiter !== "") {
        const row = "rowDelimiter:" + csvOptions.value.rowDelimiter;
        payload += payload === "" ? row : "&" + row;
      }
      if (csvOptions.value && csvOptions.value.encoding !== "") {
        const encoding = "encoding:" + csvOptions.value.encoding;
        payload += payload === "" ? encoding : "&" + encoding;
      }
      if (csvOptions.value && csvOptions.value.quoteType !== "") {
        const quoteType = "quoteType:" + csvOptions.value.quoteType;
        payload += payload === "" ? quoteType : "&" + quoteType;
      }
      HTTP.get("export/" + itemsName + (payload !== "" ? "?" + payload : ""))
        .then((response) => {
          console.log(response.data);
        })
        .catch(() => {
          hasRequestError.value = true;
        });
    };
    const importFile = () => {
      switch (store.state.application.listToImport) {
        case "users":
          importRequest("user");
          break;
        case "institutions":
          importRequest("institution");
          break;
      }
    };

    return {
      csvOptions,
      getQuoteType,
      getRowDelimiter,
      getEncoding,
      getFieldSeprators,
      importFile,
      show,
      hasRequestError,
    };
  },
};
</script>
