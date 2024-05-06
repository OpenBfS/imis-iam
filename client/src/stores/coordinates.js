/* Copyright (C) 2023 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY!
 */
import { defineStore } from "pinia";
import { Promise } from "core-js";
import { PhotonHTTP } from "../lib/http";
export const useCoordinatesStore = defineStore("coordinates", {
  namespaced: true,
  state: () => ({
    coordinates: [],
    coordinate: {},
  }),
  actions: {
    loadCoordinates(query) {
      return new Promise((resolve, reject) => {
        PhotonHTTP.get("/api?q=" + query)
          .then((response) => {
            this.setCoordinateList(response);
            resolve(response);
          })
          .catch((error) => reject(error));
      });
    },
    setCoordinateList(response) {
      this.coordinates.length = 0;
      response?.data?.features.forEach((feature) => {
        var properties = feature.properties;
        var xCoord = feature.geometry.coordinates[0];
        var yCoord = feature.geometry.coordinates[1];
        var country = properties.country;
        var city = properties.city ? properties.city : properties.name;
        var street = properties.street ? properties.street : properties.name;
        var housenumber = properties.housenumber ? properties.housenumber : "";

        feature.properties.display = `${street} ${housenumber}, ${city}, ${country} [${xCoord} - ${yCoord}]`;
        this.coordinates.push(feature);
      });
    },
  },
});
