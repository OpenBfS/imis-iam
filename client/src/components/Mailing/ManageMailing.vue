<!--
 Copyright (C) 2022 by Bundesamt fuer Strahlenschutz
 Software engineering by Intevation GmbH

 This file is Free Software under the GNU GPL (v>=3)
 and comes with ABSOLUTELY NO WARRANTY!
 -->
<template>
  <v-dialog
    v-model="show"
    v-if="['add', 'edit'].indexOf(props.processType) !== -1"
  >
    <v-card width="400px">
      <v-card-title v-if="props.processType == 'add'">{{
        $t("mailinglist.add_mailing_list")
      }}</v-card-title>
      <v-card-title v-else
        >{{ props.item.name }} {{ $t("label.edit") }}
      </v-card-title>
      <v-divider></v-divider>
      <v-container>
        <v-row justify="center">
          <v-form v-model="valid" ref="form" class="v-col v-col-10">
            <TextField
              :label="$t('mailinglist.name')"
              :modelValue="listName"
              :attribute="'name'"
              required
              @update:modelValue="listName = $event"
            ></TextField>
            <Select
              attribute="users"
              :no-data-text="$t('label.no_data_text')"
              clearable
              :label="$t('mailinglist.recipient')"
              :items="users"
              v-model="selectedUsers"
              item-title="attributes.username[0]"
              item-value="id"
              persistent-hint
              multiple
              required
            ></Select>
            <v-label>* {{ $t("hints.required_fields") }}</v-label>
            <UIAlert
              v-if="hasRequestError"
              v-bind:isSuccessful="!hasRequestError"
              v-bind:message="applicationStore.httpErrorMessage"
            />
          </v-form>
        </v-row>
      </v-container>
      <v-divider></v-divider>
      <v-card-actions>
        <v-spacer></v-spacer>
        <v-btn
          v-if="props.processType == 'add'"
          size="small"
          @click="createMailList()"
          :disabled="!valid"
          color="accent"
        >
          {{ $t("button.create") }}
        </v-btn>
        <v-btn
          v-else
          size="small"
          @click="editMailList()"
          :disabled="!valid"
          color="accent"
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
    v-if="['delete', 'enter', 'exit'].indexOf(props.processType) !== -1"
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
              v-bind:message="applicationStore.httpErrorMessage"
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
<style lang="scss" scoped>
::v-deep(.v-card) {
  align-self: center;
}
</style>
<script setup>
import { ref, onBeforeMount, onMounted, computed } from "vue";
import { useNotification } from "@/lib/use-notification";
import { HTTP } from "@/lib/http";
import { useI18n } from "vue-i18n";
import { useApplicationStore } from "@/stores/application";
import { useUserStore } from "@/stores/user";
import { useForm } from "@/lib/use-form";
import TextField from "@/components/Form/TextField.vue";
import Select from "@/components/Form/Select.vue";
const props = defineProps({
  processType: String,
  item: Object,
});
const emit = defineEmits(["child-object"]);

const applicationStore = useApplicationStore();
const userStore = useUserStore();
const show = true;

const {
  form,
  valid,
  reqField,
  reqMultipleSelect,
  initClientRules,
  handleValidationErrorFromServer,
  isServerValidationError,
} = useForm();
const { t } = useI18n();
const { hasRequestError, hasLoadingError, resetNotification } =
  useNotification();
const listName = ref("");
const users = computed(() => {
  return userStore.users;
});
const selectedUsers = ref([]);
const getUsers = () => {
  userStore
    .loadUsers()
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
    .catch((error) => {
      isServerValidationError(error)
        ? handleValidationErrorFromServer(error.response.data)
        : (hasRequestError.value = true);
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
onBeforeMount(() => {
  applicationStore.setForm(form);
  initClientRules({
    name: reqField(t("mailinglist.required_mailinglist_name")),
    users: reqMultipleSelect(t("mailinglist.required_user")),
  });
});

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
    .catch((error) => {
      isServerValidationError(error)
        ? handleValidationErrorFromServer(error.response.data)
        : (hasRequestError.value = true);
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
</script>
