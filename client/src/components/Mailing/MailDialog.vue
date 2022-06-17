<template>
  <v-dialog v-model="show">
    <v-card min-width="50vw">
      <v-card-title>{{ $t("mailinglist.write_new_email") }}</v-card-title>
      <v-divider></v-divider>
      <v-container>
        <v-row class="px-2 py-2" no-gutters="">
          <v-col cols="7">
            <v-select
              return-object
              clearable
              :label="$t('mailinglist.select_mailing_list')"
              :items="mailingLists"
              item-title="name"
              item-value="id"
              :no-data-text="$t('mailinglist.no_Mailing_list')"
              v-model="selectedList"
            >
            </v-select>
          </v-col>
          <v-col cols="7">
            <v-text-field
              :label="$t('mailinglist.subject')"
              v-model="subject"
            ></v-text-field>
          </v-col>
          <v-row>
            <v-col cols="4">
              <v-select
                return-object
                clearable
                :label="$t('mailinglist.type')"
                :items="types"
                item-title="name"
                item-value="id"
                v-model="selectedType"
              >
              </v-select>
            </v-col>
            <v-col cols="4">
              <v-select
                :items="[$store.state.profile.userData.email]"
                :label="$t('mailinglist.sender')"
                v-model="$store.state.profile.userData.email"
                item-title="name"
                item-value="id"
              >
              </v-select>
            </v-col>
            <v-col cols="4">
              <v-checkbox
                :label="$t('mailinglist.publish')"
                v-model="shouldPublish"
              ></v-checkbox>
            </v-col>
          </v-row>

          <v-col cols="12">
            <v-textarea name="input-7-1" v-model="mailText"></v-textarea>
          </v-col>
        </v-row>
        <UIAlert
          v-if="hasRequestError || hasLoadingError"
          v-bind:isSuccessful="false"
          v-bind:message="$store.state.application.httpErrorMessage"
        />
      </v-container>
      <v-divider></v-divider>
      <v-card-actions>
        <v-spacer></v-spacer>
        <v-btn
          size="small"
          :disabled="!selectedList || !selectedType"
          :color="selectedList && selectedType ? 'accent' : 'grey'"
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

<script>
import { onMounted, ref, defineAsyncComponent } from "vue";
import { HTTP } from "@/lib/http";
import { useStore } from "vuex";
import { useNotification } from "@/lib/use-notification";

export default {
  props: ["mailingLists"],
  components: {
    UIAlert: defineAsyncComponent(() => import("@/components/UI/UIAlert.vue")),
  },
  setup(_, { emit }) {
    const show = true;
    const store = useStore();
    const { hasRequestError, hasLoadingError, resetNotification } =
      useNotification();
    // Mail
    const selectedList = ref(null);
    const mailText = ref("");
    const subject = ref("");
    const shouldPublish = ref(false);
    const selectedSender = ref("");
    const sendMail = () => {
      resetNotification();
      HTTP.post("mail", {
        sender: selectedSender.value,
        text: mailText.value,
        subject: subject.value,
        publish: shouldPublish.value,
        type: selectedType.value.id,
        receipient: selectedList.value.id,
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
    const types = ref([]);
    const selectedType = ref();
    const getTypes = () => {
      HTTP.get("mail/type")
        .then((response) => {
          types.value = response.data;
        })
        .then()
        .catch(() => {
          hasLoadingError.value = true;
        });
    };
    onMounted(() => {
      getTypes();
      selectedSender.value = store.state.profile.userData.email || "";
    });
    return {
      hasRequestError,
      hasLoadingError,
      selectedSender,
      selectedType,
      types,
      shouldPublish,
      subject,
      mailText,
      sendMail,
      selectedList,
      show,
    };
  },
};
</script>
