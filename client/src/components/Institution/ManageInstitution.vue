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
            <v-row>
              <v-col>
                <TextField
                  :label="$t('label.name')"
                  :attribute="'name'"
                  @update:modelValue="institution.name = $event"
                ></TextField>
              </v-col>
              <v-col>
                <Checkbox
                  attribute="active"
                  v-model="institution.active"
                  :label="$t('institution.active')"
                ></Checkbox>
              </v-col>
            </v-row>
            <v-row>
              <v-col cols="3">
                <TextField
                  :disabled="profileStore.userData.role !== 'chief_editor'"
                  :label="$t('institution.meas_facil_id')"
                  :attribute="'measFacilId'"
                  @update:modelValue="institution.measFacilId = $event"
                ></TextField>
              </v-col>
              <v-col cols="3">
                <TextField
                  :label="$t('institution.meas_facil_name')"
                  :attribute="'measFacilName'"
                  @update:modelValue="institution.measFacilName = $event"
                ></TextField>
              </v-col>
            </v-row>
            <div class="group_class">
              <TextField
                :label="$t('institution.service_building_location')"
                :attribute="'serviceBuildingLocation'"
                required
                @update:modelValue="
                  institution.serviceBuildingLocation = $event
                "
              ></TextField>
              <TextField
                :label="$t('institution.service_building_postal_code')"
                :attribute="'serviceBuildingPostalCode'"
                required
                @update:modelValue="
                  institution.serviceBuildingPostalCode = $event
                "
              ></TextField>
              <TextField
                :label="$t('institution.service_building_street')"
                :attribute="'serviceBuildingStreet'"
                required
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
                :attribute="'addressLocation'"
                @update:modelValue="institution.addressLocation = $event"
              ></TextField>
              <TextField
                :label="$t('institution.address_postal_code')"
                :attribute="'addressPostalCode'"
                @update:modelValue="institution.addressPostalCode = $event"
              ></TextField>
              <TextField
                :label="$t('institution.address_street')"
                :attribute="'addressStreet'"
                @update:modelValue="institution.addressStreet = $event"
              ></TextField>
            </div>
            <div class="group_class">
              <TextField
                :label="$t('institution.central_phone')"
                @update:modelValue="institution.centralPhone = $event"
                :attribute="'centralPhone'"
                :required="true"
              ></TextField>
              <TextField
                :label="$t('institution.central_mail')"
                :attribute="'centralMail'"
                @update:modelValue="institution.centralMail = $event"
                required
              ></TextField>
              <!--TODO: Add this rule once the validation for
                    optional fields gets implemented by upstream.
                    :rules="validPhone($t('error.valid_fax'))" -->
              <TextField
                :label="$t('institution.central_fax')"
                @update:modelValue="institution.centralFax = $event"
                :attribute="'centralFax'"
              ></TextField>
            </div>
            <v-row>
              <v-col>
                <ChipTextField
                  :label="$t('institution.central_alarm_phone_numbers')"
                  :rules="validPhone($t('error.valid_phone'))"
                  @update:modelValue="
                    institution.centralAlarmPhoneNumbers = $event
                  "
                  :attribute="'centralAlarmPhoneNumbers'"
                ></ChipTextField>
              </v-col>
              <v-col>
                <ChipTextField
                  :label="$t('institution.central_alarm_email_addresses')"
                  :rules="validMail($t('error.valid_email'))"
                  @update:modelValue="
                    institution.centralAlarmEmailAddresses = $event
                  "
                  :attribute="'centralAlarmEmailAddresses'"
                ></ChipTextField>
              </v-col>
            </v-row>
            <v-row>
              <v-col>
                <Select
                  attribute="tags"
                  :no-data-text="$t('label.no_data_text')"
                  :label="$t('institution.tags')"
                  :items="categories"
                  v-model="institution.tags"
                  item-title="name"
                  item-value="id"
                  persistent-hint
                  required
                  multiple
                ></Select>
              </v-col>
            </v-row>
          </v-form>
        </v-col>
      </v-row>
      <v-label>* {{ $t("hints.required_fields") }}</v-label>
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
          resetForm(originalInstitution, institution, resetNotification);
          showPostalAddress = initialShowPostalAddress;
        "
      >
        {{ $t("button.reset") }}
      </v-btn>
      <v-btn
        color="accent"
        @click="
          onCancel(() => applicationStore.setShowManageInstitutionDialog(false))
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
        @click="applicationStore.setShowManageInstitutionDialog(false)"
      >
        {{ $t("button.cancel") }}
      </v-btn>
    </v-card-actions>
  </v-card>
  <ConfirmCancelDialog
    :isActive="showConfirmCancelDialog"
    :onConfirm="() => applicationStore.setShowManageInstitutionDialog(false)"
    :onCancel="() => closeConfirmCancelDialog()"
  ></ConfirmCancelDialog>
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
import { computed, onBeforeMount, onMounted, ref, watch } from "vue";
import { HTTP } from "@/lib/http";
import { useNotification } from "@/lib/use-notification";
import { useForm } from "@/lib/use-form";
import { useApplicationStore } from "@/stores/application";
import { useCoordinatesStore } from "@/stores/coordinates";
import { useInstitutionStore } from "@/stores/institution";
import { useProfileStore } from "@/stores/profile";
import { debounce } from "debounce";
import Checkbox from "@/components/Form/Checkbox.vue";
import ChipTextField from "@/components/Form/ChipTextField.vue";
import TextField from "@/components/Form/TextField.vue";
import Select from "@/components/Form/Select.vue";
import ConfirmCancelDialog from "@/components/ConfirmCancelDialog.vue";
import { useI18n } from "vue-i18n";

