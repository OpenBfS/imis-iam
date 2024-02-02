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
            v-if="$store.getters['profile/isChiefEditor']"
            color="accent"
            class="mr-4"
            v-bind="props"
            icon="mdi-calendar-plus"
            @click="
              resetNotification();
              $store.commit('events/setManagedEvent', { ...exampleEvent });
              $store.commit('application/setProcessType', 'add');
              $store.commit('application/setShowManageEventDialog', true);
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
                      $store.commit('events/setManagedEvent', {
                        ...$store.getters['events/getEvent'](event.id),
                      });
                      $store.commit('application/setProcessType', 'edit');
                      $store.commit(
                        'application/setShowManageEventDialog',
                        true
                      );
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
      v-bind:message="$store.state.application.httpErrorMessage"
    />
  </v-container>
</template>
<script setup>
import { expEvent } from "./event";
import { computed, onMounted, ref } from "vue";
import { useNotification } from "@/lib/use-notification";
import { useStore } from "vuex";
import { HTTP } from "@/lib/http";

const hover = true;
const store = useStore();
const { hasLoadingError, hasRequestError, resetNotification } =
  useNotification();

const exampleEvent = ref({ ...expEvent });

const formattedEvents = computed(() => {
  const newFormattedEvents = [];
  //Display dates as non repeating strings
  store.state.events.events.forEach((value, index) => {
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
        lastTimeString = store.state.events.events[index - 1].date;
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
  store.commit("events/setManagedEvent", {
    ...store.getters["events/getEvent"](id),
  });
  store.commit("application/setProcessType", "show");
  store.commit("application/setShowManageEventDialog", true);
};

const deleteEvent = (event) => {
  HTTP.delete("event/" + event.id)
    .then(() => {
      store.commit("events/removeEvent", event);
    })
    .catch(() => {
      hasRequestError.value = true;
    });
};

onMounted(() => {
  store.dispatch("events/loadEvents").catch(() => {
    hasLoadingError.value = true;
  });
});
</script>
