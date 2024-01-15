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
            v-if="profileStore.isAllowedToManage"
            color="accent"
            class="mr-4"
            v-bind="props"
            icon="mdi-calendar-plus"
            @click="
              resetNotification();
              eventsStore.setManagedEvent(exampleEvent);
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
                      eventsStore.setManagedEvent({
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
                    @click="
                      eventsStore.setManagedEvent({
                        ...event,
                      });
                      applicationStore.setProcessType('delete');
                      applicationStore.setShowManageEventDialog(true);
                    "
                  />
                </template>
                <span>{{ $t("label.delete") }}</span>
              </v-tooltip>
            </td>
          </tr>
        </tbody>
      </v-table>
    </v-row>
  </v-container>
</template>
<script setup>
import { expEvent } from "./event";
import { computed, onMounted, ref } from "vue";
import { useNotification } from "@/lib/use-notification";
import { useApplicationStore } from "@/stores/application";
import { useEventsStore } from "@/stores/events";
import { useProfileStore } from "@/stores/profile";

const hover = true;
const applicationStore = useApplicationStore();
const eventsStore = useEventsStore();
const profileStore = useProfileStore();
const { hasLoadingError, resetNotification } = useNotification();

const exampleEvent = ref({ ...expEvent });

const formattedEvents = computed(() => {
  const newFormattedEvents = [];
  //Display dates as non repeating strings
  eventsStore.events.forEach((value, index) => {
    const now = Date.now();
    if (
      profileStore.isAllowedToManage ||
      value.startDate > now ||
      value.endDate > now
    ) {
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
  eventsStore.setManagedEvent({
    ...eventsStore.getEvent(id),
  });
  applicationStore.setProcessType("show");
  applicationStore.setShowManageEventDialog(true);
};

onMounted(() => {
  eventsStore.loadEvents().catch(() => {
    hasLoadingError.value = true;
  });
});
</script>
