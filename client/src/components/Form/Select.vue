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
    :clearable="props.clearable && editable"
    :closableChips="props.closableChips"
    :counter="props.counter"
    :counterValue="props.counterValue"
    :density="props.density"
    :disabled="props.disabled"
    :hint="props.hint"
    :items="props.items"
    :item-title="props.itemTitle"
    :item-value="props.itemValue"
    :label="`${props.label}${props.required ? ' *' : ''}`"
    :model-value="
      managedItemIndex !== undefined && props.attribute && !props.modelValue
        ? applicationStore.managedItems[managedItemIndex].item[props.attribute]
        : props.modelValue
    "
    :multiple="props.multiple"
    :name="props.name || props.attribute"
    :no-data-text="props.noDataText"
    :persistent-hint="props.persistentHint"
    :prepend-inner-icon="props.prependInnerIcon"
    :readonly="form?.readonly || props.readonly"
    :return-object="props.returnObject"
    :rules="[
      ...rules,
      ...(clientAndServerRules[props.attribute]
        ? clientAndServerRules[props.attribute]
        : (props.rules ?? [])),
    ]"
    :variant="props.variant"
    @update:model-value="
      (event) => onUpdateModelValue(event, emit, props.attribute)
    "
  ></v-autocomplete>
</template>

<script setup>
import { createRequiredRule } from "@/lib/form-helper";
import { useApplicationStore } from "@/stores/application.js";
import { computed, inject, ref } from "vue";

const { onUpdateModelValue, clientAndServerRules } = inject("useForm");
const managedItemIndex = inject("managedItemIndex");

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
const translationCategory = inject("translationCategory");
const { form } = inject("useForm");
const rules = ref(
  createRequiredRule(props.required, props.attribute, translationCategory)
);

const editable = computed(() => {
  return !props.disabled && !props.readonly && !form?.readonly;
});
</script>
