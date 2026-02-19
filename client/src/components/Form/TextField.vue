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
      managedItemIndex !== undefined && props.attribute && !props.modelValue
        ? applicationStore.managedItems[managedItemIndex].item[props.attribute]
        : props.modelValue
    "
    :name="props.name || props.attribute"
    :prepend-inner-icon="props.prependInnerIcon"
    :readonly="form?.readonly || props.readonly"
    ref="textField"
    :rules="[
      ...rules,
      ...(clientAndServerRules[props.attribute]
        ? clientAndServerRules[props.attribute]
        : (props.rules ?? [])),
    ]"
    :type="props.type ?? 'text'"
    :variant="props.variant ?? 'underlined'"
    @blur="emit('blur')"
    @update:model-value="
      (event) => onUpdateModelValue(event, emit, props.attribute)
    "
  ></v-text-field>
</template>

<script setup>
import { computed, inject, ref } from "vue";
import { useApplicationStore } from "@/stores/application.js";
import { useForm } from "@/lib/use-form.js";

const { createRequiredRule } = useForm();
const { onUpdateModelValue, clientAndServerRules } = inject("useForm");
const managedItemIndex = inject("managedItemIndex");

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

const emit = defineEmits(["blur", "update:modelValue"]);
const translationCategory = inject("translationCategory");
const { form } = inject("useForm");
const textField = ref(null);
const rules = ref(
  createRequiredRule(props.required, props.attribute, translationCategory)
);

const validate = () => {
  textField.value.validate();
};

defineExpose({
  validate,
});

const editable = computed(() => {
  return !props.disabled && !props.readonly && !form?.readonly;
});
</script>
