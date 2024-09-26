<!--
 Copyright (C) 2023 by Bundesamt fuer Strahlenschutz
 Software engineering by Intevation GmbH

 This file is Free Software under the GNU GPL (v>=3)
 and comes with ABSOLUTELY NO WARRANTY!
 -->
<template>
  <v-container>
    <UIHeader>
      {{ $t("calendar.component_title") }}
    </UIHeader>
    <v-row justify="end" class="mt-6">
      <v-tooltip location="top">
        <template v-slot:activator="{ props }">
          <v-btn
            v-if="profileStore.isChiefEditor"
            color="accent"
            class="mr-4"
            v-bind="props"
            icon="mdi-calendar-plus"
            @click="
              resetNotification();
              applicationStore.setManagedItem({ ...exampleEvent });
              applicationStore.setProcessType('add');
              applicationStore.setShowManageEventDialog(true);
            "
          >
          </v-btn>
        </template>
        <span>{{ $t("calendar.add_entry") }}</span>
      </v-tooltip>
    </v-row>
    <v-row>
      <v-table class="ma-2 pa-2" :hover="hover" density="compact">
        <tbody>
          <tr v-for="event in formattedEvents" :key="event.id">
            <td>{{ event.date }}</td>
            <td>
              <v-btn variant="text" @click="itemClicked(event.id)">
                {{ event.title }}
              </v-btn>
            </td>
            <td>
              <v-tooltip location="top">
                <template v-slot:activator="{ props }">
                  <v-btn
                    v-if="event.readonly == false"
                    icon="mdi-calendar-edit"
                    variant="plain"
                    v-bind="props"
                    @click="
                      applicationStore.setManagedItem({
                        ...eventsStore.getEvent(event.id),
                      });
                      applicationStore.setProcessType('edit');
                      applicationStore.setShowManageEventDialog(true);
                    "
                  />
                </template>
                <span>{{ $t("label.edit") }}</span>
              </v-tooltip>
            </td>
            <td>
              <v-tooltip location="top">
                <template v-slot:activator="{ props }">
                  <v-btn
                    v-if="event.readonly == false"
                    icon="mdi-calendar-remove"
                    variant="plain"
                    v-bind="props"
                    @click="deleteEvent(event)"
                  />
                </template>
                <span>{{ $t("label.delete") }}</span>
              </v-tooltip>
            </td>
          </tr>
        </tbody>
      </v-table>
    </v-row>
    <UIAlert
      v-if="hasLoadingError || hasRequestError"
      v-bind:message="applicationStore.httpErrorMessage"
    />
  </v-container>
</template>
<script setup>
import { expEvent } from "./event.js";
import { computed, onMounted, ref } from "vue";
import { useNotification } from "@/lib/use-notification.js";
import { useApplicationStore } from "@/stores/application.js";
import { useEventsStore } from "@/stores/events.js";
import { useProfileStore } from "@/stores/profile.js";
import { HTTP } from "@/lib/http.js";

const hover = true;
const applicationStore = useApplicationStore();
const eventsStore = useEventsStore();
const profileStore = useProfileStore();
const { hasLoadingError, hasRequestError, resetNotification } =
  useNotification();

const exampleEvent = ref({ ...expEvent });

const formattedEvents = computed(() => {
  const newFormattedEvents = [];
  //Display dates as non repeating strings
  eventsStore.events.forEach((value, index) => {
    const startOfToday = new Date();
    startOfToday.setHours(0, 0, 0, 0);
    if (value.endDate > startOfToday.getTime()) {
      const date = new Date(value.startDate);
      var currentTimeString = date.toLocaleString("default", {
        month: "long",
        year: "numeric",
      });
      var lastTimeString = "";
      if (index > 0) {
        lastTimeString = eventsStore.events[index - 1].date;
      }
      let title = value.title;
      let site = value.site ? `(${value.site})` : "";
      var titleString = `${title} ${site}`;
      newFormattedEvents.push({
        id: value.id,
        date: currentTimeString === lastTimeString ? "" : currentTimeString,
        title: titleString,
        readonly: value.readonly,
      });
    }
  });
  return newFormattedEvents;
});

const itemClicked = (id) => {
  applicationStore.setManagedItem({
    ...eventsStore.getEvent(id),
  });
  applicationStore.setProcessType("show");
  applicationStore.setShowManageEventDialog(true);
};

const deleteEvent = (event) => {
  HTTP.delete("event/" + event.id)
    .then(() => {
      eventsStore.removeEvent(event);
    })
    .catch(() => {
      hasRequestError.value = true;
    });
};

onMounted(() => {
  eventsStore.loadEvents().catch(() => {
    hasLoadingError.value = true;
  });
});
</script>
