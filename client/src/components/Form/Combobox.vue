<!--
 Copyright (C) 2024 by Bundesamt fuer Strahlenschutz
 Software engineering by Intevation GmbH

 This file is Free Software under the GNU GPL (v>=3)
 and comes with ABSOLUTELY NO WARRANTY!
 -->
<template>
  <v-combobox
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
      props.attribute && !props.modelValue
        ? applicationStore.managedItem[props.attribute]
        : props.modelValue
    "
    :name="props.name"
    :no-data-text="props.noDataText ?? $t('label.no_data_text')"
    :prepend-inner-icon="props.prependInnerIcon"
    :readonly="applicationStore.form?.readonly || props.readonly"
    :return-object="props.returnObject"
    ref="combobox"
    :rules="[
      ...rules,
      ...(applicationStore.clientAndServerRules[props.attribute]
        ? applicationStore.clientAndServerRules[props.attribute]
        : props.rules ?? []),
    ]"
    :type="props.type ?? 'text'"
    :variant="props.variant ?? 'underlined'"
    @update:model-value="
      (event) => onUpdateModelValue(event, emit, props.attribute)
    "
  ></v-combobox>
</template>

<script setup>
import { computed, inject, ref } from "vue";
import { useApplicationStore } from "@/stores/application.js";
import { useForm } from "@/lib/use-form.js";

const { createRequiredRule, onUpdateModelValue } = useForm();

const applicationStore = useApplicationStore();

const props = defineProps({
  active: Boolean,
  autofocus: Boolean,
  autoSelectFirst: [Boolean, String],
  baseColor: String,
  bgColor: String,
  centerAffix: Boolean,
  chips: Boolean,
  class: String,
  clearable: Boolean,
  clearOnSelect: Boolean,
  closableChips: Boolean,
  color: String,
  counter: [String, Number, Boolean],
  density: String,
  disabled: Boolean,
  hint: String,
  items: Array,
  itemTitle: String,
  itemValue: String,
  label: String,
  modelValue: [Object, String],
  name: String,
  noDataText: String,
  prependInnerIcon: [Object, String],
  persistentHint: Boolean,
  readonly: Boolean,
  returnObject: Boolean,
  rules: Array,
  type: String,
  variant: String,

  // Custom props
  attribute: String,
  required: Boolean,
  updateCallBack: Function,
});

const emit = defineEmits(["update:modelValue"]);
const combobox = ref(null);
const translationCategory = inject("translationCategory");
const rules = ref(
  createRequiredRule(props.required, props.attribute, translationCategory)
);

const validate = () => {
  combobox.value.validate();
};

defineExpose({
  validate,
});

const editable = computed(() => {
  return !props.disabled && !props.readonly && !applicationStore.form?.readonly;
});
</script>
