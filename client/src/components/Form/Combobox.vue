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
      managedItemIndex !== undefined && props.attribute && !props.modelValue
        ? applicationStore.managedItems[managedItemIndex].item[props.attribute]
        : props.modelValue
    "
    :name="props.name || props.attribute"
    :no-data-text="props.noDataText ?? $t('label.noDataText')"
    :prepend-inner-icon="props.prependInnerIcon"
    :readonly="form?.readonly || props.readonly"
    :return-object="props.returnObject"
    ref="combobox"
    :rules="[
      ...rules,
      ...(clientAndServerRules[props.attribute]
        ? clientAndServerRules[props.attribute]
        : (props.rules ?? [])),
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

const { createRequiredRule } = useForm();

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
const { form } = inject("useForm");
const managedItemIndex = inject("managedItemIndex");
const combobox = ref(null);
const translationCategory = inject("translationCategory");
const { onUpdateModelValue, clientAndServerRules } = inject("useForm");
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
  return !props.disabled && !props.readonly && !form?.readonly;
});
</script>
