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
            :readonly="!profileStore.isAllowedToManage"
          >
            <div class="group_class">
              <TextField
                :label="$t('label.name')"
                :modelValue="institution.name"
                :rules="reqField($t('institution.required_name'))"
                @update:modelValue="institution.name = $event"
              ></TextField>
              <TextField
                :label="$t('institution.shortname')"
                :modelValue="institution.shortName"
                :rules="reqField($t('institution.required_shortname'))"
                @update:modelValue="institution.shortName = $event"
              ></TextField>
              <v-checkbox
                density="compact"
                v-model="institution.active"
                :label="$t('institution.active')"
              ></v-checkbox>
            </div>
            <div class="group_class">
              <TextField
                :label="$t('institution.service_building_location')"
                :modelValue="institution.serviceBuildingLocation"
                :rules="
                  reqField($t('institution.required_service_building_location'))
                "
                @update:modelValue="
                  institution.serviceBuildingLocation = $event
                "
              ></TextField>
              <TextField
                :label="$t('institution.service_building_postalcode')"
                :modelValue="institution.serviceBuildingPostalCode"
                :rules="
                  reqValidPostalcode(
                    $t('institution.required_service_building_postalcode'),
                    $t('error.valid_postalcode')
                  )
                "
                @update:modelValue="
                  institution.serviceBuildingPostalCode = $event
                "
              ></TextField>
              <TextField
                :label="$t('institution.service_building_street')"
                :modelValue="institution.serviceBuildingStreet"
                :rules="
                  reqField($t('institution.required_service_building_street'))
                "
                @update:modelValue="institution.serviceBuildingStreet = $event"
              ></TextField>
            </div>
            <v-form
              ><v-row>
                <v-select
                  :no-data-text="$t('label.no_data_text')"
                  v-model="selectedCoordinates"
                  clearable
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
                  @update:modelValue="coordinatesPicked"
                ></v-select>
              </v-row>
            </v-form>
            <div class="group_class">
              <TextField
                disabled
                :label="$t('institution.x_coordinate')"
                :modelValue="institution.xCoordinate"
                @update:modelValue="institution.xCoordinate = $event"
              ></TextField>
              <TextField
                disabled
                :label="$t('institution.y_coordinate')"
                :modelValue="institution.yCoordinate"
                @update:modelValue="institution.yCoordinate = $event"
              ></TextField>
            </div>

            <v-checkbox
              v-model="showPostalAddress"
              :label="$t('institution.differing_postal_address')"
            ></v-checkbox>
            <div v-if="showPostalAddress" class="group_class">
              <!-- TODO: Add this rules once the validation for
                    optional fields gets implemented by upstream.
                    :rules="validPostalcode($t('error.valid_postalcode'))" -->
              <TextField
                :label="$t('institution.address_location')"
                :modelValue="institution.addressLocation"
                @update:modelValue="institution.addressLocation = $event"
              ></TextField>
              <TextField
                :label="$t('institution.address_postalcode')"
                :modelValue="institution.addressPostalCode"
                @update:modelValue="institution.addressPostalCode = $event"
              ></TextField>
              <TextField
                :label="$t('institution.address_street')"
                :modelValue="institution.addressStreet"
                @update:modelValue="institution.addressStreet = $event"
              ></TextField>
            </div>
            <div class="group_class">
              <TextField
                :label="$t('institution.central_phone')"
                :modelValue="institution.centralPhone"
                :rules="
                  reqValidPhone(
                    $t('institution.required_central_phone'),
                    $t('error.valid_phone')
                  )
                "
                @update:modelValue="institution.centralPhone = $event"
              ></TextField>
              <TextField
                :label="$t('institution.central_email')"
                :modelValue="institution.centralMail"
                :rules="
                  reqValidmail(
                    $t('institution.required_central_email'),
                    $t('error.valid_email')
                  )
                "
                @update:modelValue="institution.centralMail = $event"
              ></TextField>
              <!--TODO: Add this rule once the validation for
                    optional fields gets implemented by upstream.
                    :rules="validPhone($t('error.valid_fax'))" -->
              <TextField
                :label="$t('institution.central_fax')"
                :modelValue="institution.centralFax"
                @update:modelValue="institution.centralFax = $event"
              ></TextField>
            </div>
            <div class="group_class">
              <TextField
                :disabled="
                  !profileStore.userData.roles.some((e) => e === 'chief_editor')
                "
                :label="$t('institution.imis_id')"
                :modelValue="institution.imisId"
                :rules="[
                  (v) =>
                    !v ||
                    (v && v.length === 5) ||
                    $t('institution.imis_id_length_validation_message'),
                ]"
                @update:modelValue="institution.imisId = $event"
              ></TextField>
              <TextField
                :disabled="
                  !profileStore.userData.roles.some((e) => e === 'chief_editor')
                "
                :label="$t('institution.imis_usergroup_id')"
                :modelValue="institution.imisUserGroupId"
                :rules="[
                  (v) => !!v || $t('institution.required_imis_usergroup_id'),
                  (v) =>
                    (v && v.length === 3) ||
                    $t(
                      'institution.imis_usergroup_id_length_validation_message'
                    ),
                ]"
                @update:modelValue="institution.imisUserGroupId = $event"
              ></TextField>
            </div>
            <div class="group_class align-center">
              <v-select
                :no-data-text="$t('label.no_data_text')"
                dense
                :label="$t('institution.categories')"
                :items="categories"
                v-model="institution.categoryNames"
                item-title="name"
                item-value="id"
                persistent-hint
                density="compact"
                :rules="reqField($t('error.required_category'))"
                multiple
              >
              </v-select>
            </div>
          </v-form>
        </v-col>
      </v-row>
      <UIAlert
        v-if="hasLoadingError || hasRequestError"
        v-bind:message="applicationStore.httpErrorMessage"
      />
    </v-container>
    <v-divider></v-divider>
    <v-card-actions>
      <v-spacer></v-spacer>
      <v-btn
        v-if="profileStore.isAllowedToManage"
        color="accent"
        :disabled="!valid || (hasNoChange && !isPostalAddressToBeDeleted)"
        @click="
          processType == 'add' ? createInstitution() : updateInstitution()
        "
      >
        {{ processType == "add" ? $t("button.create") : $t("button.save") }}
      </v-btn>
      <v-btn
        v-if="processType === 'edit' && profileStore.isAllowedToManage"
        color="accent"
        :disabled="
          hasNoChange && initialShowPostalAddress === showPostalAddress
        "
        @click="
          resetForm(originalInstitution, institution);
          showPostalAddress = initialShowPostalAddress;
        "
      >
        {{ $t("button.reset") }}
      </v-btn>
      <v-btn
        color="accent"
        @click="applicationStore.setShowManageInstitutionDialog(false)"
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
        @click="applicationStore.setShowManageInstitutionDialog(false)"
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
import { computed, onMounted, ref, watch } from "vue";
import { HTTP } from "@/lib/http";
import { useNotification } from "@/lib/use-notification";
import { useForm } from "@/lib/use-form";
import { useApplicationStore } from "@/stores/application";
import { useCoordinatesStore } from "@/stores/coordinates";
import { useInstitutionStore } from "@/stores/institution";
import { useProfileStore } from "@/stores/profile";
import { debounce } from "debounce";
import TextField from "@/components/TextField.vue";

