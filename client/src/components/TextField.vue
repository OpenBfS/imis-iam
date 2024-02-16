<!--
 Copyright (C) 2024 by Bundesamt fuer Strahlenschutz
 Software engineering by Intevation GmbH

 This file is Free Software under the GNU GPL (v>=3)
 and comes with ABSOLUTELY NO WARRANTY!
 -->
<template>
  <v-text-field
    :clearable="props.clearable"
    :density="props.density ?? 'compact'"
    :disabled="props.disabled"
    :hint="props.hint"
    :label="props.label"
    :model-value="props.modelValue"
    :name="props.name"
    :prepend-inner-icon="props.prependInnerIcon"
    :readonly="props.readonly"
    :rules="props.rules"
    :variant="props.variant ?? 'underlined'"
    @update:model-value="emit('update:modelValue', $event)"
  ></v-text-field>
</template>

<script setup>
const props = defineProps([
  "class",
  "clearable",
  "density",
  "disabled",
  "hint",
  "label",
  "modelValue",
  "name",
  "prependInnerIcon",
  "readonly",
  "rules",
  "updateCallback",
  "variant",
]);
const emit = defineEmits(["update:modelValue"]);
</script>
