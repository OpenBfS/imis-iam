<!--
 Copyright (C) 2024 by Bundesamt fuer Strahlenschutz
 Software engineering by Intevation GmbH

 This file is Free Software under the GNU GPL (v>=3)
 and comes with ABSOLUTELY NO WARRANTY!
 -->
<template>
  <v-text-field
    @keydown.enter="addEntry"
    v-model="input"
    :clearable="props.clearable"
    :density="props.density ?? 'compact'"
    :disabled="props.disabled"
    :hint="props.hint"
    :label="props.label"
    :name="props.name"
    :persistent-hint="true"
    :prepend-inner-icon="props.prependInnerIcon"
    :readonly="props.readonly"
    :rules="
      applicationStore.clientAndServerRules[props.attribute]
        ? [
            ...applicationStore.clientAndServerRules[props.attribute],
            ...props.rules,
          ]
        : props.rules
    "
    :variant="props.variant ?? 'underlined'"
  >
    <template v-slot:append="{ isValid }">
      <v-btn
        @click="addEntry"
        size="x-small"
        icon="mdi-plus"
        :disabled="!unref(isValid) || input.length === 0"
      ></v-btn>
    </template>
  </v-text-field>
  <v-chip
    v-for="(entry, index) in entries"
    @click:close="() => removeEntry(index)"
    closable
    size="small"
    :key="entry"
  >
    {{ entry }}
  </v-chip>
</template>

<script setup>
import { onMounted, ref, unref, watch } from "vue";
import { useApplicationStore } from "@/stores/application";
import { useForm } from "@/lib/use-form";

const { onUpdateModelValue } = useForm();

const applicationStore = useApplicationStore();

const props = defineProps([
  "class",
  "clearable",
  "density",
  "disabled",
  "hint",
  "label",
  "name",
  "prependInnerIcon",
  "readonly",
  "rules",
  "updateCallback",
  "variant",

  // Custom props
  "attribute",
]);
const emit = defineEmits(["update:modelValue"]);

const input = ref("");
const entries = ref([]);

onMounted(() => {
  if (props.attribute && applicationStore.managedItem[props.attribute]) {
    entries.value = applicationStore.managedItem[props.attribute];
  }
});

watch(entries, (newEntries) => {
  onUpdateModelValue(newEntries, emit, props.attribute);
});

const addEntry = () => {
  entries.value = [...entries.value, input.value];
  input.value = "";
};

const removeEntry = (index) => {
  entries.value = entries.value.toSpliced(index, 1);
};
</script>
