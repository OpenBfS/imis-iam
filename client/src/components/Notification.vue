<!--
 Copyright (C) 2022 by Bundesamt fuer Strahlenschutz
 Software engineering by Intevation GmbH

 This file is Free Software under the GNU GPL (v>=3)
 and comes with ABSOLUTELY NO WARRANTY!
 -->
<template>
  <v-container>
    <v-row>
      <v-col cols="10">
        <div class="text-h6">{{ $t("mailinglist.failure_maintenance") }}</div>
        <div
          class="d-flex justify-start align-center"
          v-for="mail in criticalMails"
          :key="mail.id"
        >
          <v-card
            @click="
              selectedMail = mail;
              showMailContent = true;
            "
            max-height="100px"
            class="my-2 v-col v-col-10"
            color="#E57373"
            density="compact"
          >
            <v-card-title>
              <div>
                <div class="d-flex flex-row align-center">
                  <div class="subtitle-2">
                    {{ mail.subject }}
                  </div>
                  <div class="ml-4 text-caption">
                    {{ new Date(mail.sendDate).toLocaleDateString() }}
                  </div>
                </div>
              </div>
            </v-card-title>
          </v-card>
          <v-tooltip location="top">
            <template v-slot:activator="{ props }">
              <v-btn
                v-bind="props"
                color="#E57373"
                class="ml-2"
                icon="mdi-archive-arrow-up"
                @click="archiveMail(mail.id)"
              >
              </v-btn>
            </template>
            <span>{{ $t("mailinglist.archive") }}</span>
          </v-tooltip>
        </div>
        <div
          class="ml-4 text-caption"
          v-if="criticalMails && criticalMails.length === 0"
        >
          {{ $t("mailinglist.no_mails_available") }}
        </div>

        <div class="text-h6 mt-4">
          {{ $t("mailinglist.current") }}
        </div>
        <div
          class="d-flex justify-start align-center"
          v-for="mail in otherMails"
          :key="mail.id"
        >
          <v-card
            @click="
              selectedMail = mail;
              showMailContent = true;
            "
            max-height="150px"
            class="my-2 v-col v-col-10"
            color="#E0E0E0"
            density="compact"
          >
            <v-card-title>
              <div>
                <div class="d-flex flex-row align-center">
                  <div class="text-subtitle-2">
                    {{ mail.subject }}
                  </div>
                  <div class="ml-4 text-caption">
                    {{ new Date(mail.sendDate).toLocaleDateString() }}
                  </div>
                </div>
              </div>
            </v-card-title>
          </v-card>
          <v-tooltip location="top">
            <template v-slot:activator="{ props }">
              <v-btn
                v-bind="props"
                color="#E0E0E0"
                class="ml-2"
                icon="mdi-archive-arrow-up"
                @click="archiveMail(mail.id)"
              >
              </v-btn>
            </template>
            <span>{{ $t("mailinglist.archive") }}</span>
          </v-tooltip>
        </div>
        <div
          class="ml-4 text-caption"
          v-if="otherMails && otherMails.length === 0"
        >
          {{ $t("mailinglist.no_mails_available") }}
        </div>
      </v-col>
      <MailContent
        v-if="showMailContent"
        v-bind:mail="selectedMail"
        @child-object="checkChildObject"
      />
      <UIAlert
        v-if="hasRequestError"
        v-bind:isSuccessful="!hasRequestError"
        v-bind:message="$store.state.application.httpErrorMessage"
      />
    </v-row>
  </v-container>
</template>
<style lang="scss" scoped>
p {
  white-space: pre-wrap;
}
.v-card {
  overflow-y: auto;
}
</style>
<script>
import { HTTP } from "@/lib/http";
import { useNotification } from "@/lib/use-notification";
import { ref, onMounted, defineAsyncComponent } from "vue";
export default {
  components: {
    MailContent: defineAsyncComponent(() =>
      import("@/components/Mailing/MailContent.vue")
    ),
    UIAlert: defineAsyncComponent(() => import("@/components/UI/UIAlert.vue")),
  },
  setup() {
    const otherMails = ref([]);
    const criticalMails = ref([]);
    const { hasRequestError, resetNotification } = useNotification();
    const getOtherMails = () => {
      HTTP.get("mail?count=2&type=1&type=2&type=5&type=6&type=7&type=8")
        .then((response) => {
          otherMails.value = response.data;
        })
        .catch(() => {
          hasRequestError.value = true;
        });
    };
    const getCriticalMails = () => {
      HTTP.get("mail?count=2&type=3&type=4")
        .then((response) => {
          criticalMails.value = response.data;
        })
        .catch(() => {
          hasRequestError.value = true;
        });
    };
    const showMailContent = ref(false);
    const selectedMail = ref();
    const showMail = () => {
      showMailContent.value = true;
    };
    onMounted(() => {
      getCriticalMails();
      getOtherMails();
    });
    const checkChildObject = (e) => {
      if (e.closeDialog) {
        showMailContent.value = false;
      }
    };
    const archiveMail = (id) => {
      resetNotification();
      HTTP.get("mail/archive/" + id)
        .then(() => {
          getCriticalMails();
          getOtherMails();
        })
        .catch(() => {
          hasRequestError.value = true;
        });
    };
    return {
      hasRequestError,
      archiveMail,
      checkChildObject,
      showMailContent,
      selectedMail,
      showMail,
      otherMails,
      criticalMails,
    };
  },
};
</script>
