<template>
  <v-dialog v-model="show" v-if="processType === 'delete'">
    <v-card width="400px">
      <v-card-title>Delete {{ item.name }}</v-card-title>
      <v-divider></v-divider>
      <v-container>
        <v-row>
          <v-col cols="10">
            <span>Are you sure you want to delete {{ item.name }} ?</span>
          </v-col>
        </v-row>
      </v-container>
      <v-divider></v-divider>
      <v-card-actions>
        <v-spacer></v-spacer>
        <v-btn size="small" @click="deleteList()" color="accent">
          {{ $t("button.yes") }}
        </v-btn>
        <v-btn
          color="accent"
          size="small"
          @click="
            $emit('child-object', {
              closeDialog: true,
            })
          "
        >
          {{ $t("button.cancel") }}
        </v-btn>
      </v-card-actions>
    </v-card>
  </v-dialog>
  <v-dialog v-model="show" v-else>
    <v-card width="400px">
      <v-card-title v-if="processType == 'add'">Add Mailing list</v-card-title>
      <v-card-title v-else>Edit {{ item.name }} </v-card-title>
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
          v-if="processType == 'add'"
          size="small"
          @click="createMailList()"
          :color="listName === '' ? 'grey' : 'accent'"
          :disabled="listName === ''"
        >
          {{ $t("button.create") }}
        </v-btn>
        <v-btn
          v-else
          size="small"
          @click="editMailList()"
          :color="listName === '' ? 'grey' : 'accent'"
          :disabled="listName === ''"
        >
          {{ $t("button.save") }}
        </v-btn>
        <v-btn
          color="accent"
          size="small"
          @click="
            $emit('child-object', {
              closeDialog: true,
            })
          "
        >
          {{ $t("button.cancel") }}
        </v-btn>
      </v-card-actions>
    </v-card>
  </v-dialog>
</template>

<script>
import { ref, defineAsyncComponent, onMounted } from "vue";
import { useNotification } from "@/lib/use-notification";
import { HTTP } from "@/lib/http";

export default {
  props: {
    item: Object,
    processType: String,
  },
  components: {
    UIAlert: defineAsyncComponent(() => import("@/components/UI/UIAlert.vue")),
  },
  setup(props, { emit }) {
    const show = true;
    const { hasRequestError, resetNotification } = useNotification();
    const listName = ref("");
    const createMailList = () => {
      resetNotification();
      HTTP.post("mail/list", { name: listName.value })
        .then(() => {
          emit("child-object", { closeDialog: true, hasChanges: true });
          listName.value = "";
        })
        .catch(() => {
          hasRequestError.value = true;
        });
    };
    const deleteList = () => {
      HTTP.delete("mail/list/" + props.item.id)
        .then(() => {
          emit("child-object", { closeDialog: true, hasChanges: true });
        })
        .catch(() => {
          hasRequestError.value = true;
        });
    };
    // Edit
    onMounted(() => {
      if (props.processType === "edit") {
        listName.value = props.item.name;
      }
    });
    const editMailList = () => {
      HTTP.put("mail/list/", {
        id: props.item.id,
        name: listName.value,
      }).then(() => {
        emit("child-object", { closeDialog: true, hasChanges: true }).catch(
          () => {
            hasRequestError.value = true;
          }
        );
      });
    };
    return {
      editMailList,
      deleteList,
      createMailList,
      show,
      listName,
      hasRequestError,
    };
  },
};
</script>
