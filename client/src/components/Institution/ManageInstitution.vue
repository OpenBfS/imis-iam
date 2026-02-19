<!--
 Copyright (C) 2022 by Bundesamt fuer Strahlenschutz
 Software engineering by Intevation GmbH

 This file is Free Software under the GNU GPL (v>=3)
 and comes with ABSOLUTELY NO WARRANTY!
 -->
<template>
  <FloatingDialog
    :z-index="managedItem.zIndex"
    @raise="applicationStore.raiseManagedItem(props.index)"
  >
    <template v-slot:header>
      <span v-if="processType === PROCESS_TYPE.ADD" class="text-h5">{{
        $t("institution.createTitle")
      }}</span>
      <span v-if="processType === PROCESS_TYPE.EDIT" class="text-h5">
        {{ $t("institution.editTitle", { name: institution.name }) }}
      </span>
    </template>
    <v-form
      v-model="valid"
      ref="form"
      :readonly="
        !profileStore.isAllowedToManage ||
        (profileStore.userData.role !== 'chief_editor' &&
          institution.network !== profileStore.userData.network)
      "
    >
      <v-container class="pa-3">
        <v-row>
          <v-col>
            <TextField
              :label="$t('label.name')"
              :attribute="'name'"
              required
              @update:modelValue="institution.name = $event"
            ></TextField>
          </v-col>
          <v-col>
            <Checkbox
              attribute="active"
              v-model="institution.active"
              :disabled="profileStore.userData.role !== 'chief_editor'"
              :label="$t('institution.active')"
            ></Checkbox>
          </v-col>
        </v-row>
        <v-row>
          <v-col :cols="cols">
            <TextField
              ref="measFacilIdField"
              :disabled="profileStore.userData.role !== 'chief_editor'"
              :label="$t('institution.measFacilId')"
              :attribute="'measFacilId'"
              @update:modelValue="
                institution.measFacilId = $event;
                measFacilNameField.validate();
              "
            ></TextField>
          </v-col>
          <v-col :cols="cols">
            <TextField
              ref="measFacilNameField"
              :label="$t('institution.measFacilName')"
              :attribute="'measFacilName'"
              @update:modelValue="
                institution.measFacilName = $event;
                measFacilIdField.validate();
              "
            ></TextField>
          </v-col>
          <v-col :cols="cols">
            <Combobox
              :items="applicationStore.networks"
              item-title="name"
              item-value="name"
              :label="$t('institution.network')"
              :attribute="'network'"
              :disabled="profileStore.userData.role !== 'chief_editor'"
              required
              @update:modelValue="institution.network = $event"
            ></Combobox>
          </v-col>
        </v-row>
        <v-row>
          <v-col :cols="cols">
            <TextField
              :label="$t('institution.serviceBuildingLocation')"
              :attribute="'serviceBuildingLocation'"
              required
              @update:modelValue="institution.serviceBuildingLocation = $event"
            ></TextField>
          </v-col>
          <v-col :cols="cols">
            <TextField
              :label="$t('institution.serviceBuildingPostalCode')"
              :attribute="'serviceBuildingPostalCode'"
              required
              @blur="checkForDuplicateAddress"
              @update:modelValue="
                institution.serviceBuildingPostalCode = $event
              "
            ></TextField>
          </v-col>
          <v-col :cols="cols">
            <TextField
              :label="$t('institution.serviceBuildingStreet')"
              :attribute="'serviceBuildingStreet'"
              required
              @blur="checkForDuplicateAddress"
              @update:modelValue="institution.serviceBuildingStreet = $event"
            ></TextField>
          </v-col>
        </v-row>
        <v-row>
          <v-col>
            <Select
              attribute="serviceBuildingState"
              clearable
              :items="states"
              item-title="label"
              item-value="value"
              :label="$t('institution.serviceBuildingState')"
              @update:modelValue="institution.serviceBuildingState = $event"
            ></Select>
          </v-col>
        </v-row>
        <!-- TODO: Geocoding feature delayed to a subsequent date -->
        <!--
            <v-form
              ><v-row>
                <v-select
                  :no-data-text="$t('label.noDataText')"
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
                :label="$t('institution.xCoordinate')"
                :modelValue="institution.xCoordinate"
                @update:modelValue="institution.xCoordinate = $event"
              ></TextField>
              <TextField
                disabled
                :label="$t('institution.yCoordinate')"
                :modelValue="institution.yCoordinate"
                @update:modelValue="institution.yCoordinate = $event"
              ></TextField>
            </div>
            -->

        <v-row>
          <v-col>
            <v-checkbox
              v-model="showPostalAddress"
              :label="$t('institution.differingPostalAddress')"
            ></v-checkbox>
          </v-col>
        </v-row>
        <v-row v-if="showPostalAddress">
          <v-col :cols="cols">
            <TextField
              :label="$t('institution.addressLocation')"
              :attribute="'addressLocation'"
              @update:modelValue="institution.addressLocation = $event"
            ></TextField>
          </v-col>
          <v-col :cols="cols">
            <TextField
              :label="$t('institution.addressPostalCode')"
              :attribute="'addressPostalCode'"
              @update:modelValue="institution.addressPostalCode = $event"
            ></TextField>
          </v-col>
          <v-col :cols="cols">
            <TextField
              :label="$t('institution.addressStreet')"
              :attribute="'addressStreet'"
              @update:modelValue="institution.addressStreet = $event"
            ></TextField>
          </v-col>
        </v-row>
        <v-row>
          <v-col :cols="cols">
            <ChipTextField
              :label="$t('institution.phoneNumbers')"
              :rules="validPhone($t('error.validPhone'))"
              @update:modelValue="institution.phoneNumbers = $event"
              :attribute="'phoneNumbers'"
            ></ChipTextField>
          </v-col>
          <v-col :cols="cols">
            <ChipTextField
              :label="$t('institution.mailAddresses')"
              :rules="validMail($t('error.validEmail'))"
              :attribute="'mailAddresses'"
              @update:modelValue="institution.mailAddresses = $event"
            ></ChipTextField>
          </v-col>
          <v-col :cols="cols">
            <TextField
              :label="$t('institution.centralFax')"
              @update:modelValue="institution.centralFax = $event"
              :attribute="'centralFax'"
            ></TextField>
          </v-col>
        </v-row>
        <v-row>
          <v-col>
            <v-label>{{
              $t("institution.operationModeChangeContact")
            }}</v-label>
          </v-col>
        </v-row>
        <v-row>
          <v-col :cols="cols">
            <ChipTextField
              :label="$t('institution.operationModeChangePhoneNumbers')"
              :rules="validPhone($t('error.validPhone'))"
              @update:modelValue="
                institution.operationModeChangePhoneNumbers = $event
              "
              :attribute="'operationModeChangePhoneNumbers'"
            ></ChipTextField>
          </v-col>
          <v-col :cols="cols">
            <ChipTextField
              :label="$t('institution.operationModeChangeSmsPhoneNumbers')"
              :rules="validPhone($t('error.validPhone'))"
              @update:modelValue="
                institution.operationModeChangeSmsPhoneNumbers = $event
              "
              :attribute="'operationModeChangeSmsPhoneNumbers'"
            ></ChipTextField>
          </v-col>
        </v-row>
        <v-row>
          <v-col :cols="cols">
            <ChipTextField
              :label="$t('institution.operationModeChangeMailAddresses')"
              :rules="validMail($t('error.validEmail'))"
              @update:modelValue="
                institution.operationModeChangeMailAddresses = $event
              "
              :attribute="'operationModeChangeMailAddresses'"
            ></ChipTextField>
          </v-col>
        </v-row>
        <v-row>
          <v-col :cols="cols">
            <Select
              attribute="tags"
              :no-data-text="$t('label.noDataText')"
              :label="$t('institution.tags')"
              :items="institutionStore.institutionTags"
              v-model="institution.tags"
              :disabled="profileStore.userData.role !== 'chief_editor'"
              item-title="name"
              item-value="id"
              persistent-hint
              multiple
              required
            ></Select>
          </v-col>
        </v-row>
        <v-row>
          <v-col>
            <v-label>* {{ $t("hints.requiredFields") }}</v-label>
          </v-col>
        </v-row>
      </v-container>
    </v-form>
    <UIAlert
      v-if="hasLoadingError || hasRequestError"
      v-bind:message="applicationStore.httpErrorMessage"
    />
    <template v-slot:actions>
      <v-btn
        v-if="profileStore.isAllowedToManage"
        color="accent"
        :disabled="!valid || (hasNoChange && !isPostalAddressToBeDeleted)"
        @click="processType == PROCESS_TYPE.ADD ? createInstitution() : saveInstitution()"
      >
        {{ processType == PROCESS_TYPE.ADD ? $t("button.create") : $t("button.save") }}
      </v-btn>
      <v-btn
        v-if="processType === PROCESS_TYPE.EDIT && profileStore.isAllowedToManage"
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
        @click="onCancel(() => applicationStore.removeManagedItem(props.index))"
      >
        {{ $t("button.cancel") }}
      </v-btn>
    </template>
  </FloatingDialog>
  <ConfirmCancelDialog
    :isActive="isDuplicateAddressDialogOpen"
    :message="$t('institution.institutionWithSameAddressMessage')"
    :title="$t('institution.institutionAlreadyExists')"
    :confirmButtonText="$t('button.yesProceed')"
    :cancelButtonText="$t('button.noCancelEntry')"
    :onConfirm="
      () => {
        dismissedDuplicateAddressDialog = true;
        isDuplicateAddressDialogOpen = false;
      }
    "
    :onCancel="() => close()"
  ></ConfirmCancelDialog>
  <ConfirmCancelDialog
    :isActive="showConfirmCancelDialog"
    :onConfirm="() => close()"
    :onCancel="() => closeConfirmCancelDialog()"
  ></ConfirmCancelDialog>
