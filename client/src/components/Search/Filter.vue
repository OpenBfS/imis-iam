<!--
 Copyright (C) 2024 by Bundesamt fuer Strahlenschutz
 Software engineering by Intevation GmbH

 This file is Free Software under the GNU GPL (v>=3)
 and comes with ABSOLUTELY NO WARRANTY!
 -->
<template>
  <div class="table-filter">
    <v-autocomplete
      v-if="props.items"
      :label="props.label"
      :items="props.items"
      :item-title="props.itemTitle"
      :item-value="props.itemValue"
      :no-data-text="$t('label.noDataText')"
      base-color="accent"
      bg-color="accent-lighten-5"
      class="my-1"
      clearable
      hide-details
      min-width="120"
      :model-value="autocompleteValue"
      :multiple="multiple"
      variant="outlined"
      @click:clear="handleFilterInput"
      @update:menu="
        (event) => {
          if (!event) handleFilterInput();
        }
      "
      @update:modelValue="
        (event) => {
          autocompleteValue = event;
        }
      "
    ></v-autocomplete>
    <v-text-field
      v-else
      base-color="accent"
      bg-color="accent-lighten-5"
      class="my-1"
      clearable
      color="accent"
      density="compact"
      :model-value="textFieldValue"
      :placeholder="props.placeholder"
      hide-details
      min-width="40"
      variant="outlined"
      @update:modelValue="
        (event) => {
          textFieldValue = event;
          handleFilterInput();
        }
      "
    ></v-text-field>
  </div>
</template>

<style>
.table-filter .v-field__input {
  padding-inline: 6pt 6pt !important;
}

.table-filter .v-autocomplete .v-field label {
  margin-inline-start: 6pt;
}
</style>

<script setup>
import { computed, onMounted, ref, toRaw } from "vue";
import { useApplicationStore } from "@/stores/application";
import { useInstitutionStore } from "@/stores/institution";
import { useUserStore } from "@/stores/user";
import debounce from "debounce";
import { useForm } from "@/lib/use-form";

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

const { areArraysDifferent } = useForm();

const textFieldValue = ref("");
const autocompleteValue = ref(null);

const multiple = computed(() => {
  return (
    props.type === "users" &&
    !["enabled", "hiddenInAddressbook", "role", "retired"].includes(props.filterKey)
  );
});

onMounted(() => {
  // Restore value when the column containing this filter component was hidden and shown again.
  let restoredKey = "";
  if (props.type === "users") {
    restoredKey = userStore.filterBy[props.filterKey];
  } else if (props.type === "institutions") {
    restoredKey = institutionStore.filterBy[props.filterKey];
  }
  if (props.items) {
    autocompleteValue.value = restoredKey;
  } else {
    textFieldValue.value = restoredKey;
  }
});

const triggerSearch = debounce(() => {
  applicationStore.searchRequest([props.type]);
}, 500);

const handleFilterInput = () => {
  const value = toRaw(
    props.items ? autocompleteValue.value : textFieldValue.value
  );
  const term = value !== null ? value : "";
  const store = props.type === "users" ? userStore : institutionStore;
  const oldTerm = toRaw(store.filterBy[props.filterKey]);
  store.updateFilter(props.filterKey, term);
  // Only send request when value did change
  if (
    multiple.value &&
    ((oldTerm == undefined && term == undefined) ||
      (oldTerm != undefined && oldTerm.length === 0 && term == undefined) ||
      (term != undefined && term.length === 0 && oldTerm == undefined) ||
      (oldTerm != undefined &&
        term != undefined &&
        !areArraysDifferent(oldTerm, term, false)))
  ) {
    return;
  } else if (!multiple.value && oldTerm === term) {
    return;
  }
  triggerSearch();
};
</script>
