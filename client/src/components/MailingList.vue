<template>
  <v-container>
    <v-row>
      <v-col cols="12" class="mt-10 pa-2 text-h6 bg-secondary">
        {{ $t("mailinglist.title") }}
      </v-col>
    </v-row>
    <v-row>
      <v-col cols="10" class="mt-10">
        <span class="mb-4 text-subtitle-1 font-weight-medium"
          >Mailing Lists</span
        >
        <div class="ml-2" v-for="list in mailLists" :key="list.id">
          <span>{{ list.name }}</span>
        </div>
        <div v-if="!hasLoadingError && mailLists.length == 0">
          <!-- {{ $t("No mailing lists are available") }} -->
          No mailing lists are available
        </div>
      </v-col>
      <v-col cols="4">
        <v-btn
          color="accent"
          @click="
            resetNotification();
            showCreateDialog = true;
          "
          >Add Mailing-List</v-btn
        >
      </v-col>
      <UIAlert
        v-if="hasLoadingError"
        v-bind:isSuccessful="!hasLoadingError"
        v-bind:message="$store.state.application.httpErrorMessage"
      />
    </v-row>
    <v-dialog v-model="showCreateDialog">
      <v-card width="400px">
        <v-card-title>Add Mailing list</v-card-title>
        <v-divider></v-divider>
        <v-container>
          <v-row>
            <v-col cols="10">
              <v-text-field
                variant="underlined"
                :label="$t('mailinglist.name')"
                v-model="listName"
              ></v-text-field>
              <UIAlert
                v-if="hasRequestError"
                v-bind:isSuccessful="!hasRequestError"
                v-bind:message="$store.state.application.httpErrorMessage"
              />
            </v-col>
          </v-row>
        </v-container>
        <v-divider></v-divider>
        <v-card-actions>
          <v-spacer></v-spacer>
          <v-btn
            size="small"
            @click="createMailList()"
            :color="listName === '' ? 'grey' : 'accent'"
            :disabled="listName === ''"
          >
            {{ $t("button.create") }}
          </v-btn>
          <v-btn
            color="accent"
            size="small"
            @click="
              listName = '';
              showCreateDialog = false;
            "
          >
            {{ $t("button.cancel") }}
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>
  </v-container>
</template>

<script>
import { HTTP } from "@/lib/http";
import { useNotification } from "@/lib/use-notification";
import { onMounted, ref, defineAsyncComponent } from "vue";

export default {
  components: {
    UIAlert: defineAsyncComponent(() => import("../components/UI/UIAlert.vue")),
  },
  setup() {
    /*  const mailList = [
      { name: "maillist-1", id: 1 },
      { name: "maillist-2", id: 2 },
    ]; */
    const { hasRequestError, hasLoadingError, resetNotification } =
      useNotification();
    const mailLists = ref([]);

    const getMailLists = () => {
      resetNotification();
      HTTP.get("mail/list")
        .then((response) => {
          mailLists.value = response.data;
          console.log(response.data);
        })
        .catch(() => {
          hasLoadingError.value = true;
        });
    };
    onMounted(() => {
      getMailLists();
    });

    const showCreateDialog = ref(false);
    const listName = ref("");
    const createMailList = () => {
      resetNotification();
      HTTP.post("mail/list", { name: listName.value })
        .then(() => {
          showCreateDialog.value = false;
          getMailLists();
          listName.value = "";
        })
        .catch(() => {
          hasRequestError.value = true;
        });
    };
    return {
      createMailList,
      resetNotification,
      hasRequestError,
      mailLists,
      listName,
      showCreateDialog,
      hasLoadingError,
    };
  },
};
</script>