</template>

<script setup>
import {
  computed,
  onBeforeMount,
  onMounted,
  onUnmounted,
  provide,
  ref,
} from "vue";
import { createSearchQueryString, HTTP } from "@/lib/http.js";
import { useNotification } from "@/lib/use-notification.js";
import { useForm } from "@/lib/use-form.js";
import { trimSpacesInObject } from "@/lib/form-helper.js";
import { PROCESS_TYPE, useApplicationStore } from "@/stores/application.js";
// TODO: Geocoding feature delayed to a subsequent date
// import { useCoordinatesStore } from "@/stores/coordinates.js";
// import { debounce } from "debounce";
import { useInstitutionStore } from "@/stores/institution.js";
import { useProfileStore } from "@/stores/profile.js";
import { finishInstitutionDialog, updateInstitution } from "./institution.js";
import Checkbox from "@/components/Form/Checkbox.vue";
import ChipTextField from "@/components/Form/ChipTextField.vue";
import TextField from "@/components/Form/TextField.vue";
import Select from "@/components/Form/Select.vue";
import ConfirmCancelDialog from "@/components/ConfirmCancelDialog.vue";
import { useI18n } from "vue-i18n";
import { states } from "./institution";
import UIAlert from "../UI/UIAlert.vue";

const { t } = useI18n();

