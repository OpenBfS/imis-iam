<!--
 Copyright (C) 2022 by Bundesamt fuer Strahlenschutz
 Software engineering by Intevation GmbH

 This file is Free Software under the GNU GPL (v>=3)
 and comes with ABSOLUTELY NO WARRANTY!
 -->
<template>
  <v-dialog v-model="show">
    <v-card
      width="60vw"
      max-height="80vh"
      class="overflow-y-auto align-self-center"
    >
      <v-layout row class="pa-2">
        <div
          class="d-flex flex-column mr-auto text-medium-emphasis text-body-2 ml-2"
        >
          <div>
            <span class="subheading mr-2">{{ $t("mailinglist.from") }}: </span>
            <span class="mr-1 mr-auto">{{ mail.sender }}</span>
          </div>
          <div>
            <span class="subheading mr-2">{{ $t("mailinglist.date") }} : </span>
            <span class="mr-1">{{
              new Date(mail.sendDate).toLocaleDateString()
            }}</span>
          </div>
          <div class="d-flex">
            <span class="subheading mr-2"
              >{{ $t("mailinglist.subject") }} :
            </span>
            <span class="mr-1">{{ mail.subject }}</span>
          </div>
        </div>
        <div class="d-flex align-self-start">
          <v-tooltip location="top">
            <template v-slot:activator="{ props }">
              <v-icon
                v-bind="props"
                style="opacity: 1"
                color="accent"
                size="x-large"
                icon="mdi-close-circle"
                @click="
                  $emit('child-object', {
                    closeDialog: true,
                  })
                "
              ></v-icon>
            </template>
            <span>{{ $t("button.close") }}</span>
          </v-tooltip>
        </div>
      </v-layout>
      <v-divider></v-divider>
      <v-card-text id="mail_content"> {{ mail.text }} </v-card-text>
    </v-card>
  </v-dialog>
</template>

<script>
export default {
  props: {
    mail: Object,
  },
  setup() {
    const show = true;

    return {
      show,
    };
  },
};
</script>

<style lang="scss" scoped>
#mail_content {
  white-space: pre-wrap;
}
</style>
