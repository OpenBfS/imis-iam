<!--
 Copyright (C) 2022 by Bundesamt fuer Strahlenschutz
 Software engineering by Intevation GmbH

 This file is Free Software under the GNU GPL (v>=3)
 and comes with ABSOLUTELY NO WARRANTY!
 -->
<template>
  <v-card width="80vw" v-if="['add', 'edit'].indexOf(processType) !== -1">
    <v-card-title v-if="processType === 'add'">
      <span class="text-h5">{{ $t("institution.create_title") }}</span>
    </v-card-title>
    <v-card-title v-if="processType === 'edit'">
      <span class="text-h5">
        {{ $t("institution.edit_title", { name: institution.name }) }}
      </span>
    </v-card-title>
    <v-divider></v-divider>
    <v-container class="pa-1 mt-4 mx-2">
      <v-row justify="center">
        <v-col cols="11">
          <v-form
            v-model="valid"
            ref="form"
            :readonly="!$store.state.profile.isAllowedToManage"
          >
            <div class="group_class">
              <v-text-field
                variant="underlined"
                density="compact"
                :label="$t('label.name')"
                v-model="institution.name"
                :rules="reqField($t('institution.required_name'))"
              ></v-text-field>
              <v-text-field
                variant="underlined"
                density="compact"
                :label="$t('institution.shortname')"
                :rules="reqField($t('institution.required_shortname'))"
                v-model="institution.shortName"
              ></v-text-field>
              <v-checkbox
                density="compact"
                v-model="institution.active"
                :label="$t('institution.active')"
              ></v-checkbox>
            </div>
            <div class="group_class">
              <v-text-field
                variant="underlined"
                density="compact"
                :label="$t('institution.service_building_location')"
                :rules="
                  reqField($t('institution.required_service_building_location'))
                "
                v-model="institution.serviceBuildingLocation"
              ></v-text-field>
              <v-text-field
                variant="underlined"
                density="compact"
                :label="$t('institution.service_building_postalcode')"
                :rules="
                  reqValidPostalcode(
                    $t('institution.required_service_building_postalcode'),
                    $t('form.valid_postalcode')
                  )
                "
                v-model="institution.serviceBuildingPostalCode"
              ></v-text-field>
              <v-text-field
                variant="underlined"
                density="compact"
                :label="$t('institution.service_building_street')"
                v-model="institution.serviceBuildingStreet"
                :rules="
                  reqField($t('institution.required_service_building_street'))
                "
              ></v-text-field>
            </div>
            <v-form
              ><v-row>
                <v-select
                  :no-data-text="$t('label.no_data_text')"
                  v-model="coordinates.coordinate"
                  dense
                  :label="$t('institution.coordinates')"
                  :loading="coordinatesLoading"
                  :error="coordinatesError"
                  :errorMessages="coordinatesErrorMessages"
                  :items="coordinates"
                  item-title="properties.display"
                  item-value="id"
                  persistent-hint
                  :return-object="coordinatesReturnObj"
                  density="compact"
                ></v-select>
              </v-row>
            </v-form>
            <div class="group_class">
              <v-text-field
                :disabled="true"
                variant="underlined"
                density="compact"
                :label="$t('institution.x_coordinate')"
                v-model="institution.xCoordinate"
              ></v-text-field>
              <v-text-field
                :disabled="true"
                variant="underlined"
                density="compact"
                :label="$t('institution.y_coordinate')"
                v-model="institution.yCoordinate"
              ></v-text-field>
            </div>

            <div class="group_class">
              <!-- TODO: Add this rules once the validation for
                    optional fields gets implemented by upstream.
                    :rules="validPostalcode($t('form.valid_postalcode'))" -->
              <v-text-field
                variant="underlined"
                density="compact"
                :label="$t('institution.address_location')"
                v-model="institution.addressLocation"
              ></v-text-field>
              <v-text-field
                variant="underlined"
                density="compact"
                :label="$t('institution.address_postalcode')"
                v-model="institution.addressPostalCode"
              ></v-text-field>
              <v-text-field
                variant="underlined"
                density="compact"
                :label="$t('institution.address_street')"
                v-model="institution.addressStreet"
              ></v-text-field>
            </div>
            <div class="group_class">
              <v-text-field
                variant="underlined"
                density="compact"
                :label="$t('institution.central_phone')"
                :rules="
                  reqValidPhone(
                    $t('institution.required_central_phone'),
                    $t('form.valid_phone')
                  )
                "
                v-model="institution.centralPhone"
              ></v-text-field>
              <v-text-field
                variant="underlined"
                density="compact"
                :label="$t('institution.central_email')"
                :rules="
                  reqValidmail(
                    $t('institution.required_central_email'),
                    $t('form.valid_email')
                  )
                "
                v-model="institution.centralMail"
              ></v-text-field>
              <!--TODO: Add this rule once the validation for
                    optional fields gets implemented by upstream.
                    :rules="validPhone($t('form.valid_fax'))" -->
              <v-text-field
                variant="underlined"
                density="compact"
                :label="$t('institution.central_fax')"
                v-model="institution.centralFax"
              ></v-text-field>
            </div>
            <div class="group_class">
              <v-text-field
                variant="underlined"
                density="compact"
                :label="$t('institution.imis_Id')"
                :disabled="
                  !$store.state.profile.userData.roles.some(
                    (e) => e === 'chief_editor'
                  )
                "
                :rules="[
                  (v) =>
                    !v ||
                    (v && v.length === 5) ||
                    $t('institution.imis_Id_length_validation_message'),
                ]"
                v-model="institution.imisId"
              ></v-text-field>
              <v-text-field
                v-model="institution.imisUserGroupId"
                variant="underlined"
                density="compact"
                :label="$t('institution.imis_usergroup_Id')"
                :disabled="
                  !$store.state.profile.userData.roles.some(
                    (e) => e === 'chief_editor'
                  )
                "
                :rules="[
                  (v) =>
                    !v ||
                    (v && v.length === 3) ||
                    $t(
                      'institution.imis_usergroup_Id_length_validation_message'
                    ),
                ]"
              >
              </v-text-field>
            </div>
            <div class="group_class align-center">
              <v-select
                :no-data-text="$t('label.no_data_text')"
                dense
                :label="$t('institution.categories')"
                :items="categories"
                v-model="institution.categoryName"
                item-title="name"
                item-value="id"
                persistent-hint
                density="compact"
                :rules="reqField($t('institution.required_category'))"
              >
              </v-select>
            </div>
          </v-form>
        </v-col>
      </v-row>
      <UIAlert
        v-if="hasLoadingError || hasRequestError"
        v-bind:message="$store.state.application.httpErrorMessage"
      />
    </v-container>
    <v-divider></v-divider>
    <v-card-actions>
      <v-spacer></v-spacer>
      <v-btn
        v-if="$store.state.profile.isAllowedToManage"
        color="accent"
        :disabled="!valid || hasNoChange"
        @click="
          processType == 'add' ? createInstitution() : updateInstitution()
        "
      >
        {{ processType == "add" ? $t("button.create") : $t("button.save") }}
      </v-btn>
      <v-btn
        v-if="processType === 'edit' && $store.state.profile.isAllowedToManage"
        color="accent"
        :disabled="hasNoChange"
        @click="resetForm(originalInstitution, institution)"
      >
        {{ $t("button.reset") }}
      </v-btn>
      <v-btn
        color="accent"
        @click="
          $store.commit('application/setShowManageInstitutionDialog', false)
        "
      >
        {{ $t("button.cancel") }}
      </v-btn>
    </v-card-actions>
  </v-card>
  <v-card v-else width="50vw">
    <v-card-text>
      <span class="text-h5">{{
        $t("label.confirm_deletion", { name: institution.name })
      }}</span>
    </v-card-text>
    <v-divider></v-divider>
    <v-card-actions>
      <v-spacer></v-spacer>
      <v-btn color="accent" @click="deleteInstitution()">
        {{ $t("button.delete") }}
      </v-btn>
      <v-btn
        color="accent"
        @click="
          $store.commit('application/setShowManageInstitutionDialog', false)
        "
      >
        {{ $t("button.cancel") }}
      </v-btn>
    </v-card-actions>
  </v-card>
