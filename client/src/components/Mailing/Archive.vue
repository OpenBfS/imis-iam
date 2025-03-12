<!--
 Copyright (C) 2022 by Bundesamt fuer Strahlenschutz
 Software engineering by Intevation GmbH

 This file is Free Software under the GNU GPL (v>=3)
 and comes with ABSOLUTELY NO WARRANTY!
 -->
<template>
  <v-container>
    <UIHeader>
      {{ $t("archive.title") }}
    </UIHeader>
    <v-row class="mt-6" align="center">
      <v-autocomplete
        :no-data-text="$t('label.noDataText')"
        class="mx-1"
        style="max-width: 20%"
        v-model="selectedFilter"
        :items="mailTypes"
        item-title="name"
        item-value="id"
        return-object
        multiple
        density="compact"
        :hint="$t('emails.filterByType')"
        persistent-hint
      ></v-autocomplete>
      <div class="d-flex mx-3 flex-column" style="width: 20%">
        <div style="position: relative">
          <div id="startDateTextfield">
            <v-text-field
              v-model="startDateString"
              clearable
              prepend-inner-icon="mdi-calendar-blank"
              :hint="$t('hints.dateFormat')"
              :label="$t('label.from')"
              :rules="validGermanDate()"
              @click="isStartDatePickerOpen = true"
              @click:clear="handleClearStartDateTextfield"
              @input="handleInputForStartDate"
            ></v-text-field>
          </div>
          <v-date-picker
            v-click-outside="{
              handler: toggleStartDatePicker,
              closeConditional: startDateCloseConditional,
              include: includeStartDatePicker,
            }"
            v-model="startDate"
            v-show="isStartDatePickerOpen"
            className="startDatePicker"
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
            :title="$t('label.from')"
            @update:modelValue="handleStartDateUpdate"
          >
            <template v-slot:header>
              <div class="v-date-picker-header bg-accent">
                <div class="v-date-picker-header__content">
                  {{ $d(startDate, "short") }}
                </div>
              </div>
            </template>
          </v-date-picker>
        </div>
        <div style="position: relative">
          <div id="endDateTextfield">
            <v-text-field
              v-model="endDateString"
              clearable
              prepend-inner-icon="mdi-calendar-blank"
              :hint="$t('hints.dateFormat')"
              :label="$t('label.to')"
              :rules="validGermanDate()"
              @click="isEndDatePickerOpen = true"
              @click:clear="handleClearEndDateTextfield"
              @input="handleInputForEndDate"
            ></v-text-field>
          </div>
          <v-date-picker
            v-click-outside="{
              handler: toggleEndDatePicker,
              closeConditional: endDateCloseConditional,
              include: includeEndDatePicker,
            }"
            v-model="endDate"
            v-show="isEndDatePickerOpen"
            className="endDatePicker"
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
            :title="$t('label.to')"
            @update:modelValue="handleEndDateUpdate"
          >
            <template v-slot:header>
              <div class="v-date-picker-header bg-accent">
                <div class="v-date-picker-header__content">
                  {{ $d(endDate, "short") }}
                </div>
              </div>
            </template>
          </v-date-picker>
        </div>
      </div>
      <v-text-field
        density="compact"
        class="mx-1"
        variant="underlined"
        style="max-width: 20%"
        :label="$t('emails.filterBySender')"
        v-model="sender"
      ></v-text-field>
    </v-row>
    <v-row>
      <v-col cols="12" class="mt-6">
        <v-table class="pa-2 ma-2" density="compact">
          <thead>
            <tr>
              <th class="text-left">{{ $t("emails.subject") }}</th>
              <th class="text-left">{{ $t("emails.sender") }}</th>
              <th class="text-left">{{ $t("emails.date") }}</th>
            </tr>
          </thead>
          <tbody>
            <tr
              v-for="mail in mails"
              :key="mail.id"
              @click="
                selectedMail = mail;
                showMailContent = true;
              "
            >
              <td>{{ mail.subject }}</td>
              <td>{{ mail.sender }}</td>
              <td>{{ new Date(mail.sendDate).toLocaleDateString() }}</td>
            </tr>
            <tr v-if="mails && !mails.length">
              <td colspan="3">{{ $t("emails.noMailsAvailable") }}</td>
            </tr>
          </tbody>
        </v-table>

        <UIAlert
          v-if="hasLoadingError"
          v-bind:isSuccessful="!hasLoadingError"
          v-bind:message="applicationStore.httpErrorMessage"
        />
      </v-col>
    </v-row>
  </v-container>
  <MailContent
    v-if="showMailContent"
    v-bind:mail="selectedMail"
    @child-object="checkChildObject"
  />
</template>
<style scoped>
tr {
  cursor: pointer;
}
.v-table > .v-table__wrapper > table > tbody > tr:hover {
  background-color: darkgray;
}
</style>
<script setup>
import { onMounted, ref, computed, watch } from "vue";
import { HTTP } from "@/lib/http.js";
import { useForm } from "@/lib/use-form.js";
import { useNotification } from "@/lib/use-notification.js";
import { useApplicationStore } from "@/stores/application.js";
import { useMailStore } from "@/stores/mail.js";
import { useRoute } from "vue-router";
import debounce from "debounce";
import { useI18n } from "vue-i18n";