const { hasLoadingError, hasRequestError } = useNotification();

const applicationStore = useApplicationStore();
const coordinatesStore = useCoordinatesStore();
const institutionStore = useInstitutionStore();
const profileStore = useProfileStore();

const institution = ref(applicationStore.managedItem);
const originalInstitution = { ...institution.value };
const processType = ref(applicationStore.processType);
const coordinates = ref(coordinatesStore.coordinates);
const selectedCoordinates = ref(null);
const showPostalAddress = ref(false);
const initialShowPostalAddress = ref(false);
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
  coordinatesStore.coordinates.length = 0;
  getCategories();

  if (hasPostalAddress()) {
    showPostalAddress.value = true;
    initialShowPostalAddress.value = true;
  }

  // Initialize dropdown for coordinates
  const loc = institution.value.serviceBuildingLocation;
  const poc = institution.value.serviceBuildingPostalCode;
  const str = institution.value.serviceBuildingStreet;
  if (loc || poc || str) {
    triggerLoadCoordinates([loc, poc, str].join(" "));
  }
});
const createInstitution = () => {
  let payload = { ...institution.value };
  HTTP.post("/institution", payload)
    .then((response) => {
      institutionStore.addInstitution(response.data);
      applicationStore.setShowManageInstitutionDialog(false);
    })
    .catch(() => {
      hasRequestError.value = true;
    });
};
const updateInstitution = () => {
  let payload = { ...institution.value };
  if (!showPostalAddress.value) {
    delete payload.addressLocation;
    delete payload.addressPostalCode;
    delete payload.addressStreet;
  }
  institutionStore
    .updateInstitution(payload)
    .then(() => {
      applicationStore.setShowManageInstitutionDialog(false);
    })
    .catch(() => {
      hasRequestError.value = true;
    });
};
const deleteInstitution = () => {
  HTTP.delete("institution/" + institution.value.id)
    .then(() => {
      institutionStore.removeInstitution(institution.value);
      applicationStore.setShowManageInstitutionDialog(false);
    })
    .catch(() => {
      hasRequestError.value = true;
    });
};

const hasNoChange = hasNoChangeWrapper(originalInstitution, institution.value);

function hasPostalAddress() {
  return (
    institution.value.addressLocation?.length > 0 ||
    institution.value.addressPostalCode?.length > 0 ||
    institution.value.addressStreet?.length > 0
  );
}
const isPostalAddressToBeDeleted = computed(() => {
  return hasPostalAddress() && !showPostalAddress.value;
});

const coordinatesLoading = ref(false);
const coordinatesError = ref(false);
const coordinatesErrorMessages = ref("");
const coordinatesReturnObj = ref(true);

//Handle coordinates picked
const coordinatesPicked = () => {
  if (!selectedCoordinates.value?.geometry) {
    institution.value.xCoordinate = null;
    institution.value.yCoordinate = null;
  } else {
    var geometry = selectedCoordinates.value.geometry.coordinates;
    institution.value.xCoordinate = geometry[0];
    institution.value.yCoordinate = geometry[1];
  }
};
//Load coordinate store
const loadCoordinates = (queryString) => {
  return coordinatesStore.loadCoordinates(queryString);
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
