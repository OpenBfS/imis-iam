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
        <!-- Failure Mails -->
        <div class="text-h6">{{ $t("emails.failure") }}</div>
        <div
          class="d-flex justify-start align-center"
          v-for="mail in failureMails"
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
                v-if="profileStore.isAllowedToManage"
                v-bind="props"
                color="#E57373"
                class="ml-2"
                icon="mdi-archive-arrow-up"
                @click="archiveMail(mail.id, mail.type)"
              >
              </v-btn>
            </template>
            <span>{{ $t("emails.archive") }}</span>
          </v-tooltip>
        </div>
        <div
          class="ml-4 text-caption"
          v-if="failureMails && failureMails.length === 0"
        >
          {{ $t("emails.noMailsAvailable") }}
        </div>
        <!-- maintenance Mails -->
        <div class="text-h6 mt-4">
          {{ $t("emails.maintenance") }}
        </div>
        <div
          class="d-flex justify-start align-center"
          v-for="mail in maintenanceMails"
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
                v-if="profileStore.isAllowedToManage"
                v-bind="props"
                color="#E0E0E0"
                class="ml-2"
                icon="mdi-archive-arrow-up"
                @click="archiveMail(mail.id, mail.type)"
              >
              </v-btn>
            </template>
            <span>{{ $t("emails.archive") }}</span>
          </v-tooltip>
        </div>
        <div
          class="ml-4 text-caption"
          v-if="maintenanceMails && maintenanceMails.length === 0"
        >
          {{ $t("emails.noMailsAvailable") }}
        </div>
        <!-- Rest Mails -->
        <div class="text-h6 mt-4">
          {{ $t("emails.current") }}
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
                v-if="profileStore.isAllowedToManage"
                v-bind="props"
                color="#E0E0E0"
                class="ml-2"
                icon="mdi-archive-arrow-up"
                @click="archiveMail(mail.id, mail.type)"
              >
              </v-btn>
            </template>
            <span>{{ $t("emails.archive") }}</span>
          </v-tooltip>
        </div>
        <div
          class="ml-4 text-caption"
          v-if="otherMails && otherMails.length === 0"
        >
          {{ $t("emails.noMailsAvailable") }}
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
        v-bind:message="applicationStore.httpErrorMessage"
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
<script setup>
import { HTTP } from "@/lib/http.js";
import { useNotification } from "@/lib/use-notification.js";
import { useApplicationStore } from "@/stores/application.js";
import { useProfileStore } from "@/stores/profile.js";
import { ref, onMounted } from "vue";

const applicationStore = useApplicationStore();
const profileStore = useProfileStore();
const { hasRequestError, resetNotification } = useNotification();
const showMailContent = ref(false);
const selectedMail = ref();

// Mails
const otherMails = ref([]);
const failureMails = ref([]);
const maintenanceMails = ref([]);
const restTypes = [1, 2, 5, 6, 7, 8];
const getMailsbyTypes = (types, count) => {
  let path = "iam/mail?";
  if (count) {
    path += "count=" + count;
  }
  types.forEach((t) => {
    path += "&type=" + t;
  });
  HTTP.get(path)
    .then((response) => {
      switch (types[0]) {
        case 3:
          failureMails.value = response.data;
          break;
        case 4:
          maintenanceMails.value = response.data;
          break;
        default:
          otherMails.value = response.data;
      }
    })
    .catch(() => {
      hasRequestError.value = true;
    });
};
onMounted(() => {
  getMailsbyTypes([3]);
  getMailsbyTypes([4]);
  getMailsbyTypes(restTypes, 2);
});
const checkChildObject = (e) => {
  if (e.closeDialog) {
    showMailContent.value = false;
  }
};
const archiveMail = (id, type) => {
  resetNotification();
  HTTP.get("iam/mail/archive/" + id)
    .then(() => {
      if ([3, 4].indexOf(type) !== -1) {
        getMailsbyTypes([type]);
      } else {
        getMailsbyTypes(restTypes);
      }
    })
    .catch(() => {
      hasRequestError.value = true;
    });
};
</script>
