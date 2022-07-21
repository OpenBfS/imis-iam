<template>
  <v-container>
    <v-row>
      <v-col cols="12" class="mt-10 pa-2 text-h6 bg-secondary">
        {{ $t("archive.title") }}
      </v-col>
    </v-row>
    <v-row class="mt-6">
      <!--TODO: Use @change event when this gets implemented by upstream -->
      <v-select
        class="ml-1"
        style="max-width: 40%"
        v-model="selectedFilter"
        :items="mailTypes"
        item-title="name"
        item-value="id"
        return-object
        multiple
        density="compact"
        :hint="$t('mailinglist.filter_by_type')"
        persistent-hint
      ></v-select>
      <v-tooltip location="top">
        <template v-slot:activator="{ props }">
          <v-btn
            v-bind="props"
            color="accent"
            class="mr-4 ml-auto"
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
            <tr v-if="mails && !mails.length">
              <td colspan="3">{{ $t("mailinglist.no_mails_available") }}</td>
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
import { onMounted, ref, defineAsyncComponent, computed, watch } from "vue";
import { HTTP } from "@/lib/http";
import { useNotification } from "@/lib/use-notification";
import { useStore } from "vuex";
import { useRoute } from "vue-router";
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
    const store = useStore();
    const route = useRoute();
    const { hasLoadingError } = useNotification();
    const getMails = () => {
      let date = "";
      switch (route.params.year) {
        case "2022":
          date += `start=${new Date(
            "2022-1-1"
          ).getTime()}&end=${new Date().getTime()}`;
          break;
        case "2021":
          date += `start=${new Date("2021-1-1").getTime()}&end=${new Date(
            "2021-12-31"
          ).getTime()}`;
          break;
        case "2020":
          date += `start=${new Date("2020-1-1").getTime()}&end=${new Date(
            "2020-12-31"
          ).getTime()}`;
          break;
      }
      date = date === "" ? date : "&" + date;
      let payload =
        date === "" ? "mail?archived=true" : "mail?archived=true" + date;
      if (selectedFilter.value && selectedFilter.value.length) {
        selectedFilter.value.forEach((t) => {
          payload += "&type=" + t.id;
        });
      }
      HTTP.get(payload)
        .then((response) => {
          mails.value = response.data;
        })
        .catch(() => {
          hasLoadingError.value = true;
        });
    };
    onMounted(() => {
      getMails();
      store
        .dispatch("mail/loadMailTypes")
        .then()
        .catch(() => {
          hasLoadingError.value = true;
        });
    });
    const selectedMail = ref();
    const showMailContent = ref(false);
    const checkChildObject = (e) => {
      if (e.closeDialog) {
        showMailContent.value = false;
      }
    };
    const mailTypes = computed(() => {
      return store.state.mail.mailTypes;
    });
    const selectedFilter = ref([]);
    watch(
      () => selectedFilter.value,
      () => {
        getMails();
      }
    );
    watch(
      () => route.params,
      (previousParams, toParams) => {
        if (toParams.year !== previousParams.year) {
          getMails();
        }
      }
    );

    return {
      mailTypes,
      selectedFilter,
      checkChildObject,
      selectedMail,
      showMailContent,
      mails,
      hasLoadingError,
    };
  },
};
</script>
