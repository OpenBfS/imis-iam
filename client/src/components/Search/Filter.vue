<!--
 Copyright (C) 2024 by Bundesamt fuer Strahlenschutz
 Software engineering by Intevation GmbH

 This file is Free Software under the GNU GPL (v>=3)
 and comes with ABSOLUTELY NO WARRANTY!
 -->
<template>
  <v-autocomplete
    v-if="props.items"
    :label="props.label"
    :items="props.items"
    :item-title="props.itemTitle"
    :item-value="props.itemValue"
    :no-data-text="$t('label.no_data_text')"
    class="my-1"
    clearable
    hide-details
    min-width="120"
    variant="outlined"
    @update:modelValue="(event) => handleFilterInput(event)"
  ></v-autocomplete>
  <v-text-field
    v-else
    class="my-1"
    density="compact"
    :placeholder="props.placeholder"
    hide-details
    variant="outlined"
    @update:modelValue="(event) => handleFilterInput(event)"
  ></v-text-field>
</template>

<script setup>
import { useApplicationStore } from "@/stores/application";
import { useInstitutionStore } from "@/stores/institution";
import { useUserStore } from "@/stores/user";
import debounce from "debounce";

const applicationStore = useApplicationStore();
const institutionStore = useInstitutionStore();
const userStore = useUserStore();

const props = defineProps([
  "filterKey",
  "items",
  "itemTitle",
  "itemValue",
  "label",
  "placeholder",
  "type",
]);

const triggerSearch = debounce(() => {
  applicationStore.searchRequest([props.type]);
}, 500);

const handleFilterInput = (value) => {
  const term = value !== null ? value : "";
  if (props.type === "users") {
    userStore.updateFilter(props.filterKey, term);
  } else if (props.type === "institutions") {
    institutionStore.updateFilter(props.filterKey, term);
  }
  triggerSearch();
};
</script>
