<!--
 Copyright (C) 2024 by Bundesamt fuer Strahlenschutz
 Software engineering by Intevation GmbH

 This file is Free Software under the GNU GPL (v>=3)
 and comes with ABSOLUTELY NO WARRANTY!
 -->
<template>
  <v-autocomplete
    :active="props.active"
    :chips="props.chips"
    :clearable="props.clearable"
    :closableChips="props.closableChips"
    :counter="props.counter"
    :counterValue="props.counterValue"
    :density="props.density"
    :disabled="applicationStore.form?.readonly || props.disabled"
    :hint="props.hint"
    :items="props.items"
    :item-title="props.itemTitle"
    :item-value="props.itemValue"
    :label="`${props.label}${props.required ? ' *' : ''}`"
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
  ></v-autocomplete>
</template>

<script setup>
import { useApplicationStore } from "@/stores/application";
import { useForm } from "@/lib/use-form";

const { onUpdateModelValue } = useForm();

const applicationStore = useApplicationStore();

const props = defineProps({
  active: Boolean,
  chips: Boolean,
  clearable: Boolean,
  closableChips: Boolean,
  counter: Number,
  counterValue: Function,
  density: String,
  disabled: Boolean,
  hint: String,
  items: Array,
  itemTitle: String,
  itemValue: [Object, String],
  label: String,
  modelValue: [Object, String],
  multiple: Boolean,
  name: String,
  noDataText: String,
  persistentHint: String,
  prependInnerIcon: [Object, String],
  readonly: Boolean,
  returnObject: Boolean,
  rules: Array,
  variant: String,

  // Custom props
  attribute: String,
  required: Boolean,
  updateCallBack: Function,
});

const emit = defineEmits(["update:modelValue"]);
</script>
