/* Copyright (C) 2023 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY!
 */
import { Promise } from "core-js";
import { PhotonHTTP } from "../lib/http";
export const coordinates = {
  namespaced: true,
  state: () => ({
    coordinates: [],
    coordinate: {},
  }),
  mutations: {
    setCoordinateList: (state, data) => {
      state.coordinates.length = 0;
      data.features.forEach((feature) => {
        var properties = feature.properties;
        var xCoord = feature.geometry.coordinates[0];
        var yCoord = feature.geometry.coordinates[1];
        var country = properties.country;
        var city = properties.city ? properties.city : properties.name;
        var street = properties.street ? properties.street : properties.name;
        var housenumber = properties.housenumber ? properties.housenumber : "";

        feature.properties.display = `${street} ${housenumber}, ${city}, ${country} [${xCoord} - ${yCoord}]`;
        state.coordinates.push(feature);
      });
    },
  },
  actions: {
    loadCoordinates({ commit }, query) {
      return new Promise((resolve, reject) => {
        PhotonHTTP.get("/api?q=" + query)
          .then((response) => {
            commit("setCoordinateList", response.data);
            resolve(response);
          })
          .catch((error) => reject(error));
      });
    },
  },
};
