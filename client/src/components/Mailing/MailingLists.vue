<!--
 Copyright (C) 2022 by Bundesamt fuer Strahlenschutz
 Software engineering by Intevation GmbH

 This file is Free Software under the GNU GPL (v>=3)
 and comes with ABSOLUTELY NO WARRANTY!
 -->
<template>
  <v-container>
    <UIHeader>
      {{ $t("mailinglist.title") }}
    </UIHeader>
    <v-row justify="end" class="mt-6">
      <v-tooltip location="top">
        <template v-slot:activator="{ props }">
          <v-btn
            v-if="isAllowedToManage"
            v-bind="props"
            color="accent"
            class="mr-4"
            icon="mdi-email-plus"
            @click="
              resetNotification();
              showMailDialog = true;
            "
          >
          </v-btn>
        </template>
        <span>{{ $t("button.new_email") }}</span>
      </v-tooltip>
    </v-row>
    <v-row>
      <v-col cols="10" class="mt-6">
        <v-table class="ma-2 pa-2">
          <thead>
            <th class="text-left">{{ $t("label.name") }}</th>
            <th class="text-left">{{ $t("label.actions") }}</th>
          </thead>
          <tbody>
            <tr v-for="list in mailingLists" :key="list.id">
              <td>{{ list.name }}</td>
              <td class="d-flex">
                <v-tooltip location="top">
                  <template v-slot:activator="{ props }">
                    <v-btn
                      v-if="isAllowedToManage"
                      variant="plain"
                      icon="mdi-pencil"
                      size="small"
                      v-bind="props"
                      @click="
                        resetNotification();
                        processType = 'edit';
                        selectedItem = list;
                        showManagementDialog = true;
                      "
                    ></v-btn>
                  </template>
                  <span>{{ $t("label.edit") }}</span>
                </v-tooltip>
                <v-tooltip location="top">
                  <template v-slot:activator="{ props }">
                    <v-btn
                      v-if="isAllowedToManage"
                      variant="plain"
                      icon="mdi-delete"
                      size="small"
                      v-bind="props"
                      @click="
                        resetNotification();
                        processType = 'delete';
                        selectedItem = list;
                        showManagementDialog = true;
                      "
                    ></v-btn>
                  </template>
                  <span>{{ $t("label.delete") }}</span>
                </v-tooltip>
                <v-tooltip location="top">
                  <template v-slot:activator="{ props }">
                    <v-btn
                      variant="plain"
                      icon="mdi-location-enter"
                      size="small"
                      :disabled="isUserInList(list)"
                      v-bind="props"
                      @click="
                        resetNotification();
                        selectedItem = list;
                        processType = 'enter';
                        showManagementDialog = true;
                      "
                    ></v-btn>
                  </template>
                  <span>{{ $t("mailinglist.enter_mailinglist") }}</span>
                </v-tooltip>
                <v-tooltip location="top">
                  <template v-slot:activator="{ props }">
                    <v-btn
                      variant="plain"
                      icon="mdi-location-exit"
                      size="small"
                      v-bind="props"
                      :disabled="!isUserInList(list) || list.users.length <= 1"
                      @click="
                        selectedItem = list;
                        processType = 'exit';
                        resetNotification();
                        showManagementDialog = true;
                      "
                    ></v-btn>
                  </template>
                  <span>{{ $t("mailinglist.exit_mailinglist") }}</span>
                </v-tooltip>
              </td>
            </tr>
          </tbody>
        </v-table>
        <div v-if="!hasLoadingError && mailingLists.length == 0">
          {{ $t("mailinglist.no_mailing_list") }}
        </div>
      </v-col>
      <v-col cols="4">
        <v-btn
          v-if="isAllowedToManage"
          size="small"
          color="accent"
          @click="
            resetNotification();
            processType = 'add';
            showManagementDialog = true;
          "
        >
          {{ $t("mailinglist.add_mailing_list") }}</v-btn
        >
      </v-col>
      <UIAlert
        v-if="hasLoadingError"
        v-bind:isSuccessful="!hasLoadingError"
        v-bind:message="applicationStore.httpErrorMessage"
      />
    </v-row>
    <ManageMailing
      v-if="showManagementDialog"
      v-bind:processType="processType"
      v-bind:item="selectedItem"
      @child-object="checkChildObject"
    />
    <MailDialog
      v-if="showMailDialog"
      v-bind:mailingLists="mailingLists"
      @mail-dialog-object="checkMailDialogObject"
    />
  </v-container>
</template>

<script setup>
import { useNotification } from "@/lib/use-notification";
import { onMounted, ref, defineAsyncComponent, computed } from "vue";
import { useApplicationStore } from "@/stores/application";
import { useMailStore } from "@/stores/mail";
import { useProfileStore } from "@/stores/profile";

const ManageMailing = defineAsyncComponent(() =>
  import("@/components/Mailing/ManageMailing.vue")
);
const MailDialog = defineAsyncComponent(() =>
  import("@/components/Mailing/MailDialog.vue")
);
const applicationStore = useApplicationStore();
const mailStore = useMailStore();
const profileStore = useProfileStore();
const { hasLoadingError, resetNotification } = useNotification();
const mailingLists = computed(() => {
  return mailStore.mailingLists;
});
const isAllowedToManage = computed(() => {
  return profileStore.isAllowedToManage;
});
const getMailLists = () => {
  resetNotification();
  mailStore
    .loadMailinglists()
    .then()
    .catch(() => {
      hasLoadingError.value = true;
    });
};
onMounted(() => {
  getMailLists();
  getMyMailinglist();
});

const getMyMailinglist = () => {
  profileStore
    .getMyMailingLists()
    .then()
    .catch(() => {
      hasLoadingError.value = true;
    });
};
const showManagementDialog = ref(false);
const selectedItem = ref({});
const processType = ref({});
const checkChildObject = (e) => {
  if (e.closeDialog) {
    showManagementDialog.value = false;
  }
  if (e.hasChanges) {
    getMailLists();
    getMyMailinglist();
  }
};
const showMailDialog = ref(false);
const checkMailDialogObject = (e) => {
  if (e.closeDialog) {
    showMailDialog.value = false;
  }
};
const myMailingLists = computed(() => {
  return profileStore.myMailingLists;
});
const isUserInList = (list) => {
  return myMailingLists.value.map((l) => l.id).some((r) => r === list.id);
};
</script>
