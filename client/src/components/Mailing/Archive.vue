<template>
  <v-container>
    <v-row>
      <v-col cols="12" class="mt-10 pa-2 text-h6 bg-secondary">
        {{ $t("archive.title") }}
      </v-col>
    </v-row>
    <v-row justify="end" class="mt-6">
      <v-tooltip location="top">
        <template v-slot:activator="{ props }">
          <v-btn
            v-bind="props"
            color="accent"
            class="mr-4"
            icon="mdi-magnify"
            disabled
          >
          </v-btn>
        </template>
        <span>{{ $t("archive.search") }}</span>
      </v-tooltip>
    </v-row>
    <v-row>
      <v-col cols="12" class="mt-6">
        <v-table class="pa-2 ma-2" density="compact">
          <thead>
            <th class="text-left">{{ $t("mailinglist.subject") }}</th>
            <th class="text-left">{{ $t("mailinglist.sender") }}</th>
            <th class="text-left">{{ $t("mailinglist.date") }}</th>
          </thead>
          <tbody>
            <tr
              v-for="mail in mails"
              :key="mail.id"
              @click="
                selectedMail = mail;
                showMailContent = true;
              "
            >
              <td>{{ mail.subject }}</td>
              <td>{{ mail.sender }}</td>
              <td>{{ new Date(mail.sendDate).toLocaleDateString() }}</td>
            </tr>
          </tbody>
        </v-table>

        <UIAlert
          v-if="hasLoadingError"
          v-bind:isSuccessful="!hasLoadingError"
          v-bind:message="$store.state.application.httpErrorMessage"
        />
      </v-col>
    </v-row>
  </v-container>
  <MailContent
    v-if="showMailContent"
    v-bind:mail="selectedMail"
    @child-object="checkChildObject"
  />
</template>
<style scoped>
tr {
  cursor: pointer;
}
.v-table > .v-table__wrapper > table > tbody > tr:hover {
  background-color: darkgray;
}
</style>
<script>
import { onMounted, ref, defineAsyncComponent } from "vue";
import { HTTP } from "@/lib/http";
import { useNotification } from "@/lib/use-notification";
/* Copyright (C) 2022 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY!
 */

export default {
  components: {
    UIAlert: defineAsyncComponent(() => import("../UI/UIAlert.vue")),
    MailContent: defineAsyncComponent(() =>
      import("@/components/Mailing/MailContent.vue")
    ),
  },
  setup() {
    const mails = ref([]);
    const { hasLoadingError } = useNotification();
    const getMails = () => {
      HTTP.get("mail?archived=true")
        .then((response) => {
          mails.value = response.data;
        })
        .catch(() => {
          hasLoadingError.value = true;
        });
    };
    onMounted(() => {
      getMails();
    });
    const selectedMail = ref();
    const showMailContent = ref(false);
    const checkChildObject = (e) => {
      if (e.closeDialog) {
        showMailContent.value = false;
      }
    };
    return {
      checkChildObject,
      selectedMail,
      showMailContent,
      mails,
      hasLoadingError,
    };
  },
};
</script>
