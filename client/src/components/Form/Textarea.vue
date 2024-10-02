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
    :disabled="applicationStore.form?.readonly || props.disabled"
    :hint="props.hint"
    :label="`${props.label}${props.required ? ' *' : ''}`"
    :model-value="
      props.attribute && !props.modelValue
        ? applicationStore.managedItem[props.attribute]
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
    @update:model-value="
      (event) => onUpdateModelValue(event, emit, props.attribute)
    "
  ></v-textarea>
</template>

<script setup>
import { useApplicationStore } from "@/stores/application";
import { useForm } from "@/lib/use-form";

const { onUpdateModelValue } = useForm();

const applicationStore = useApplicationStore();

const props = defineProps({
  active: Boolean,
  autoGrow: Boolean,
  clearable: Boolean,
  counter: Number,
  counterValue: Function,
  density: String,
  disabled: Boolean,
  hint: String,
  label: String,
  modelValue: [Object, String],
  name: String,
  prependInnerIcon: [Object, String],
  readonly: Boolean,
  rules: Array,
  variant: String,

  // Custom props
  attribute: String,
  required: Boolean,
  updateCallBack: Function,
});

const emit = defineEmits(["update:modelValue"]);
</script>
