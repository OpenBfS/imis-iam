<template>
  <v-container>
    <v-row>
      <v-col cols="10">
        <div class="text-h6">{{ $t("mailinglist.failure_report") }}</div>
        <v-card
          max-height="150px"
          class="my-2"
          color="#E57373"
          density="compact"
          v-for="mail in failureMails
            .sort((a, b) => {
              return b.sendDate - a.sendDate;
            })
            .slice(0, 2)"
          :key="mail.id"
        >
          <v-card-header>
            <div>
              <div class="d-flex flex-row">
                <div class="mb-1 subtitle-2">
                  {{ mail.subject }}
                </div>
                <div class="ml-4 text-caption">
                  {{ new Date(mail.sendDate).toLocaleDateString() }}
                </div>
              </div>
              <p class="text-caption mt-4">
                {{ mail.text }}
              </p>
            </div>
          </v-card-header>
        </v-card>
        <div
          class="ml-4 text-caption"
          v-if="failureMails && failureMails.length === 0"
        >
          {{ $t("mailinglist.no_mails_available") }}
        </div>
        <div class="text-h6 mt-4">
          {{ $t("mailinglist.maintenance_report") }}
        </div>
        <v-card
          max-height="150px"
          class="my-2"
          color="#E0E0E0"
          density="compact"
          v-for="mail in maintenanceMails
            .sort((a, b) => {
              return b.sendDate - a.sendDate;
            })
            .slice(0, 2)"
          :key="mail.id"
        >
          <v-card-header>
            <div>
              <div class="d-flex flex-row">
                <div class="mb-1 text-subtitle-2">
                  {{ mail.subject }}
                </div>
                <div class="ml-4 text-caption">
                  {{ new Date(mail.sendDate).toLocaleDateString() }}
                </div>
              </div>
              <p class="text-caption mt-4">
                {{ mail.text }}
              </p>
            </div>
          </v-card-header>
        </v-card>
        <div
          class="ml-4 text-caption"
          v-if="maintenanceMails && maintenanceMails.length === 0"
        >
          {{ $t("mailinglist.no_mails_available") }}
        </div>
      </v-col>
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
import { ref, onMounted } from "vue";
export default {
  setup() {
    const maintenanceMails = ref([]);
    const failureMails = ref([]);
    const { hasLoadingError } = useNotification();
    const getFailureMails = () => {
      HTTP.get("mail?type=3")
        .then((response) => {
          failureMails.value = response.data;
        })
        .catch(() => {
          hasLoadingError.value = true;
        });
    };
    const getMaintenanceMails = () => {
      HTTP.get("mail?type=4")
        .then((response) => {
          maintenanceMails.value = response.data;
        })
        .catch(() => {
          hasLoadingError.value = true;
        });
    };
    onMounted(() => {
      getFailureMails();
      getMaintenanceMails();
    });

    return {
      maintenanceMails,
      failureMails,
    };
  },
};
</script>