const {
  dateStringToDate,
  validGermanDate,
  doesRegexMatchWholeString,
  germanDateRegex,
} = useForm();
const { d } = useI18n();
const startDate = ref(new Date());
const startDateString = ref("");
const endDate = ref(new Date());
const endDateString = ref("");
const isStartDatePickerOpen = ref(false);
const isEndDatePickerOpen = ref(false);
const mails = ref([]);
const applicationStore = useApplicationStore();
const mailStore = useMailStore();
const route = useRoute();
const { hasLoadingError } = useNotification();
const toggleStartDatePicker = () => {
  isStartDatePickerOpen.value = !isStartDatePickerOpen.value;
};
const handleInputForStartDate = (event) => {
  const input = event.target.value;
  if (doesRegexMatchWholeString(germanDateRegex, input)) {
    const newDate = dateStringToDate(input);
    if (newDate) {
      startDate.value = newDate;
    }
  }
};
const handleStartDateUpdate = (event) => {
  startDateString.value = d(event, "short");
};
const handleClearStartDateTextfield = () => {
  isStartDatePickerOpen.value = false;
  getMails();
};
const toggleEndDatePicker = () => {
  isEndDatePickerOpen.value = !isEndDatePickerOpen.value;
};
const handleInputForEndDate = (event) => {
  const input = event.target.value;
  if (doesRegexMatchWholeString(germanDateRegex, input)) {
    const newDate = dateStringToDate(input);
    if (newDate) {
      endDate.value = newDate;
    }
  }
};
const handleEndDateUpdate = (event) => {
  endDateString.value = d(event, "short");
};
const handleClearEndDateTextfield = () => {
  isEndDatePickerOpen.value = false;
  getMails();
};
const startDateCloseConditional = () => {
  return isStartDatePickerOpen.value;
};
const endDateCloseConditional = () => {
  return isEndDatePickerOpen.value;
};
const getIncludedElements = (selector) => {
  const elements = document.querySelectorAll(selector);
  const includedElements = [];
  for (let i = 0; i < elements.length; i++) {
    includedElements.push(elements[i]);
  }
  return includedElements;
};
const includeStartDatePicker = () => {
  return getIncludedElements(".startDatePicker *, #startDateTextfield *");
};
const includeEndDatePicker = () => {
  return getIncludedElements(".endDatePicker *, #endDateTextfield *");
};
const getMails = () => {
  if (!startDate.value) return;
  let date = "";

  if (startDateString.value?.length > 0) {
    const tmpStartDate = new Date(Date.parse(startDate.value));
    tmpStartDate.setHours(0, 0, 0, 0);
    if (startDate.value) {
      date += `start=${tmpStartDate.toISOString()}`;
    }
  }

  if (endDateString.value?.length > 0) {
    const tmpEndDate = new Date(Date.parse(endDate.value));
    tmpEndDate.setHours(23, 59, 59, 999);
    if (endDate.value) {
      date = date != "" ? date + "&" : date;
      date += `end=${tmpEndDate.toISOString()}`;
    }
  }

  date = date === "" ? date : "&" + date;

  let payload =
    date === "" ? "mail?archived=true" : "mail?archived=true" + date;
  if (sender.value) {
    payload += "&sender=" + sender.value;
  }
  if (selectedFilter.value && selectedFilter.value.length) {
    selectedFilter.value.forEach((t) => {
      payload += "&type=" + t.id;
    });
  }
  HTTP.get(payload)
    .then((response) => {
      mails.value = response.data;
    })
    .catch(() => {
      hasLoadingError.value = true;
    });
};
onMounted(() => {
  setStartAndEndDate();
  getMails();
  mailStore
    .loadMailTypes()
    .then()
    .catch(() => {
      hasLoadingError.value = true;
    });
});
const selectedMail = ref();
const showMailContent = ref(false);
const checkChildObject = (e) => {
  if (e.closeDialog) {
    showMailContent.value = false;
  }
};
// Filters
const mailTypes = computed(() => {
  return mailStore.mailTypes;
});
const selectedFilter = ref([]);
watch(
  () => selectedFilter.value,
  () => {
    getMails();
  }
);
const currentYear = new Date().getFullYear();

const setStartAndEndDate = () => {
  if (route.params.year !== "all") {
    let newStartDate, newEndDate;
    switch (Number(route.params.year)) {
      case currentYear:
        newStartDate = new Date(currentYear + "-01-01");
        newEndDate = new Date(currentYear + "-12-31");
        break;
      case currentYear - 1:
        newStartDate = new Date(currentYear - 1 + "-01-01");
        newEndDate = new Date(currentYear - 1 + "-12-31");
        break;
      case currentYear - 2:
        newStartDate = new Date(currentYear - 2 + "-01-01");
        newEndDate = new Date(currentYear - 2 + "-12-31");
        break;
    }
    startDate.value = newStartDate;
    handleStartDateUpdate(newStartDate);
    endDate.value = newEndDate;
    handleEndDateUpdate(newEndDate);
  }
};
watch(
  () => route.params,
  (previousParams, toParams) => {
    if (toParams.year !== previousParams.year) {
      setStartAndEndDate(route.params.year);
      getMails();
    }
  }
);
watch([() => endDate.value, () => startDate.value], () => {
  getMails();
});
const sender = ref("");
const triggerFilter = debounce(() => {
  getMails();
}, 500);
watch(
  () => sender.value,
  (oldValue, newValue) => {
    if (oldValue !== newValue) {
      triggerFilter();
    }
  }
);
</script>
