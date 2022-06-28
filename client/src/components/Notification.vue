<template>
  <v-container>
    <v-row>
      <v-col cols="10">
        <div class="text-h6">{{ $t("mailinglist.failure_maintenance") }}</div>
        <v-card
          @click="
            selectedMail = mail;
            showMailContent = true;
          "
          max-height="100px"
          class="my-2"
          color="#E57373"
          density="compact"
          v-for="mail in criticalMails
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
          v-if="criticalMails && criticalMails.length === 0"
        >
          {{ $t("mailinglist.no_mails_available") }}
        </div>
        <div class="text-h6 mt-4">
          {{ $t("mailinglist.current") }}
        </div>
        <v-card
          @click="
            selectedMail = mail;
            showMailContent = true;
          "
          max-height="150px"
          class="my-2"
          color="#E0E0E0"
          density="compact"
          v-for="mail in otherMails
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
  },
  setup() {
    const otherMails = ref([]);
    const criticalMails = ref([]);
    const { hasLoadingError } = useNotification();
    const getcriticalMails = () => {
      HTTP.get("mail?type=1&type=2&type=3&type=4&type=5&type=6&type=7&type=8")
        .then((response) => {
          criticalMails.value = response.data.filter(
            (m) => m.type === 3 || m.type === 4
          );
          otherMails.value = response.data.filter(
            (m) => m.type !== 3 || m.type !== 3
          );
        })
        .catch(() => {
          hasLoadingError.value = true;
        });
    };
    const showMailContent = ref(false);
    const selectedMail = ref();
    const showMail = () => {
      showMailContent.value = true;
    };
    onMounted(() => {
      getcriticalMails();
    });
    const checkChildObject = (e) => {
      if (e.closeDialog) {
        showMailContent.value = false;
      }
    };
    return {
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
