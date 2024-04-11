<!--
 Copyright (C) 2024 by Bundesamt fuer Strahlenschutz
 Software engineering by Intevation GmbH

 This file is Free Software under the GNU GPL (v>=3)
 and comes with ABSOLUTELY NO WARRANTY!
 -->
<template>
  <v-textarea
    :active="props.active"
    :auto-grow="props.autoGrow"
    :clearable="props.clearable"
    :counter="props.counter"
    :counterValue="props.counterValue"
    :density="props.density"
    :disabled="props.disabled"
    :hint="props.hint"
    :label="props.label"
    :model-value="
      props.attribute && !props.modelValue
        ? managedItem[props.attribute]
        : props.modelValue
    "
    :name="props.name"
    :persistent-hint="props.persistentHint"
    :prepend-inner-icon="props.prependInnerIcon"
    :readonly="props.readonly"
    :rules="
      applicationStore.clientAndServerRules[props.attribute]
        ? applicationStore.clientAndServerRules[props.attribute]
        : props.rules
    "
    :variant="props.variant"
    @update:model-value="onUpdateModelValue"
  ></v-textarea>
</template>

<script setup>
import { ref } from "vue";
import { useApplicationStore } from "@/stores/application";

const applicationStore = useApplicationStore();

const props = defineProps([
  // Vuetify props
  "active",
  "autoGrow",
  "clearable",
  "counter",
  "counterValue",
  "density",
  "disabled",
  "hint",
  "label",
  "modelValue",
  "name",
  "persistentHint",
  "prependInnerIcon",
  "readonly",
  "rules",
  "variant",

  // Custom props
  "attribute",
]);
const emit = defineEmits(["update:modelValue"]);

const managedItem = ref(applicationStore.managedItem);

const onUpdateModelValue = (event) => {
  if (applicationStore.clientAndServerRules) {
    applicationStore.clearValidationError(props.attribute);
  }
  emit("update:modelValue", event);
};
</script>
