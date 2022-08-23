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
              :items="fieldSeparators"
              v-model="csvOptions.fieldSeperator"
              item-title="name"
              item-value="value"
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
    // Use objects to enable translation of the itemes in <v-select> element
    // TODO: Use slots to change the appearance of the items in the element
    // when this gets implemented by Vuetify upstream.
    const fieldSeparators = [
      {
        name: t("label.semicolon"),
        value: "semicolon",
      },
      {
        name: t("label.comma"),
        value: "comma",
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
        name: t("label.singlequotes"),
        value: "singlequote",
      },
      {
        name: t("label.doublequote"),
        value: "douplequote",
      },
    ];
    const exportRequest = (itemsName) => {
      let payload = "";
      if (csvOptions.value && csvOptions.value.fieldSeperator !== "") {
        payload += "fieldSeparator=" + csvOptions.value.fieldSeperator;
      }
      if (csvOptions.value && csvOptions.value.rowDelimiter !== "") {
        const row = "rowDelimiter=" + csvOptions.value.rowDelimiter;
        payload += payload === "" ? row : "&" + row;
      }
      if (csvOptions.value && csvOptions.value.encoding !== "") {
        const encoding = "encoding=" + csvOptions.value.encoding;
        payload += payload === "" ? encoding : "&" + encoding;
      }
      if (csvOptions.value && csvOptions.value.quoteType !== "") {
        const quoteType = "quoteType=" + csvOptions.value.quoteType;
        payload += payload === "" ? quoteType : "&" + quoteType;
      }
      HTTP.get("export/" + itemsName + (payload !== "" ? "?" + payload : ""))
        .then((response) => {
          const blob = new Blob([response.data], {
            type: "application/octet-stream",
          });
          const link = document.createElement("a");
          link.href = URL.createObjectURL(blob);
          link.download = "export.csv";
          link.click();
          URL.revokeObjectURL(link.href);
        })
        .catch(() => {
          hasRequestError.value = true;
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

    return {
      fieldSeparators,
      quoteTypes,
      csvOptions,
      exportFile,
      show,
      hasRequestError,
    };
  },
};
</script>
