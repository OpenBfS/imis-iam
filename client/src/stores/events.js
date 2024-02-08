/* Copyright (C) 2023 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY!
 */
import { defineStore } from "pinia";
import { HTTP } from "../lib/http";
export const useEventsStore = defineStore("events", {
  namespaced: true,
  state: () => ({
    events: [],
    managedEvent: {},
  }),
  actions: {
    setManagedEvent(data) {
      this.managedEvent = data;
    },
    setEvents(data) {
      this.events = data;
    },
    updateEventEntity(data) {
      this.events.forEach((element, index) => {
        if (element.id === data.id) {
          this.events[index] = data;
        }
      });
    },
    addEvent(data) {
      this.events.push(data);
    },
    removeEvent(data) {
      const index = this.events.findIndex((e) => e.id === data.id);
      this.events.splice(index, 1);
    },
    loadEvents(searchString) {
      return new Promise((resolve, reject) => {
        HTTP.get("/event", {
          params: { search: searchString },
        })
          .then((response) => {
            this.setEvents(response.data);
            resolve(response);
          })
          .catch((error) => reject(error));
      });
    },
    updateEvent(event) {
      return new Promise((resolve, reject) => {
        HTTP.put("/event", event)
          .then((response) => {
            this.updateEventEntity(response.data);
            resolve(response);
          })
          .catch((error) => reject(error));
      });
    },
  },
  getters: {
    getEvents: (state) => {
      return state.events;
    },
    getEvent: (state) => {
      return (id) =>
        state.events.find((event) => {
          return event.id === id;
        });
    },
  },
});