const { t } = useI18n();

const { hasLoadingError, hasRequestError, resetNotification } =
  useNotification();

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
  hasNoChange,
  reqValidmail,
  validMail,
  reqField,
  reqValidPhone,
  validPhone,
  reqValidPostalcode,
  resetForm,
  watchChange,
  onCancel,
  showConfirmCancelDialog,
  closeConfirmCancelDialog,
  handleValidationErrorFromServer,
  isServerValidationError,
} = useForm();
const categories = ref([]);
const getCategories = () => {
  HTTP.get("institution/tag")
    .then((response) => {
      categories.value = response.data;
    })
    .catch(() => {
      hasLoadingError.value = true;
    });
};
const measIdAndNameOrNothing = () => {
  return [
    () =>
      (institution.value.measFacilId === "" &&
        institution.value.measFacilName === "") ||
      (institution.value.measFacilId !== "" &&
        institution.value.measFacilName !== "") ||
      t("error.all_or_nothing", [
        t("institution.meas_facil_id"),
        t("institution.meas_facil_name"),
      ]),
  ];
};
onBeforeMount(() => {
  applicationStore.setForm(form);
  applicationStore.initClientRules({
    name: reqField(t("institution.required_name")),
    measFacilName: [...measIdAndNameOrNothing()],
    serviceBuildingLocation: reqField(
      t("institution.required_service_building_location")
    ),
    serviceBuildingPostalCode: reqValidPostalcode(
      t("institution.required_service_building_postal_code"),
      t("error.valid_postalcode")
    ),
    serviceBuildingStreet: reqField(
      t("institution.required_service_building_street")
    ),
    centralPhone: reqValidPhone(
      t("institution.required_central_phone"),
      t("error.valid_phone")
    ),
    centralMail: reqValidmail(
      t("institution.required_central_email"),
      t("error.valid_email")
    ),
    measFacilId: [
      (v) =>
        !v ||
        (v && v.length === 5) ||
        t("institution.imis_id_length_validation_message"),
      ...measIdAndNameOrNothing(),
    ],
    tags: reqField(t("error.required_tag")),
  });
});
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
      applicationStore.searchRequest(["institutions"]);
      applicationStore.setShowManageInstitutionDialog(false);
    })
    .catch((error) => {
      isServerValidationError(error)
        ? handleValidationErrorFromServer(error.response.data)
        : (hasRequestError.value = true);
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
      applicationStore.searchRequest(["institutions"]);
      applicationStore.setShowManageInstitutionDialog(false);
    })
    .catch((error) => {
      isServerValidationError(error)
        ? handleValidationErrorFromServer(error.response.data)
        : (hasRequestError.value = true);
    });
};
const deleteInstitution = () => {
  HTTP.delete("institution/" + institution.value.id)
    .then(() => {
      institutionStore.removeInstitution(institution.value);
      applicationStore.searchRequest(["institutions"]);
      applicationStore.setShowManageInstitutionDialog(false);
    })
    .catch(() => {
      hasRequestError.value = true;
    });
};

watchChange(originalInstitution, institution.value);

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