const { hasLoadingError, hasRequestError, resetNotification } =
  useNotification();

const applicationStore = useApplicationStore();
// TODO: Geocoding feature delayed to a subsequent date
// const coordinatesStore = useCoordinatesStore();
const institutionStore = useInstitutionStore();
const profileStore = useProfileStore();

const props = defineProps(["index"]);

provide("translationCategory", "institution");
provide("managedItemIndex", props.index);

const measFacilIdField = ref(null);
const measFacilNameField = ref(null);
const isDuplicateAddressDialogOpen = ref(false);
const dismissedDuplicateAddressDialog = ref(false);
const managedItem = applicationStore.managedItems[props.index];
const institution = ref(managedItem.item);
const processType = managedItem.processType;
const originalInstitution = ref(managedItem.originalItem);

// TODO: Geocoding feature delayed to a subsequent date
// const coordinates = ref(coordinatesStore.coordinates);
// const selectedCoordinates = ref(null);
const showPostalAddress = ref(false);
const initialShowPostalAddress = ref(false);
const {
  valid,
  hasNoChange,
  validMail,
  validPhone,
  validPostalcode,
  resetForm,
  validRegex,
  noLeadingTrailingSpaces,
  watchChange,
  onCancel,
  showConfirmCancelDialog,
  closeConfirmCancelDialog,
  handleValidationErrorFromServer,
  isServerValidationError,
  removeAllResetEventListeners,
  initClientRules,
  cols,
} = useForm();

