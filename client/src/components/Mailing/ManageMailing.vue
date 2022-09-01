<!--
 Copyright (C) 2022 by Bundesamt fuer Strahlenschutz
 Software engineering by Intevation GmbH

 This file is Free Software under the GNU GPL (v>=3)
 and comes with ABSOLUTELY NO WARRANTY!
 -->
<template>
  <v-dialog v-model="show" v-if="['add', 'edit'].indexOf(processType) !== -1">
    <v-card width="400px">
      <v-card-title v-if="processType == 'add'">{{
        $t("mailinglist.add_mailing_list")
      }}</v-card-title>
      <v-card-title v-else
        >{{ item.name }} {{ $t("label.edit") }}
      </v-card-title>
      <v-divider></v-divider>
      <v-container>
        <v-row justify="center">
          <v-form v-model="valid" class="v-col v-col-10">
            <v-text-field
              variant="underlined"
              :label="$t('mailinglist.name')"
              v-model="listName"
            ></v-text-field>
            <v-select
              :no-data-text="$t('label.no_data_text')"
              dense
              clearable
              :label="$t('main.users')"
              :items="users"
              v-model="selectedUsers"
              item-title="username"
              item-value="id"
              persistent-hint
              multiple
              :rules="reqMultipleSelect($t('mailinglist.required_user'))"
            >
            </v-select>
            <UIAlert
              v-if="hasRequestError"
              v-bind:isSuccessful="!hasRequestError"
              v-bind:message="$store.state.application.httpErrorMessage"
            />
          </v-form>
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
  <v-dialog
    v-model="show"
    v-if="['delete', 'enter', 'exit'].indexOf(processType) !== -1"
  >
    <v-card width="400px">
      <v-card-title>{{ getTitle() }}</v-card-title>
      <v-divider></v-divider>
      <v-container>
        <v-row>
          <v-col cols="10">
            <span>{{ $t("mailinglist.confirmation_message") }}</span>
          </v-col>
          <v-col>
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
        <v-btn size="small" @click="checkDeleteEnterExit()" color="accent">
          {{ $t("button.confirm") }}
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
import { ref, defineAsyncComponent, onMounted, computed } from "vue";
import { useNotification } from "@/lib/use-notification";
import { HTTP } from "@/lib/http";
import { useI18n } from "vue-i18n";
import { useStore } from "vuex";
import { useForm } from "@/lib/use-form";
export default {
  props: {
    item: Object,
    processType: String,
  },
  emits: ["child-object"],
  components: {
    UIAlert: defineAsyncComponent(() => import("@/components/UI/UIAlert.vue")),
  },
  setup(props, { emit }) {
    const store = useStore();
    const show = true;

    const { valid, reqField, reqMultipleSelect } = useForm();
    const { t } = useI18n();
    const { hasRequestError, hasLoadingError, resetNotification } =
      useNotification();
    const listName = ref("");
    const users = computed(() => {
      return store.state.user.users;
    });
    const selectedUsers = ref([]);
    const getUsers = () => {
      store
        .dispatch("user/loadUsers")
        .then()
        .catch(() => {
          hasLoadingError.value = true;
        });
    };
    const createMailList = () => {
      resetNotification();
      HTTP.post("mail/list", {
        name: listName.value,
        users: selectedUsers.value,
      })
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
      getUsers();
      if (props.processType === "edit") {
        listName.value = props.item.name;
        selectedUsers.value = props.item.users;
      }
    });
    const editMailList = () => {
      HTTP.put("mail/list/", {
        id: props.item.id,
        name: listName.value,
        users: selectedUsers.value,
      })
        .then(() => {
          emit("child-object", { closeDialog: true, hasChanges: true });
        })
        .catch(() => {
          hasRequestError.value = true;
        });
    };
    // Enter/Exit Mailing-list
    const enterMailingList = () => {
      HTTP.get("mail/list/" + props.item.id + "/join")
        .then(() => {
          emit("child-object", { closeDialog: true, hasChanges: true });
        })
        .catch(() => {
          hasRequestError.value = true;
        });
    };
    const exitMailingList = () => {
      HTTP.get("mail/list/" + props.item.id + "/leave")
        .then(() => {
          emit("child-object", { closeDialog: true, hasChanges: true });
        })
        .catch(() => {
          hasRequestError.value = true;
        });
    };
    const checkDeleteEnterExit = () => {
      switch (props.processType) {
        case "delete": {
          deleteList();
          break;
        }
        case "enter": {
          enterMailingList();
          break;
        }
        case "exit": {
          exitMailingList();
          break;
        }
      }
    };
    const getTitle = () => {
      switch (props.processType) {
        case "delete":
          return props.item.name + " " + t("label.delete");
        case "enter":
          return props.item.name + " " + t("mailinglist.enter");
        case "exit":
          return props.item.name + " " + t("mailinglist.exit");
      }
    };

    return {
      reqField,
      reqMultipleSelect,
      valid,
      users,
      selectedUsers,
      getTitle,
      checkDeleteEnterExit,
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
