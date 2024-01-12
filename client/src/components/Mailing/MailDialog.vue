<!--
 Copyright (C) 2022 by Bundesamt fuer Strahlenschutz
 Software engineering by Intevation GmbH

 This file is Free Software under the GNU GPL (v>=3)
 and comes with ABSOLUTELY NO WARRANTY!
 -->
<template>
  <v-dialog v-model="show">
    <v-card min-width="60vw" class="align-self-center">
      <v-card-title>{{ $t("mailinglist.write_new_email") }}</v-card-title>
      <v-divider></v-divider>
      <v-container>
        <v-row class="px-2 py-2" no-gutters>
          <v-form ref="form" v-model="valid" class="v-col v-col-12">
            <v-row>
              <v-col cols="10">
                <v-row>
                  <v-select
                    density="compact"
                    class="v-col-6"
                    :no-data-text="$t('label.no_data_text')"
                    :items="senderList"
                    :label="$t('mailinglist.sender')"
                    v-model="selectedSender"
                  >
                  </v-select>
                  <v-select
                    density="compact"
                    class="v-col-6"
                    return-object
                    clearable
                    :label="$t('mailinglist.select_mailing_list')"
                    :items="props.mailingLists"
                    item-title="name"
                    item-value="id"
                    :no-data-text="$t('mailinglist.no_mailing_list')"
                    v-model="selectedList"
                    :rules="reqField($t('mailinglist.required_mailing_list'))"
                  >
                  </v-select>
                  <v-select
                    density="compact"
                    class="v-col-6"
                    :no-data-text="$t('label.no_data_text')"
                    return-object
                    clearable
                    :label="$t('mailinglist.type')"
                    :items="types"
                    item-title="name"
                    item-value="id"
                    v-model="selectedType"
                    :rules="reqField($t('mailinglist.required_type'))"
                  >
                  </v-select>
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
                      <v-text-field
                        v-model="expiryDateString"
                        clearable
                        prepend-inner-icon="mdi-calendar-blank"
                        :hint="$t('form.date_format')"
                        :label="$t('mailinglist.expiry_date')"
                        :rules="validGermanDate()"
                        @click="isExpiryDatePickerOpen = true"
                        @input="handleInputForExpiryDate"
                      >
                        <template v-slot:details></template>
                      </v-text-field>
                    </div>
                    <v-date-picker
                      v-click-outside="{
                        handler: toggleExpiryDatePicker,
                        closeConditional: closeConditional,
                        include: includeExpiryDatePicker,
                      }"
                      v-model="expiryDate"
                      v-show="isExpiryDatePickerOpen"
                      className="expiryDatePicker"
                      color="accent"
                      elevation="6"
                      style="
                        position: absolute;
                        top: 80pt;
                        z-index: 20;
                        background-color: white;
                        box-shadow: 0pt 0pt 8pt 4pt rgba(20, 20, 20, 0.2);
                      "
                      :show-adjacent-months="true"
                      :title="$t('mailinglist.expiry_date')"
                      @update:modelValue="handleExpiryDateUpdate"
                      ><template v-slot:header>
                        <div class="v-date-picker-header bg-accent">
                          <div class="v-date-picker-header__content">
                            {{ $d(expiryDate, "short") }}
                          </div>
                        </div>
                      </template>
                    </v-date-picker>
                  </div>
                  <v-checkbox
                    density="compact"
                    class="v-col-6"
                    :label="$t('mailinglist.publish')"
                    v-model="archived"
                  ></v-checkbox>
                </v-row>
              </v-col>
              <v-col cols="10">
                <v-row>
                  <v-text-field
                    :label="$t('mailinglist.subject')"
                    density="compact"
                    v-model="subject"
                    :rules="reqField($t('mailinglist.required_subject'))"
                  ></v-text-field>
                </v-row>
              </v-col>
            </v-row>
            <v-row>
              <v-col cols="12" class="mt-4">
                <v-textarea
                  :label="$t('mailinglist.message')"
                  variant="outlined"
                  name="input-7-1"
                  v-model="mailText"
                  :rules="reqField($t('mailinglist.required_content'))"
                ></v-textarea>
              </v-col>
            </v-row>
          </v-form>
        </v-row>
        <UIAlert
          v-if="hasRequestError || hasLoadingError"
          v-bind:message="$store.state.application.httpErrorMessage"
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
            $emit('mail-dialog-object', {
              closeDialog: true,
            })
          "
        >
          {{ $t("button.cancel") }}
        </v-btn>
      </v-card-actions>
    </v-card>
  </v-dialog>
</template>

<script setup>
import { onMounted, ref, computed } from "vue";
import { HTTP } from "@/lib/http";
import { useStore } from "vuex";
import { useNotification } from "@/lib/use-notification";
import { useForm } from "@/lib/use-form";
import { useI18n } from "vue-i18n";

const props = defineProps({
  mailingLists: Array,
});
const emit = defineEmits(["mail-dialog-object"]);

const show = true;
const store = useStore();
const { d } = useI18n();
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
} = useForm();
const selectedList = ref(null);
const mailText = ref("");
const subject = ref("");
const archived = ref(false);
const expiryDate = ref(new Date());
const expiryDateString = ref("");
const isExpiryDatePickerOpen = ref(false);
const userData = store.state.profile.userData;
const senderList = ref([
  [userData.firstName, userData.lastName, "<" + userData.email + ">"].join(" "),
  store.state.application.reportMail,
]);
const selectedSender = ref(senderList.value[0] || senderList.value[0] || "");
const toggleExpiryDatePicker = () => {
  isExpiryDatePickerOpen.value = !isExpiryDatePickerOpen.value;
};
const handleInputForExpiryDate = (event) => {
  const input = event.target.value;
  if (doesRegexMatchWholeString(germanDateRegex, input)) {
    const newDate = dateStringToDate(input);
    if (newDate) {
      expiryDate.value = newDate;
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
    ".expiryDatePicker *, #expiryDateTextfield *"
  );
  const includedElements = [];
  for (let i = 0; i < elements.length; i++) {
    includedElements.push(elements[i]);
  }
  return includedElements;
};
const sendMail = () => {
  resetNotification();
  HTTP.post("mail", {
    sender: selectedSender.value,
    text: mailText.value,
    subject: subject.value,
    archived: archived.value,
    type: selectedType.value.id,
    recipient: selectedList.value.id,
    expiryDate:
      expiryDateString.value.length > 0 ? expiryDate.value.toISOString() : "",
  })
    .then(() => {
      emit("mail-dialog-object", {
        closeDialog: true,
      });
    })
    .catch(() => {
      hasRequestError.value = true;
    });
};
// type
const selectedType = ref();
const types = computed(() => {
  return store.state.mail.mailTypes;
});
const getTypes = () => {
  store
    .dispatch("mail/loadMailTypes")
    .then()
    .catch(() => {
      hasLoadingError.value = true;
    });
};
onMounted(() => {
  getTypes();
});
</script>
