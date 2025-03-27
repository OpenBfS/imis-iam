<!--
 Copyright (C) 2024 by Bundesamt fuer Strahlenschutz
 Software engineering by Intevation GmbH

 This file is Free Software under the GNU GPL (v>=3)
 and comes with ABSOLUTELY NO WARRANTY!
 -->
<template>
  <v-text-field
    :clearable="props.clearable && editable"
    :density="props.density ?? 'compact'"
    :disabled="props.disabled"
    :hint="props.hint"
    :label="`${props.label}${props.required ? ' *' : ''}`"
    :model-value="
      props.attribute && !props.modelValue
        ? applicationStore.managedItem[props.attribute]
        : props.modelValue
    "
    :name="props.name"
    :prepend-inner-icon="props.prependInnerIcon"
    :readonly="applicationStore.form?.readonly || props.readonly"
    ref="textField"
    :rules="[
      ...rules,
      ...(applicationStore.clientAndServerRules[props.attribute]
        ? applicationStore.clientAndServerRules[props.attribute]
        : (props.rules ?? [])),
    ]"
    :type="props.type ?? 'text'"
    :variant="props.variant ?? 'underlined'"
    @update:model-value="
      (event) => onUpdateModelValue(event, emit, props.attribute)
    "
  ></v-text-field>
</template>

<script setup>
import { computed, inject, ref } from "vue";
import { useApplicationStore } from "@/stores/application.js";
import { useForm } from "@/lib/use-form.js";

const { createRequiredRule, onUpdateModelValue } = useForm();

const applicationStore = useApplicationStore();

const props = defineProps({
  class: String,
  clearable: Boolean,
  density: String,
  disabled: Boolean,
  hint: String,
  label: String,
  modelValue: [Object, String],
  name: String,
  prependInnerIcon: [Object, String],
  readonly: Boolean,
  rules: Array,
  type: String,
  variant: String,

  // Custom props
  attribute: String,
  required: Boolean,
  updateCallBack: Function,
});

const emit = defineEmits(["update:modelValue"]);
const translationCategory = inject("translationCategory");
const textField = ref(null);
const rules = ref(
  createRequiredRule(props.required, props.attribute, translationCategory),
);

const validate = () => {
  textField.value.validate();
};

defineExpose({
  validate,
});

const editable = computed(() => {
  return !props.disabled && !props.readonly && !applicationStore.form?.readonly;
});
</script>
