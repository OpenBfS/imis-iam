/* Copyright (C) 2022 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY!
 */
import { defineStore } from "pinia";
import { HTTP } from "../lib/http.js";

export const useProfileStore = defineStore("profile", {
  state: () => ({
    userData: {},
    userProfileMetadata: {},
    myMailingLists: [],
    isAllowedToManage: true,
  }),
  getters: {
    attributes: (state) => {
      return state.userProfileMetadata.attributes;
    },
    getAttribute: (state) => {
      return (name) => {
        return state.attributes?.find((attribute) => attribute.name === name);
      };
    },
    isAttributeSelection: (state) => {
      return (name) => {
        return state.getAttribute(name)?.validations?.options !== undefined;
      };
    },
    // Returns array of items that can be used by select components
    getSelectionItemsOfAttribute: (state) => {
      return (name) => {
        return state
          .getAttribute(name)
          .validations.options?.options.map((option) => {
            return { title: option, value: option };
          });
      };
    },
    attributeGroups: (state) => {
      return state.userProfileMetadata.groups;
    },
    getGroupByAttributeName: (state) => {
      return (attributeName) => {
        const attribute = state.getAttribute(attributeName);
        return state.attributeGroups.find(
          (group) => group.name === attribute.group
        );
      };
    },
    attributesWithoutGroup: (state) => {
      return state.userProfileMetadata.attributes?.filter(
        (attribute) => !attribute.group,
      );
    },
    // Returns all attributes of a group but filters out attributes that are not allowed to be
    // seen by the current user.
    attributesOfGroup: (state) => {
      return (groupName, userToEdit) => {
        const profileUsername = state.userData.attributes.username[0];
        return state.userProfileMetadata.attributes.filter(
          (attribute) =>
            attribute.group === groupName &&
            (!attribute.annotations?.private ||
              profileUsername === userToEdit.attributes.username[0])
        );
      };
    },
    isAttributePrivate: (state) => {
      return (name) => {
        return state.getAttribute(name)?.annotations?.private === true;
      }
    },
    isChiefEditor: (state) => {
      return state.userData.role === "chief_editor";
    },
  },
  actions: {
    setUserData(data) {
      this.userData = data;
    },
    setUserProfileMetadata(data) {
      this.userProfileMetadata = data;
    },
    setMyMailingLists(data) {
      this.myMailingLists = data;
    },
    setIsAllowedToManage(data) {
      this.isAllowedToManage = data;
    },
    loadProfile() {
      return new Promise((resolve, reject) => {
        HTTP.get("/iam/user/profile")
          .then((response) => {
            this.setUserData(response.data);
            this.setIsAllowedToManage(response.data.role !== "user");
            resolve(response);
          })
          .catch((error) => reject(error));
      });
    },
    loadUserProfileMetadata() {
      return new Promise((resolve, reject) => {
        HTTP.get("/iam/user/userprofilemetadata")
          .then((response) => {
            this.setUserProfileMetadata(response.data);
            resolve(response);
          })
          .catch((error) => reject(error));
      });
    },
    getMyMailingLists() {
      return new Promise((resolve, reject) => {
        HTTP.get("iam/mail/list?subscribed=true")
          .then((response) => {
            this.setMyMailingLists(response.data);
            resolve(response);
          })
          .catch((error) => reject(error));
      });
    },
  },
});
