<!--
 Copyright (C) 2022 by Bundesamt fuer Strahlenschutz
 Software engineering by Intevation GmbH

 This file is Free Software under the GNU GPL (v>=3)
 and comes with ABSOLUTELY NO WARRANTY!
 -->
<template>
  <v-dialog v-model="show">
    <v-card min-width="60vw" class="align-self-center">
      <v-card-title>{{ $t("emails.write_newEmail") }}</v-card-title>
      <v-divider></v-divider>
      <v-container>
        <v-row class="px-2 py-2" no-gutters>
          <v-form ref="form" v-model="valid" class="v-col v-col-12">
            <v-row>
              <v-col cols="10">
                <v-row>
                  <Select
                    attribute="sender"
                    class="v-col-6"
                    :no-data-text="$t('label.noDataText')"
                    :items="senderList"
                    :label="$t('emails.sender')"
                    v-model="mail.selectedSender"
                  ></Select>
                  <Select
                    attribute="recipient"
                    class="v-col-6"
                    return-object
                    clearable
                    :label="$t('emails.recipient')"
                    :items="props.mailingLists"
                    item-title="name"
                    item-value="id"
                    :no-data-text="$t('emails.noRecipients')"
                    required
                    v-model="mail.selectedList"
                  ></Select>
                  <Select
                    attribute="type"
                    class="v-col-6"
                    :no-data-text="$t('label.noDataText')"
                    return-object
                    clearable
                    :label="$t('emails.type')"
                    :items="types"
                    item-title="name"
                    item-value="id"
                    required
                    v-model="mail.selectedType"
                  ></Select>
                </v-row>
              </v-col>
            </v-row>
            <v-row>
              <v-col cols="10">
                <v-row align="center">
                  <div
                    class="d-flex flex-column v-col-6"
                    style="position: relative"
                  >
                    <div id="expiryDateTextfield">
                      <TextField
                        v-model="expiryDateString"
                        clearable
                        variant="filled"
                        prepend-inner-icon="mdi-calendar-blank"
                        :hint="$t('hints.dateFormat')"
                        :label="$t('emails.expiryDate')"
                        :attribute="'expiryDate'"
                        @click="isExpiryDatePickerOpen = true"
                        @click:clear="isExpiryDatePickerOpen = false"
                        @input="handleInputForExpiryDate"
                      >
                        <template v-slot:details></template>
                      </TextField>
                    </div>
                    <v-date-picker
                      v-click-outside="{
                        handler: toggleExpiryDatePicker,
                        closeConditional: closeConditional,
                        include: includeExpiryDatePicker,
                      }"
                      v-model="mail.expiryDate"
                      v-show="isExpiryDatePickerOpen"
                      className="expiryDatePicker"
                      color="accent"
                      elevation="6"
                      style="
                        position: absolute;
                        top: 70pt;
                        z-index: 20;
                        background-color: white;
                        box-shadow: 0pt 0pt 8pt 4pt rgba(20, 20, 20, 0.2);
                      "
                      :show-adjacent-months="true"
                      :title="$t('emails.expiryDate')"
                      @update:modelValue="handleExpiryDateUpdate"
                      ><template v-slot:header>
                        <div class="v-date-picker-header bg-accent">
                          <div class="v-date-picker-header__content">
                            {{ $d(mail.expiryDate, "short") }}
                          </div>
                        </div>
                      </template>
                    </v-date-picker>
                  </div>
                  <Checkbox
                    attribute="archived"
                    class="v-col-6"
                    :label="$t('emails.publish')"
                    v-model="mail.archived"
                  ></Checkbox>
                </v-row>
              </v-col>
              <v-col cols="10">
                <TextField
                  :label="$t('emails.subject')"
                  v-model="mail.subject"
                  :attribute="'subject'"
                  required
                ></TextField>
              </v-col>
            </v-row>
            <v-row>
              <v-col cols="12" class="mt-4">
                <Textarea
                  :label="$t('emails.message')"
                  name="input-7-1"
                  v-model="mail.text"
                  attribute="text"
                  required
                ></Textarea>
              </v-col>
            </v-row>
          </v-form>
        </v-row>
        <v-label>* {{ $t("hints.requiredFields") }}</v-label>
        <UIAlert
          v-if="hasRequestError || hasLoadingError"
          v-bind:message="applicationStore.httpErrorMessage"
        />
      </v-container>
      <v-divider></v-divider>
      <v-card-actions>
        <v-spacer></v-spacer>
        <v-btn
          size="small"
          :disabled="!valid"
          color="accent"
          @click="sendMail()"
        >
          {{ $t("button.send") }}
        </v-btn>
        <v-btn
          color="accent"
          size="small"
          @click="
            onCancel(() => {
              $emit('mail-dialog-object', {
                closeDialog: true,
              });
            })
          "
        >
          {{ $t("button.cancel") }}
        </v-btn>
      </v-card-actions>
    </v-card>
    <ConfirmCancelDialog
      :isActive="showConfirmCancelDialog"
      :onConfirm="
        () => {
          $emit('mail-dialog-object', {
            closeDialog: true,
          });
        }
      "
      :onCancel="() => closeConfirmCancelDialog()"
    ></ConfirmCancelDialog>
  </v-dialog>
