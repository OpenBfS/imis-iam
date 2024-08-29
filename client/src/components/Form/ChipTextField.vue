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
    @update:focused="onFocus"
    v-model="input"
    :clearable="props.clearable"
    :density="props.density ?? 'compact'"
    :disabled="props.disabled"
    :hint="props.hint"
    :label="`${props.label}${props.required ? ' *' : ''}`"
    :name="props.name"
    :persistent-hint="true"
    :persistent-placeholder="entries.length > 0"
    :prepend-inner-icon="props.prependInnerIcon"
    :readonly="props.readonly"
    :rules="
      applicationStore.clientAndServerRules[props.attribute]
        ? [
            ...applicationStore.clientAndServerRules[props.attribute],
            ...props.rules,
            ...rules,
          ]
        : [...props.rules, ...rules]
    "
    :variant="props.variant ?? 'underlined'"
  >
    <template v-slot:append="{ isValid }">
      <v-btn
        ref="plusButton"
        @click="addEntry"
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
      closable
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
import { onMounted, onUnmounted, ref, unref, watch } from "vue";
import { useApplicationStore } from "@/stores/application";
import { useForm } from "@/lib/use-form";
import { useI18n } from "vue-i18n";

const { t } = useI18n();
const { onUpdateModelValue } = useForm();

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
const rules = ref([
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

onMounted(() => {
  if (props.attribute && applicationStore.managedItem[props.attribute]) {
    entries.value = applicationStore.managedItem[props.attribute];
  }
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

applicationStore.$subscribe((mutation) => {
  const key = mutation.events.key;
  if (key === props.attribute) {
    // Mutations with the same key can also occur when the rules in the application store are
    // updated so we need to ensure that we don't get an array with rules (functions).
    if (
      mutation.events.newValue[0] &&
      typeof mutation.events.newValue[0] === "function"
    )
      return;
    entries.value = mutation.events.newValue;
    input.value = "";
  } else if (key === "attributesOfFieldsThatChanged") {
    if (
      !applicationStore.attributesOfFieldsThatChanged.includes(props.attribute)
    ) {
      input.value = "";
    }
  }
});

const addEntry = (moveToNextElement = false) => {
  if (isAdding) return;
  isAdding = true;
  if (input.value === "" || entries.value.indexOf(input.value) !== -1) return;
  let isValid = true;
  props.rules.forEach((rule) => {
    const result = rule(input.value);
    if (typeof result !== "boolean" || !result) isValid = false;
  });
  if (!isValid) return;
  entries.value = [...entries.value, input.value];
  if (moveToNextElement) {
    const index = Array.from(plusButton.value.$el.form).indexOf(
      plusButton.value.$el
    );
    plusButton.value.$el.form[index + 1]?.focus();
  }
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

const onFocus = (event) => {
  if (!event) addEntry();
};

const onTab = () => {
  addEntry(true);
};
</script>
