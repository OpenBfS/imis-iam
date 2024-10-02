<!--
 Copyright (C) 2024 by Bundesamt fuer Strahlenschutz
 Software engineering by Intevation GmbH

 This file is Free Software under the GNU GPL (v>=3)
 and comes with ABSOLUTELY NO WARRANTY!
 -->
<template>
  <v-checkbox
    :density="props.density"
    :disabled="applicationStore.form?.readonly || props.disabled"
    :hint="props.hint"
    :label="props.label"
    :model-value="
      props.attribute && !props.modelValue
        ? applicationStore.managedItem[props.attribute]
        : props.modelValue
    "
    :name="props.name"
    :persistent-hint="props.persistentHint"
    :readonly="props.readonly"
    :rules="
      applicationStore.clientAndServerRules[props.attribute]
        ? applicationStore.clientAndServerRules[props.attribute]
        : props.rules
    "
    @update:model-value="
      (event) => onUpdateModelValue(event, emit, props.attribute)
    "
  ></v-checkbox>
</template>

<script setup>
import { useApplicationStore } from "@/stores/application";
import { useForm } from "@/lib/use-form";

const { onUpdateModelValue } = useForm();

const applicationStore = useApplicationStore();

const props = defineProps([
  // Vuetify props
  "density",
  "disabled",
  "hint",
  "label",
  "modelValue",
  "name",
  "persistentHint",
  "readonly",
  "rules",

  // Custom props
  "attribute",
]);
const emit = defineEmits(["update:modelValue"]);
</script>
