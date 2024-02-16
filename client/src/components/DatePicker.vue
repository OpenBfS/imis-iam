<!--
 Copyright (C) 2023 by Bundesamt fuer Strahlenschutz
 Software engineering by Intevation GmbH

 This file is Free Software under the GNU GPL (v>=3)
 and comes with ABSOLUTELY NO WARRANTY!
 -->
<template>
  <div style="position: relative">
    <div :id="`date-textfield-${id}`">
      <v-text-field
        ref="dateTextfield"
        v-model="dateString"
        :clearable="!props.readonly"
        prepend-inner-icon="mdi-calendar-blank"
        :hint="!props.readonly ? $t('hints.date_format') : ''"
        :label="label"
        :readonly="props.readonly"
        :rules="rules"
        @click="
          if (!props.readonly) {
            isDatePickerOpen = true;
          }
        "
        @click:clear="handleClearDateTextfield"
        @input="handleInputForDate"
      ></v-text-field>
    </div>
    <v-expand-transition>
      <v-date-picker
        v-click-outside="{
          handler: toggleDatePicker,
          closeConditional: dateCloseConditional,
          include: includedElements,
        }"
        :hide-header="true"
        :model-value="date"
        v-show="isDatePickerOpen"
        :id="`date-picker-${id}`"
        landscape
        color="accent"
        elevation="6"
        position="absolute"
        style="
          position: absolute;
          top: 70pt;
          z-index: 20;
          background-color: white;
          box-shadow: 0pt 0pt 8pt 4pt rgba(20, 20, 20, 0.2);
        "
        :show-adjacent-months="true"
        :title="label"
        @update:modelValue="handleDateUpdate"
      ></v-date-picker>
    </v-expand-transition>
  </div>
</template>

<script setup>
import { computed, onMounted, ref, watch } from "vue";
import { useForm } from "@/lib/use-form";
import { useI18n } from "vue-i18n";

const props = defineProps([
  "dateUpdatedCallback",
  "date",
  "label",
  "prefill",
  "readonly",
  "required",
]);

const {
  dateStringToDate,
  validGermanDate,
  doesRegexMatchWholeString,
  reqField,
  germanDateRegex,
} = useForm();
const { d, t } = useI18n();
const id = ref();
const date = ref(new Date());
const dateString = ref(null);
const isDatePickerOpen = ref(false);
const dateTextfield = ref(null);

onMounted(() => {
  id.value = Math.floor(Math.random() * 1000000).toString();
  if (props.date) {
    dateString.value = d(new Date(props.date));
    date.value = new Date(props.date);
  } else if (props.prefill) {
    handleDateUpdate(date.value);
  }
});

const rules = computed(() => {
  const tmpRules = validGermanDate();
  if (props.required) {
    tmpRules.push(...reqField(t("calendar.required_date")));
  }
  return tmpRules;
});

const toggleDatePicker = () => {
  isDatePickerOpen.value = !isDatePickerOpen.value;
};
const handleInputForDate = (event) => {
  const input = event.target.value;
  if (doesRegexMatchWholeString(germanDateRegex, input)) {
    const newDate = dateStringToDate(input);
    if (newDate) {
      props.dateUpdatedCallback(newDate);
    }
  }
};
const handleDateUpdate = (event) => {
  date.value = event;
  setDateString(event);
  props.dateUpdatedCallback(event);
};
const setDateString = (date) => {
  dateString.value = d(date, "short");
};
const handleClearDateTextfield = () => {
  isDatePickerOpen.value = false;
  dateString.value = null;
  // Setting the value of the text field to null doesn't
  // trigger validation so we do it ourself.
  dateTextfield.value.validate();
};
const dateCloseConditional = () => {
  return isDatePickerOpen.value;
};
const includedElements = () => {
  const elements = document.querySelectorAll(
    `#date-textfield-${id.value} *, #date-picker-${id.value} *`
  );
  const includedElements = [];
  for (let i = 0; i < elements.length; i++) {
    includedElements.push(elements[i]);
  }
  return includedElements;
};

// Need to watch to changes made by parent.
watch(
  () => props.date,
  (newDate) => {
    setDateString(newDate);
    date.value = newDate;
  }
);
</script>
