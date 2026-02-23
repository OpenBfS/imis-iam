<!--
 Copyright (C) 2024 by Bundesamt fuer Strahlenschutz
 Software engineering by Intevation GmbH

 This file is Free Software under the GNU GPL (v>=3)
 and comes with ABSOLUTELY NO WARRANTY!
 -->
<template>
  <v-checkbox
    :density="props.density"
    :disabled="props.disabled"
    :hint="props.hint"
    :label="props.label"
    :model-value="
      managedItemIndex !== undefined && props.attribute && !props.modelValue
        ? applicationStore.managedItems[managedItemIndex].item[props.attribute]
        : props.modelValue
    "
    :name="props.name || props.attribute"
    :persistent-hint="props.persistentHint"
    :readonly="form?.readonly || props.readonly"
    :rules="
      clientAndServerRules[props.attribute]
        ? clientAndServerRules[props.attribute]
        : props.rules
    "
    @change="(e) => { emit('change', e) }"
    @update:model-value="
      (event) => onUpdateModelValue(event, emit, props.attribute)
    "
  ></v-checkbox>
</template>

<script setup>
import { useApplicationStore } from "@/stores/application.js";
import { inject } from "vue";

const { onUpdateModelValue, clientAndServerRules } = inject("useForm");
const managedItemIndex = inject("managedItemIndex", undefined);

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
const emit = defineEmits(["change", "update:modelValue"]);

const { form } = inject("useForm");
</script>
