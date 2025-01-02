/* Copyright (C) 2022 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY!
 */
import { defineStore } from "pinia";
import { Promise } from "core-js";
import { createSearchQueryString, HTTP } from "../lib/http.js";

export const useInstitutionStore = defineStore("institution", {
  namespaced: true,
  state: () => ({
    //List of institutions
    institutions: [],
    institutionNames: [],
    //Institution currently edited
    institution: {},
    //Institution to be created
    newInstitution: {
      name: "",
    },
    foundInstitutions: [],
    selectedInstitutions: [],
    offset: 0,
    itemsPerPage: 25,
    filterBy: {},
    // Object with keys "key" and "order"
    sortBy: null,
    totalNumberOfInstitutions: 0,
  }),
  actions: {
    //Convert current institution attributes to string arrays
    convertCurrentInstitutionAttributes() {
      let attributes = this.institution.attributes;
      for (let attribute in attributes) {
        if (typeof attributes[attribute] == "string") {
          attributes[attribute] = attributes[attribute].split(", ");
        }
      }
    },
    setInstitutionList(data) {
      this.institutions = data;
    },
    setFoundInstitutions(data) {
      this.foundInstitutions = data;
    },
    setInstitutionNames(data) {
      var names = [];
      data.forEach((institution) => {
        names.push(institution.name);
      });
      this.institutionNames = names;
    },
    setInstitution(data) {
      this.institution = data;
    },
    updateInstitutionEntity(data) {
      this.institutions.forEach((element, index) => {
        if (element.id === data.id) {
          this.institutions[index] = data;
        }
      });
    },
    loadInstitutions(searchString) {
      return new Promise((resolve, reject) => {
        const params = {
          search: createSearchQueryString(searchString, this.filterBy),
          firstResult: this.offset,
          maxResults: this.itemsPerPage,
          sortBy: this.sortBy?.key,
          order: this.sortBy?.order,
        };
        HTTP.get("/institution", {
          params,
        })
          .then((response) => {
            this.totalNumberOfInstitutions = response.data.size;
            if (searchString) {
              this.setFoundInstitutions(response.data.list);
            } else {
              this.setInstitutionList(response.data.list);
              this.setFoundInstitutions(response.data.list);
            }
            resolve(response);
          })
          .catch((error) => reject(error));
      });
    },
    updateInstitution(institution) {
      return new Promise((resolve, reject) => {
        HTTP.put("/institution", institution)
          .then((response) => {
            this.updateInstitutionEntity(response.data);
            resolve(response);
          })
          .catch((error) => reject(error));
      });
    },
    addInstitution(data) {
      this.institutions.push(data);
      this.totalNumberOfInstitutions++;
    },
    removeInstitution(data) {
      const index = this.institutions.findIndex((inst) => inst.id === data.id);
      this.institutions.splice(index, 1);
      this.totalNumberOfInstitutions--;
    },
    updateFilter(key, term) {
      if (term === null || term.length === 0) {
        delete this.filterBy[key];
      } else if (!term.length || term.length > 0) {
        this.filterBy[key] = term;
      }
    },
  },
});
