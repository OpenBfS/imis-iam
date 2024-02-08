/* Copyright (C) 2023 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY!
 */
import { HTTP } from "../lib/http";
export const events = {
  namespaced: true,
  state: () => ({
    events: [],
    managedEvent: {},
  }),
  mutations: {
    setManagedEvent: (state, data) => {
      state.managedEvent = data;
    },
    setEvents: (state, data) => {
      state.events = data;
    },
    updateEventEntity: (state, data) => {
      state.events.forEach((element, index) => {
        if (element.id === data.id) {
          state.events[index] = data;
        }
      });
    },
    addEvent: (state, data) => {
      state.events.push(data);
    },
    removeEvent: (state, data) => {
      const index = state.events.findIndex((e) => e.id === data.id);
      state.events.splice(index, 1);
    },
  },
  actions: {
    loadEvents({ commit }, searchString) {
      return new Promise((resolve, reject) => {
        HTTP.get("/event", {
          params: { search: searchString },
        })
          .then((response) => {
            commit("setEvents", response.data);
            resolve(response);
          })
          .catch((error) => reject(error));
      });
    },
    updateEvent({ commit }, event) {
      return new Promise((resolve, reject) => {
        HTTP.put("/event", event)
          .then((response) => {
            commit("updateEventEntity", response.data);
            resolve(response);
          })
          .catch((error) => reject(error));
      });
    },
  },
  getters: {
    getEvents(state) {
      return state.events;
    },
    getEvent: (state) => (id) => {
      return state.events.find((event) => {
        return event.id === id;
      });
    },
  },
};