</template>
<style lang="scss" scoped>
form > div {
  display: flex;
  padding-top: 8px;
}
.group_class > div {
  max-width: 33.3333333333%;
  width: 100%;
}
.group_class > div:nth-child(2) {
  padding: 0 10px;
}
</style>
<script setup>
import { onMounted, ref, watch } from "vue";
import { HTTP } from "@/lib/http";
import { useNotification } from "@/lib/use-notification";
import { useForm } from "@/lib/use-form";
import { useStore } from "vuex";
import { debounce } from "debounce";

const { hasLoadingError, hasRequestError } = useNotification();
const store = useStore();
const institution = ref(store.state.application.managedItem);
const originalInstitution = { ...institution.value };
const processType = ref(store.state.application.processType);
const coordinates = ref(store.state.coordinates.coordinates);
const {
  form,
  valid,
  reqValidmail,
  reqField,
  reqValidPhone,
  reqValidPostalcode,
  resetForm,
  hasNoChangeWrapper,
} = useForm();
const categories = ref([]);
const getCategories = () => {
  HTTP.get("institution/category")
    .then((response) => {
      categories.value = response.data;
    })
    .catch(() => {
      hasLoadingError.value = true;
    });
};
onMounted(() => {
  getCategories();

  // Initialize dropdown for coordinates
  const loc = institution.value.serviceBuildingLocation;
  const poc = institution.value.serviceBuildingPostalCode;
  const str = institution.value.serviceBuildingStreet;
  if (loc || poc || str) {
    triggerLoadCoordinates([loc, poc, str].join(" "));
  }
});
const getInstitutions = () => {
  store
    .dispatch("institution/loadInstitutions")
    .then()
    .catch(() => {
      hasLoadingError.value = true;
    });
};
const createInstitution = () => {
  let payload = { ...institution.value };
  HTTP.post("/institution", payload)
    .then(() => {
      getInstitutions();
      store.commit("application/setShowManageInstitutionDialog", false);
    })
    .catch(() => {
      hasRequestError.value = true;
    });
};
const updateInstitution = () => {
  let payload = { ...institution.value };
  store
    .dispatch("institution/updateInstitution", payload)
    .then(() => {
      store.commit("application/setShowManageInstitutionDialog", false);
    })
    .catch(() => {
      hasRequestError.value = true;
    });
};
const deleteInstitution = () => {
  HTTP.delete("institution/" + institution.value.id)
    .then(() => {
      getInstitutions();
      store.commit("application/setShowManageInstitutionDialog", false);
    })
    .catch(() => {
      hasRequestError.value = true;
    });
};
const hasNoChange = hasNoChangeWrapper(originalInstitution, institution.value);

