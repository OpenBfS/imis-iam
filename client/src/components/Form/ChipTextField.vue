<!--
 Copyright (C) 2024 by Bundesamt fuer Strahlenschutz
 Software engineering by Intevation GmbH

 This file is Free Software under the GNU GPL (v>=3)
 and comes with ABSOLUTELY NO WARRANTY!
 -->
<template>
  <v-text-field
    @keydown.enter="addEntry"
    @keydown.delete="onDelete"
    @keydown.tab="onTab"
    v-model="input"
    :clearable="props.clearable && editable"
    :density="props.density ?? 'compact'"
    :disabled="props.disabled"
    :hint="props.hint"
    :label="`${props.label}${props.required ? ' *' : ''}`"
    :name="props.name"
    :persistent-hint="true"
    :persistent-placeholder="entries.length > 0"
    :prepend-inner-icon="props.prependInnerIcon"
    :readonly="applicationStore.form?.readonly || props.readonly"
    :rules="
      applicationStore.clientAndServerRules[props.attribute]
        ? [
            ...applicationStore.clientAndServerRules[props.attribute],
            ...(props.rules ?? []),
            ...internalRules,
          ]
        : [...(props.rules ?? []), ...internalRules]
    "
    :variant="props.variant ?? 'underlined'"
  >
    <template v-slot:append="{ isValid }">
      <v-btn
        ref="plusButton"
        @click="onClickPlusButton"
        size="x-small"
        icon="mdi-plus"
        class="ml-n3"
        min-width="36"
        min-height="36"
        :disabled="!unref(isValid) || input.length === 0"
      ></v-btn>
    </template>
    <v-chip
      v-for="(entry, index) in entries"
      @click:close="() => removeEntry(index)"
      :closable="editable"
      :color="index === indexOfDuplicate ? 'error' : 'default'"
      size="small"
      :key="entry"
    >
      {{ entry }}
    </v-chip>
  </v-text-field>
</template>

<style>
.v-input input {
  min-width: 80pt;
}
</style>

<script setup>
import { computed, onMounted, onUnmounted, ref, unref, watch } from "vue";
import { useApplicationStore } from "@/stores/application.js";
import { useForm } from "@/lib/use-form.js";
import { useI18n } from "vue-i18n";

const { t } = useI18n();
const { areArraysDifferent, onUpdateModelValue } = useForm();

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
  prependInnerIcon: Object,
  readonly: Boolean,
  rules: Array,
  variant: String,

  // Custom props
  attribute: String,
  required: Boolean,
  updateCallBack: Function,
});

const emit = defineEmits(["update:modelValue"]);

const input = ref("");
const plusButton = ref(null);
const entries = ref([]);
const indexOfDuplicate = ref(-1);
const internalRules = ref([
  (v) => {
    const index = entries.value.indexOf(v);
    indexOfDuplicate.value = index;
    if (index === -1) {
      return true;
    } else {
      return t("error.only_unique_values");
    }
  },
]);
let isAdding = false;
const editable = computed(() => {
  return !props.disabled && !props.readonly && !applicationStore.form?.readonly;
});
const resetInput = () => {
  input.value = "";
};
onMounted(() => {
  if (props.attribute && applicationStore.managedItem[props.attribute]) {
    entries.value = applicationStore.managedItem[props.attribute];
  }
  applicationStore.addResetEventListener(resetInput);
});
onUnmounted(() => {
  applicationStore.removeChangeInField(props.attribute);
});

watch(entries, (newEntries) => {
  onUpdateModelValue(newEntries, emit, props.attribute);
});
watch(input, (newInput) => {
  const isContained = applicationStore.attributesOfFieldsThatChanged.includes(
    props.attribute
  );
  if (newInput.length > 0 && !isContained) {
    applicationStore.submitChangeInField(props.attribute);
  } else if (newInput.length === 0 && isContained) {
    applicationStore.removeChangeInField(props.attribute);
  }
});

// We need this watcher as $subscribe doesn't detect all changes if
// there are several at the same time.
watch(
  // Without structuredClone changes are not detected correctly.
  () => structuredClone(applicationStore.managedItem),
  (newValue, oldValue) => {
    const oldEntries = oldValue[props.attribute];
    const newEntries = newValue[props.attribute];
    if (areArraysDifferent(oldEntries ?? [], newEntries ?? [])) {
      entries.value = newEntries;
      input.value = "";
    }
  }
);

const addEntry = () => {
  if (isAdding) return;
  isAdding = true;
  if (input.value === "" || entries.value.indexOf(input.value) !== -1) {
    isAdding = false;
    return;
  }
  let isValid = true;
  props.rules?.forEach((rule) => {
    const result = rule(input.value);
    if (typeof result !== "boolean" || !result) isValid = false;
  });
  if (!isValid) {
    isAdding = false;
    return;
  }
  entries.value = [...entries.value, input.value];
  input.value = "";
  isAdding = false;
};

const removeEntry = (index) => {
  entries.value = entries.value.toSpliced(index, 1);
};

const onDelete = () => {
  if (input.value === "") {
    removeEntry(entries.value.length - 1);
  }
};

const onClickPlusButton = () => {
  addEntry();
  // Re-focus text field so user can continue to enter values
  const index = Array.from(plusButton.value.$el.form).indexOf(
    plusButton.value.$el
  );
  plusButton.value.$el.form[index - 1]?.focus();
};

const onTab = () => {
  addEntry();
};
</script>