const measIdAndNameOrNothing = () => {
  return [
    () => {
      const id = institution.value.measFacilId;
      const name = institution.value.measFacilName;
      return (
        (id === "" && name === "") ||
        (!id && !name) ||
        ((id || "") !== "" && (name || "") !== "") ||
        t("error.allOrNothing", [
          t("institution.measFacilId"),
          t("institution.measFacilName"),
        ])
      );
    },
  ];
};
onBeforeMount(() => {
  initClientRules({
    name: [
      ...validRegex(
        noLeadingTrailingSpaces,
        t("error.noLeadingTrailingSpaces")
      ),
      (v) => !v || !v.includes("\\") || t("error.noBackslash"),
    ],
    measFacilName: [
      ...measIdAndNameOrNothing(),
      ...validRegex(
        noLeadingTrailingSpaces,
        t("error.noLeadingTrailingSpaces")
      ),
    ],
    serviceBuildingPostalCode: [...validPostalcode(t("error.validPostalcode"))],
    serviceBuildingStreet: [
      ...validRegex(
        noLeadingTrailingSpaces,
        t("error.noLeadingTrailingSpaces")
      ),
    ],
    serviceBuildingLocation: [
      ...validRegex(
        noLeadingTrailingSpaces,
        t("error.noLeadingTrailingSpaces")
      ),
    ],
    addressPostalCode: validPostalcode(t("error.validPostalcode")),
    centralPhone: validPhone(t("error.validPhone")),
    centralFax: validPhone(t("error.validFax")),
    centralMail: validMail(t("error.validEmail")),
    measFacilId: [
      (v) =>
        !v ||
        (v && v.length >= 5 && v.length <= 7) ||
        t("institution.measFacilIdLengthValidationMessage", {
          minLength: 5,
          maxLength: 7,
        }),
      ...measIdAndNameOrNothing(),
      ...validRegex(
        noLeadingTrailingSpaces,
        t("error.noLeadingTrailingSpaces")
      ),
    ],
  });
});
onMounted(() => {
  // TODO: Geocoding feature delayed to a subsequent date
  // coordinatesStore.coordinates.length = 0;
  institutionStore.loadInstitutionTags().catch(() => {
    hasLoadingError.value = true;
  });

  if (hasPostalAddress()) {
    showPostalAddress.value = true;
    initialShowPostalAddress.value = true;
  }

  // TODO: Geocoding feature delayed to a subsequent date
  // Initialize dropdown for coordinates
  /*const loc = institution.value.serviceBuildingLocation;
  const poc = institution.value.serviceBuildingPostalCode;
  const str = institution.value.serviceBuildingStreet;
  if (loc || poc || str) {
    triggerLoadCoordinates([loc, poc, str].join(" "));
  }*/
});
onUnmounted(() => {
  removeAllResetEventListeners();
});
const sanitizePayload = (payload) => {
  if (payload.measFacilId !== undefined && payload.measFacilId === "") {
    delete payload.measFacilId;
  }
  if (payload.measFacilName !== undefined && payload.measFacilName === "") {
    delete payload.measFacilName;
  }
  // Remove attributes with empty strings because some lead to a rejection by the backend
  // if a value has to be null or fulfill a contraint like a minimum length.
  const keysToDelete = [];
  const allKeys = Object.keys(payload);
  allKeys.forEach((key) => {
    if (payload[key] === "") {
      keysToDelete.push(key);
    }
  });
  keysToDelete.forEach((key) => {
    delete payload[key];
  });
  return trimSpacesInObject(payload);
};
const createInstitution = () => {
  let payload = { ...institution.value };
  sanitizePayload(payload);
  HTTP.post("/iam/institution", payload)
    .then((response) => {
      const newInstitution = response.data;
      institutionStore.addInstitution(newInstitution);
      finishInstitutionDialog(newInstitution, props.index);
    })
    .catch((error) => {
      isServerValidationError(error)
        ? handleValidationErrorFromServer(error.response.data)
        : (hasRequestError.value = true);
    });
};
// Cannot call updateInstitution directly in the <template> because then hasRequestError is no ref anymore
// but a ref is necessary so we can detect any change of it in this component.
const saveInstitution = () => {
  updateInstitution(
    sanitizePayload({ ...institution.value }),
    showPostalAddress,
    isServerValidationError,
    handleValidationErrorFromServer,
    hasRequestError,
    props.index
  );
};

watchChange(originalInstitution, institution.value);

const close = () => {
  applicationStore.removeManagedItem(props.index);
};

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

const checkForDuplicateAddress = () => {
  if (
    !institution.value.serviceBuildingPostalCode ||
    !institution.value.serviceBuildingStreet ||
    dismissedDuplicateAddressDialog.value
  )
    return;
  const params = {
    firstResult: 0,
    search: createSearchQueryString(undefined, {
      serviceBuildingPostalCode: institution.value.serviceBuildingPostalCode,
      serviceBuildingStreet: institution.value.serviceBuildingStreet,
    }),
  };
  HTTP.get("/iam/institution", {
    params,
  }).then((response) => {
    if (response.status === 200) {
      const foundInstitutions = response.data.list;
      const instWithDuplAddress = foundInstitutions.find((i) => {
        return (
          (!institution.value.id || institution.value.id !== i.id) &&
          i.serviceBuildingPostalCode ===
            institution.value.serviceBuildingPostalCode &&
          i.serviceBuildingStreet === institution.value.serviceBuildingStreet
        );
      });
      if (instWithDuplAddress) {
        isDuplicateAddressDialogOpen.value = true;
      }
    }
  });
};

// TODO: Geocoding feature delayed to a subsequent date
/*
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
);*/
</script>