const coordinatesLoading = ref(false);
const coordinatesError = ref(false);
const coordinatesErrorMessages = ref("");
const coordinatesReturnObj = ref(true);

//Handle coordinates picked
const coordinatesPicked = () => {
  var geometry = coordinates.value.coordinate.geometry.coordinates;
  institution.value.xCoordinate = geometry[0];
  institution.value.yCoordinate = geometry[1];
};
//Load coordinate store
const loadCoordinates = (queryString) => {
  return store.dispatch("coordinates/loadCoordinates", queryString);
};
const triggerLoadCoordinates = debounce((queryString) => {
  coordinatesLoading.value = true;
  loadCoordinates(queryString).then(
    () => {
      coordinatesLoading.value = false;
      coordinatesError.value = false;
      coordinatesErrorMessages.value = "";
    },
    (error) => {
      coordinatesLoading.value = false;
      coordinatesError.value = true;
      coordinatesErrorMessages.value = error.message;
    }
  );
}, 500);
watch(
  () => coordinates.value.coordinate,
  () => {
    coordinatesPicked();
  }
);
watch(
  [
    () => institution.value.serviceBuildingLocation,
    () => institution.value.serviceBuildingPostalCode,
    () => institution.value.serviceBuildingStreet,
  ],
  ([newLoc, newPc, newStreet]) => {
    triggerLoadCoordinates([newLoc, newPc, newStreet].join(" "));
  }
);
</script>
