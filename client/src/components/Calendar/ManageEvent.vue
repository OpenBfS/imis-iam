<!--
 Copyright (C) 2023 by Bundesamt fuer Strahlenschutz
 Software engineering by Intevation GmbH

 This file is Free Software under the GNU GPL (v>=3)
 and comes with ABSOLUTELY NO WARRANTY!
 -->
<template>
  <v-card
    width="80vw"
    v-if="['add', 'edit', 'show'].indexOf(processType) !== -1"
  >
    <v-card-title v-if="processType === 'add'">
      <span class="text-h5">{{ $t("calendar.create_title") }}</span>
    </v-card-title>
    <v-card-title v-if="processType === 'edit'">
      <span class="text-h5">
        {{ $t("calendar.edit_title", { title: event.title }) }}
      </span>
    </v-card-title>
    <v-divider></v-divider>
    <v-container class="pa-1 mt-4 mx-2"> </v-container>
    <v-row justify="center">
      <v-col justify="start" cols="11">
        <v-form v-model="valid" ref="form" :readonly="readonly">
          <v-text-field
            variant="underlined"
            density="compact"
            :label="$t('label.title')"
            v-model="event.title"
            :rules="reqField($t('calendar.required_title'))"
          ></v-text-field>
          <v-row>
            <v-col>
              <DatePicker
                :dateUpdatedCallback="startDateUpdatedCallback"
                :date="event.startDate"
                :label="$t('label.from')"
                :readonly="readonly"
              ></DatePicker>
            </v-col>
            <v-col>
              <DatePicker
                :dateUpdatedCallback="endDateUpdatedCallback"
                :date="event.endDate"
                :label="$t('label.to')"
                :readonly="readonly"
              ></DatePicker>
            </v-col>
            <v-col>
              <v-text-field
                variant="underlined"
                density="compact"
                :label="$t('label.site')"
                v-model="event.site"
                :rules="reqField($t('calendar.required_site'))"
              ></v-text-field>
            </v-col>
          </v-row>
          <v-textarea
            variant="underlined"
            :label="$t('label.description')"
            auto-grow
            v-model="event.description"
            :rules="reqField($t('calendar.required_description'))"
          ></v-textarea>
        </v-form>
      </v-col>
    </v-row>
    <v-card-actions>
      <v-spacer></v-spacer>
      <v-btn
        v-if="profileStore.isAllowedToManage && processType !== 'show'"
        color="accent"
        :disabled="!valid || hasNoChange"
        @click="processType == 'add' ? createEvent() : updateEvent()"
      >
        {{ processType == "add" ? $t("button.create") : $t("button.save") }}
      </v-btn>
      <v-btn
        v-if="processType === 'edit' && profileStore.isAllowedToManage"
        color="accent"
        @click="event = { ...originalEvnet }"
      >
        {{ $t("button.reset") }}
      </v-btn>
      <v-btn
        color="accent"
        @click="applicationStore.setShowManageEventDialog(false)"
      >
        {{ $t("button.cancel") }}
      </v-btn>
    </v-card-actions>
  </v-card>
  <v-card v-else width="50vw">
    <v-card-text>
      <span class="text-h5">{{
        $t("label.confirm_deletion", { name: event.title })
      }}</span>
    </v-card-text>
    <v-divider></v-divider>
    <v-card-actions>
      <v-spacer></v-spacer>
      <v-btn color="accent" @click="deleteEvent()">
        {{ $t("button.delete") }}
      </v-btn>
      <v-btn
        color="accent"
        @click="applicationStore.setShowManageEventDialog(false)"
      >
        {{ $t("button.cancel") }}
      </v-btn>
    </v-card-actions>
  </v-card>
</template>
<script setup>
import { HTTP } from "@/lib/http";
import { ref } from "vue";
import { useForm } from "@/lib/use-form";
import { useNotification } from "@/lib/use-notification";
import { useApplicationStore } from "@/stores/application";
import { useEventsStore } from "@/stores/events";
import { useProfileStore } from "@/stores/profile";

const { hasLoadingError, hasRequestError } = useNotification();
const applicationStore = useApplicationStore();
const eventsStore = useEventsStore();
const profileStore = useProfileStore();
const event = ref(eventsStore.managedEvent);
const processType = ref(applicationStore.processType);

const readonly = event.value.readonly || processType.value === "show";

const { form, valid, reqField } = useForm();

const getEvents = () => {
  eventsStore
    .loadEvents()
    .then()
    .catch(() => {
      hasLoadingError.value = true;
    });
};
const createEvent = () => {
  let payload = { ...event.value };
  HTTP.post("/event", payload)
    .then(() => {
      getEvents();
      applicationStore.setShowManageEventDialog(false);
    })
    .catch(() => {
      hasRequestError.value = true;
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
    .catch(() => {
      hasRequestError.value = true;
    });
};
const deleteEvent = () => {
  HTTP.delete("event/" + event.value.id)
    .then(() => {
      getEvents();
      applicationStore.setShowManageEventDialog(false);
    })
    .catch(() => {
      hasRequestError.value = true;
    });
};
const startDateUpdatedCallback = (newDate) => {
  event.value.startDate = newDate;
};
const endDateUpdatedCallback = (newDate) => {
  event.value.endDate = newDate;
};
</script>
