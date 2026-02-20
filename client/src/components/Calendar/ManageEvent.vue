<!--
 Copyright (C) 2023 by Bundesamt fuer Strahlenschutz
 Software engineering by Intevation GmbH

 This file is Free Software under the GNU GPL (v>=3)
 and comes with ABSOLUTELY NO WARRANTY!
 -->
<template>
  <v-card
    width="80vw"
    min-height="500pt"
    v-if="[PROCESS_TYPE.ADD, PROCESS_TYPE.EDIT, PROCESS_TYPE.SHOW].indexOf(processType) !== -1"
  >
    <v-card-title v-if="processType === PROCESS_TYPE.ADD">
      <span class="text-h5">{{ $t("calendar.createTitle") }}</span>
    </v-card-title>
    <v-card-title v-if="processType === PROCESS_TYPE.EDIT">
      <span class="text-h5">
        {{ $t("calendar.editTitle", { title: event.title }) }}
      </span>
    </v-card-title>
    <v-divider></v-divider>
    <v-container class="pa-1 mt-4 mx-2"> </v-container>
    <v-row justify="center">
      <v-col cols="11">
        <v-form v-model="valid" ref="form" :readonly="readonly">
          <TextField
            :label="$t('label.title')"
            :attribute="'title'"
            @update:modelValue="event.title = $event"
          ></TextField>
          <v-row>
            <v-col>
              <DatePicker
                :required="true"
                :prefill="true"
                :dateUpdatedCallback="startDateUpdatedCallback"
                :date="event.startDate"
                :label="$t('label.from')"
                :readonly="readonly"
              ></DatePicker>
            </v-col>
            <v-col>
              <DatePicker
                :required="true"
                :prefill="true"
                :dateUpdatedCallback="endDateUpdatedCallback"
                :date="event.endDate"
                :label="$t('label.to')"
                :readonly="readonly"
              ></DatePicker>
            </v-col>
            <v-col>
              <TextField
                :label="$t('label.site')"
                :attribute="'site'"
                @update:modelValue="event.site = $event"
              ></TextField>
            </v-col>
          </v-row>
          <Textarea
            :label="$t('label.description')"
            auto-grow
            :attribute="'description'"
            @update:modelValue="event.description = $event"
          ></Textarea>
        </v-form>
      </v-col>
    </v-row>
    <v-card-actions>
      <v-spacer></v-spacer>
      <v-btn
        v-if="profileStore.isAllowedToManage && processType !== PROCESS_TYPE.SHOW"
        color="accent"
        :disabled="!valid || hasNoChange"
        @click="processType == PROCESS_TYPE.ADD ? createEvent() : updateEvent()"
      >
        {{ processType == PROCESS_TYPE.ADD ? $t("button.create") : $t("button.save") }}
      </v-btn>
      <v-btn
        v-if="processType === PROCESS_TYPE.EDIT && profileStore.isAllowedToManage"
        color="accent"
        :disabled="hasNoChange"
        @click="resetForm(originalEvent, event, resetNotification)"
      >
        {{ $t("button.reset") }}
      </v-btn>
      <v-btn
        color="accent"
        @click="
          onCancel(() => applicationStore.setShowManageEventDialog(false))
        "
      >
        {{ $t("button.cancel") }}
      </v-btn>
    </v-card-actions>
  </v-card>
  <ConfirmCancelDialog
    :isActive="showConfirmCancelDialog"
    :onConfirm="() => applicationStore.setShowManageEventDialog(false)"
    :onCancel="() => closeConfirmCancelDialog()"
  ></ConfirmCancelDialog>
</template>
<script setup>
import { HTTP } from "@/lib/http.js";
import { onBeforeMount, ref } from "vue";
import { useForm } from "@/lib/use-form.js";
import { useNotification } from "@/lib/use-notification.js";
import { PROCESS_TYPE, useApplicationStore } from "@/stores/application.js";
import { useEventsStore } from "@/stores/events.js";
import { useProfileStore } from "@/stores/profile.js";
import Textarea from "@/components/Form/Textarea.vue";
import TextField from "@/components/Form/TextField.vue";
import ConfirmCancelDialog from "@/components/ConfirmCancelDialog.vue";
import { useI18n } from "vue-i18n";

const { t } = useI18n();

const props = defineProps(["index"]);

const { hasRequestError, resetNotification } = useNotification();
const applicationStore = useApplicationStore();
const eventsStore = useEventsStore();
const profileStore = useProfileStore();

const managedItem = applicationStore.managedItems[props.index];
const event = ref(managedItem.item);
const originalEvent = ref(managedItem.originalItem);
const processType = ref(applicationStore.processType);

const readonly = event.value.readonly || processType.value === PROCESS_TYPE.SHOW;

const {
  form,
  valid,
  hasNoChange,
  reqField,
  resetForm,
  onCancel,
  showConfirmCancelDialog,
  closeConfirmCancelDialog,
  initClientRules,
  handleValidationErrorFromServer,
  isServerValidationError,
} = useForm(originalEvent, event);
onBeforeMount(() => {
  initClientRules({
    description: reqField(t("calendar.requiredDescription")),
    site: reqField(t("calendar.requiredSite")),
    title: reqField(t("calendar.requiredTitle")),
  });
});

const createEvent = () => {
  let payload = { ...event.value };
  payload.startDate = payload.startDate.toISOString();
  payload.endDate = payload.endDate.toISOString();
  HTTP.post("/iam/event", payload)
    .then((response) => {
      eventsStore.addEvent(response.data);
      applicationStore.setShowManageEventDialog(false);
    })
    .catch((error) => {
      isServerValidationError(error)
        ? handleValidationErrorFromServer(error.response.data)
        : (hasRequestError.value = true);
    });
};
const updateEvent = () => {
  let payload = { ...event.value };
  delete payload.date;
  eventsStore
    .updateEvent(payload)
    .then(() => {
      applicationStore.setShowManageEventDialog(false);
    })
    .catch((error) => {
      isServerValidationError(error)
        ? handleValidationErrorFromServer(error.response.data)
        : (hasRequestError.value = true);
    });
};

const startDateUpdatedCallback = (newDate) => {
  event.value.startDate = newDate;
};
const endDateUpdatedCallback = (newDate) => {
  event.value.endDate = newDate;
};
</script>
