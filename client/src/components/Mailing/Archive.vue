<!--
 Copyright (C) 2022 by Bundesamt fuer Strahlenschutz
 Software engineering by Intevation GmbH

 This file is Free Software under the GNU GPL (v>=3)
 and comes with ABSOLUTELY NO WARRANTY!
 -->
<template>
  <v-container>
    <UIHeader>
      {{ $t("archive.title") }}
    </UIHeader>
    <v-row class="mt-6" align="center">
      <!--TODO: Use @change event when this gets implemented by upstream -->
      <v-select
        :no-data-text="$t('label.no_data_text')"
        class="mx-1"
        style="max-width: 20%"
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
      <v-select
        :no-data-text="$t('label.no_data_text')"
        class="mx-1 text-truncate"
        style="max-width: 20%"
        v-model="selectedMailinglist"
        :items="mailinglists"
        item-title="name"
        item-value="id"
        return-object
        multiple
        density="compact"
        :hint="$t('mailinglist.filter_by_maillist')"
        persistent-hint
      ></v-select>
      <!-- TODO: Use the v-date-picker from vuetify when this gets implemented -->
      <div class="d-flex mx-3 flex-column" style="width: 20%">
        <div class="d-flex">
          <label class="v-col-4" for="from">{{ $t("label.from") }}</label
          ><input
            class="ml-2"
            type="date"
            name="startDate"
            v-model="startDate"
          />
        </div>
        <div class="d-flex">
          <label for="to" class="v-col-4">{{ $t("label.to") }}</label
          ><input class="ml-2" type="date" name="endDate" v-model="endDate" />
        </div>
      </div>
      <!-- TODO: Check if the dinsty attribute is implemented for the
      v-select element to use the same density for all components. -->
      <v-text-field
        density="comfortable"
        class="mx-1"
        variant="underlined"
        style="max-width: 20%"
        :label="$t('mailinglist.filter_by_sender')"
        v-model="sender"
      ></v-text-field>
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
import { debounce } from "debounce";

export default {
  components: {
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
      if (startDate.value != "") {
        date += `start=${new Date(startDate.value).getTime()}`;
      }
      if (endDate.value != "") {
        date = date != "" ? date + "&" : date;
        date += `end=${new Date(endDate.value).getTime()}`;
      }
      date = date === "" ? date : "&" + date;

      let payload =
        date === "" ? "mail?archived=true" : "mail?archived=true" + date;
      if (sender.value) {
        payload += "&sender=" + sender.value;
      }
      if (selectedMailinglist.value && selectedMailinglist.value.length) {
        selectedMailinglist.value.forEach((l) => {
          payload += "&list=" + l.id;
        });
      }
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
      setStartAndEndDate();
      getMails();
      store
        .dispatch("mail/loadMailTypes")
        .then()
        .catch(() => {
          hasLoadingError.value = true;
        });
      store
        .dispatch("mail/loadMailinglists")
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
    // Filters
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
    const currentYear = new Date().getFullYear();
    // Format date yyyy-mm-dd for the date picker
    const formatDateToDisplay = (date) => {
      const year = date.getFullYear();
      let month = date.getMonth() + 1;
      month = month < 10 ? "0" + month : month;
      let day = date.getDate();
      day = day < 10 ? "0" + day : day;
      return year + "-" + month + "-" + day;
    };
    const setStartAndEndDate = () => {
      switch (Number(route.params.year)) {
        case currentYear:
          startDate.value = formatDateToDisplay(
            new Date(currentYear + "-01-01")
          );
          endDate.value = formatDateToDisplay(new Date(currentYear + "-12-31"));
          break;
        case currentYear - 1:
          startDate.value = formatDateToDisplay(
            new Date(currentYear - 1 + "-01-01")
          );
          endDate.value = formatDateToDisplay(
            new Date(currentYear - 1 + "-12-31")
          );
          break;
        case currentYear - 2:
          startDate.value = formatDateToDisplay(
            new Date(currentYear - 2 + "-01-01")
          );
          endDate.value = formatDateToDisplay(
            new Date(currentYear - 2 + "-12-31")
          );
          break;
      }
    };
    watch(
      () => route.params,
      (previousParams, toParams) => {
        if (toParams.year !== previousParams.year) {
          setStartAndEndDate(route.params.year);
          getMails();
        }
      }
    );
    const startDate = ref("");
    const endDate = ref("");
    watch([() => endDate.value, () => startDate.value], () => {
      getMails();
    });
    const sender = ref("");
    const triggerFilter = debounce(() => {
      getMails();
    }, 500);
    watch(
      () => sender.value,
      (oldValue, newValue) => {
        if (oldValue !== newValue) {
          triggerFilter();
        }
      }
    );
    const selectedMailinglist = ref([]);
    const mailinglists = computed(() => {
      return store.state.mail.mailingLists;
    });

    watch(
      () => selectedMailinglist.value,
      (oldValue, newValue) => {
        if (oldValue !== newValue) {
          getMails();
        }
      }
    );
    return {
      currentYear,
      mailinglists,
      selectedMailinglist,
      sender,
      endDate,
      startDate,
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
