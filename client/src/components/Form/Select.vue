<!--
 Copyright (C) 2024 by Bundesamt fuer Strahlenschutz
 Software engineering by Intevation GmbH

 This file is Free Software under the GNU GPL (v>=3)
 and comes with ABSOLUTELY NO WARRANTY!
 -->
<template>
  <v-select
    :active="props.active"
    :chips="props.chips"
    :clearable="props.clearable"
    :closableChips="props.closableChips"
    :counter="props.counter"
    :counterValue="props.counterValue"
    :density="props.density"
    :disabled="props.disabled"
    :hint="props.hint"
    :items="props.items"
    :item-title="props.itemTitle"
    :item-value="props.itemValue"
    :label="props.label"
    :model-value="
      props.attribute && !props.modelValue
        ? applicationStore.managedItem[props.attribute]
        : props.modelValue
    "
    :multiple="props.multiple"
    :name="props.name"
    :no-data-text="props.noDataText"
    :persistent-hint="props.persistentHint"
    :prepend-inner-icon="props.prependInnerIcon"
    :readonly="props.readonly"
    :return-object="props.returnObject"
    :rules="
      applicationStore.clientAndServerRules[props.attribute]
        ? applicationStore.clientAndServerRules[props.attribute]
        : props.rules
    "
    :variant="props.variant"
    @update:model-value="
      (event) => onUpdateModelValue(event, emit, props.attribute)
    "
  ></v-select>
</template>

<script setup>
import { useApplicationStore } from "@/stores/application";
import { useForm } from "@/lib/use-form";

const { onUpdateModelValue } = useForm();

const applicationStore = useApplicationStore();

const props = defineProps([
  // Vuetify props
  "active",
  "chips",
  "clearable",
  "closableChips",
  "counter",
  "counterValue",
  "density",
  "disabled",
  "hint",
  "items",
  "itemTitle",
  "itemValue",
  "label",
  "modelValue",
  "multiple",
  "name",
  "noDataText",
  "persistentHint",
  "prependInnerIcon",
  "readonly",
  "returnObject",
  "rules",
  "variant",

  // Custom props
  "attribute",
]);
const emit = defineEmits(["update:modelValue"]);
</script>
