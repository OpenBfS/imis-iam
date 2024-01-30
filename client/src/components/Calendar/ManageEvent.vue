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
      <v-col cols="11">
        <v-form v-model="valid" ref="form" :readonly="readonly">
          <TextField
            :label="$t('label.title')"
            :modelValue="event.title"
            :rules="reqField($t('calendar.required_title'))"
            @update:modelValue="event.title = $event"
          ></TextField>
          <v-row>
            <v-col>
              <DatePicker
                :required="true"
                :dateUpdatedCallback="startDateUpdatedCallback"
                :date="event.startDate"
                :label="$t('label.from')"
                :readonly="readonly"
              ></DatePicker>
            </v-col>
            <v-col>
              <DatePicker
                :required="true"
                :dateUpdatedCallback="endDateUpdatedCallback"
                :date="event.endDate"
                :label="$t('label.to')"
                :readonly="readonly"
              ></DatePicker>
            </v-col>
            <v-col>
              <TextField
                :label="$t('label.site')"
                :modelValue="event.site"
                :rules="reqField($t('calendar.required_site'))"
                @update:modelValue="event.site = $event"
              ></TextField>
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
        v-if="$store.state.profile.isAllowedToManage && processType !== 'show'"
        color="accent"
        :disabled="!valid || hasNoChange"
        @click="processType == 'add' ? createEvent() : updateEvent()"
      >
        {{ processType == "add" ? $t("button.create") : $t("button.save") }}
      </v-btn>
      <v-btn
        v-if="processType === 'edit' && $store.state.profile.isAllowedToManage"
        color="accent"
        :disabled="hasNoChange"
        @click="resetForm(originalEvent, event)"
      >
        {{ $t("button.reset") }}
      </v-btn>
      <v-btn
        color="accent"
        @click="$store.commit('application/setShowManageEventDialog', false)"
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
import { useStore } from "vuex";
import TextField from "@/components/TextField.vue";

const { hasRequestError } = useNotification();
const store = useStore();
const event = ref(store.state.events.managedEvent);
const originalEvent = { ...event.value };
const processType = ref(store.state.application.processType);

const readonly = event.value.readonly || processType.value === "show";

const { form, valid, reqField, resetForm, hasNoChangeWrapper } = useForm();

const createEvent = () => {
  let payload = { ...event.value };
  payload.startDate = payload.startDate.toISOString();
  payload.endDate = payload.endDate.toISOString();
  HTTP.post("/event", payload)
    .then((response) => {
      store.commit("events/addEvent", response.data);
      store.commit("application/setShowManageEventDialog", false);
    })
    .catch(() => {
      hasRequestError.value = true;
    });
};
const updateEvent = () => {
  let payload = { ...event.value };
  delete payload.date;
  store
    .dispatch("events/updateEvent", payload)
    .then(() => {
      store.commit("application/setShowManageEventDialog", false);
    })
    .catch(() => {
      hasRequestError.value = true;
    });
};

const hasNoChange = hasNoChangeWrapper(originalEvent, event.value);

const startDateUpdatedCallback = (newDate) => {
  event.value.startDate = newDate;
};
const endDateUpdatedCallback = (newDate) => {
  event.value.endDate = newDate;
};
</script>
