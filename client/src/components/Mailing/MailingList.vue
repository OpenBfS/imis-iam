<template>
  <v-container>
    <v-row>
      <v-col cols="12" class="mt-10 pa-2 text-h6 bg-secondary">
        {{ $t("mailinglist.title") }}
      </v-col>
    </v-row>
    <v-row justify="end" class="mt-6">
      <v-tooltip location="top">
        <template v-slot:activator="{ props }">
          <v-btn
            v-bind="props"
            size="small"
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
            <th class="text-left">Name</th>
            <th class="text-left">actions</th>
          </thead>
          <tbody>
            <tr v-for="list in mailingLists" :key="list.id">
              <td>{{ list.name }}</td>
              <td>
                <v-tooltip location="top">
                  <template v-slot:activator="{ props }">
                    <v-btn
                      variant="plain"
                      icon="mdi-account-edit-outline"
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
                <v-tooltip>
                  <template v-slot:activator="{ props }">
                    <v-btn
                      variant="plain"
                      icon="mdi-location-exit"
                      size="small"
                      v-bind="props"
                      :disabled="!isUserInList(list)"
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
          No mailing lists are available
        </div>
      </v-col>
      <v-col cols="4">
        <v-btn
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
        v-bind:message="$store.state.application.httpErrorMessage"
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

<script>
import { HTTP } from "@/lib/http";
import { useNotification } from "@/lib/use-notification";
import { onMounted, ref, defineAsyncComponent, computed } from "vue";
import { useStore } from "vuex";

export default {
  components: {
    UIAlert: defineAsyncComponent(() => import("../UI/UIAlert.vue")),
    ManageMailing: defineAsyncComponent(() =>
      import("@/components/Mailing/ManageMailing.vue")
    ),
    MailDialog: defineAsyncComponent(() =>
      import("@/components/Mailing/MailDialog.vue")
    ),
  },
  setup() {
    const store = useStore();
    const { hasLoadingError, resetNotification } = useNotification();
    const mailingLists = ref([]);

    const getMailLists = () => {
      resetNotification();
      HTTP.get("mail/list")
        .then((response) => {
          mailingLists.value = response.data;
        })
        .catch(() => {
          hasLoadingError.value = true;
        });
    };
    onMounted(() => {
      getMailLists();
      getMemberships();
    });
    const getMemberships = () => {
      store
        .dispatch("profile/getMyMailingLists")
        .then()
        .catch(() => {
          hasLoadingError.value = true;
        });
    };
    const showCreateDialog = ref(false);
    const listName = ref("");
    const showManagementDialog = ref(false);
    const deleteList = () => {};
    const editList = () => {};
    const selectedItem = ref({});
    const processType = ref({});
    const checkChildObject = (e) => {
      if (e.closeDialog) {
        showManagementDialog.value = false;
      }
      if (e.hasChanges) {
        getMailLists();
      }
    };
    const showMailDialog = ref(false);
    const checkMailDialogObject = (e) => {
      if (e.closeDialog) {
        showMailDialog.value = false;
      }
    };
    const myMailingLists = computed(() => {
      return store.state.profile.myMailingLists;
    });
    const isUserInList = (list) => {
      return myMailingLists.value.map((l) => l.id).some((r) => r === list.id);
    };

    return {
      isUserInList,
      checkMailDialogObject,
      showMailDialog,
      checkChildObject,
      showManagementDialog,
      selectedItem,
      processType,
      deleteList,
      editList,
      resetNotification,
      mailingLists,
      listName,
      showCreateDialog,
      hasLoadingError,
    };
  },
};
</script>
