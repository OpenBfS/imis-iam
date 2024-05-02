<!--
 Copyright (C) 2024 by Bundesamt fuer Strahlenschutz
 Software engineering by Intevation GmbH

 This file is Free Software under the GNU GPL (v>=3)
 and comes with ABSOLUTELY NO WARRANTY!
 -->
<template>
  <v-text-field
    @keydown.enter="addEntry"
    append-icon="mdi-plus"
    @click:append="addEntry"
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
        ? applicationStore.clientAndServerRules[props.attribute]
        : props.rules
    "
    :variant="props.variant ?? 'underlined'"
    @update:model-value="
      (event) => onUpdateModelValue(event, emit, props.attribute)
    "
  ></v-text-field>
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
import { ref } from "vue";
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
  "modelValue",
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

const addEntry = () => {
  entries.value = [...entries.value, input.value];
  input.value = "";
};

const removeEntry = (index) => {
  entries.value = entries.value.toSpliced(index, 1);
};
</script>