</template>

<script setup>
import { onBeforeMount, onMounted, ref, computed } from "vue";
import { HTTP } from "@/lib/http.js";
import { useApplicationStore } from "@/stores/application.js";
import { useMailStore } from "@/stores/mail.js";
import { useProfileStore } from "@/stores/profile.js";
import { useNotification } from "@/lib/use-notification.js";
import { useForm } from "@/lib/use-form.js";
import { useI18n } from "vue-i18n";
import Checkbox from "@/components/Form/Checkbox.vue";
import Textarea from "@/components/Form/Textarea.vue";
import TextField from "@/components/Form/TextField.vue";
import Select from "@/components/Form/Select.vue";
import ConfirmCancelDialog from "@/components/ConfirmCancelDialog.vue";

const props = defineProps({
  mailingLists: Array,
});
const emit = defineEmits(["mail-dialog-object"]);

const show = true;
const applicationStore = useApplicationStore();
const mailStore = useMailStore();
const profileStore = useProfileStore();

const { d, t } = useI18n();
const { hasRequestError, hasLoadingError, resetNotification } =
  useNotification();
// Mail
const {
  form,
  valid,
  reqField,
  dateStringToDate,
  validGermanDate,
  doesRegexMatchWholeString,
  germanDateRegex,
  watchChange,
  onCancel,
  showConfirmCancelDialog,
  closeConfirmCancelDialog,
  initClientRules,
  handleValidationErrorFromServer,
  isServerValidationError,
} = useForm();
const expiryDateString = ref("");
const isExpiryDatePickerOpen = ref(false);
const userData = profileStore.userData;
const senderList = ref([
  applicationStore.reportMail,
  [
    userData.attributes.firstName,
    userData.attributes.lastName,
    "<" + userData.attributes.email + ">",
  ].join(" "),
]);
// type
const types = computed(() => {
  return mailStore.mailTypes;
});
const getTypes = () => {
  mailStore
    .loadMailTypes()
    .then()
    .catch(() => {
      hasLoadingError.value = true;
    });
};
const mail = ref({
  selectedList: null,
  text: "",
  subject: "",
  archived: false,
  expiryDate: new Date(),
  selectedType: null,
  selectedSender: senderList.value[0] || "",
});
const emptyMail = structuredClone(mail.value);
watchChange(emptyMail, mail.value);
const toggleExpiryDatePicker = () => {
  isExpiryDatePickerOpen.value = !isExpiryDatePickerOpen.value;
};
const handleInputForExpiryDate = (event) => {
  const input = event.target.value;
  if (doesRegexMatchWholeString(germanDateRegex, input)) {
    const newDate = dateStringToDate(input);
    if (newDate) {
      mail.value.expiryDate = newDate;
    }
  }
};
const handleExpiryDateUpdate = (event) => {
  expiryDateString.value = d(event, "short");
};
const closeConditional = () => {
  return isExpiryDatePickerOpen.value;
};
const includeExpiryDatePicker = () => {
  const elements = document.querySelectorAll(
    ".expiryDatePicker *, #expiryDateTextfield *",
  );
  const includedElements = [];
  for (let i = 0; i < elements.length; i++) {
    includedElements.push(elements[i]);
  }
  return includedElements;
};
const sendMail = () => {
  resetNotification();
  HTTP.post("iam/mail", {
    sender: mail.value.selectedSender,
    text: mail.value.text,
    subject: mail.value.subject,
    archived: mail.value.archived,
    type: mail.value.selectedType.id,
    recipient: mail.value.selectedList.id,
    expiryDate:
      expiryDateString.value?.length > 0
        ? mail.value.expiryDate.toISOString()
        : "",
  })
    .then(() => {
      emit("mail-dialog-object", {
        closeDialog: true,
      });
    })
    .catch((error) => {
      isServerValidationError(error)
        ? handleValidationErrorFromServer(error.response.data)
        : (hasRequestError.value = true);
    });
};
onBeforeMount(() => {
  applicationStore.setForm(form);
  initClientRules({
    expiryDate: validGermanDate(),
    recipient: reqField(t("emails.requiredRecipient")),
    subject: reqField(t("emails.requiredSubject")),
    text: reqField(t("emails.requiredContent")),
    type: reqField(t("emails.requiredType")),
  });
});
onMounted(() => {
  getTypes();
});
</script>
