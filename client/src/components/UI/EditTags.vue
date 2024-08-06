<!--
 Copyright (C) 2024 by Bundesamt fuer Strahlenschutz
 Software engineering by Intevation GmbH

 This file is Free Software under the GNU GPL (v>=3)
 and comes with ABSOLUTELY NO WARRANTY!
 -->
<template>
  <v-dialog :model-value="props.isActive">
    <v-card
      width="50vw"
      class="mx-auto"
      :title="
        props.type === 'institutions'
          ? $t('search.edit_tags_institutions')
          : $t('search.edit_tags_users')
      "
    >
      <v-card-text>
        <v-form v-model="valid" ref="form" class="v-col v-col-10">
          <Select
            v-if="props.type === 'institutions'"
            attribute="tags"
            :no-data-text="$t('label.no_data_text')"
            :label="$t('institution.tags')"
            :items="institutionTags"
            v-model="selectedInstitutionTags"
            item-title="name"
            item-value="id"
            persistent-hint
            multiple
          ></Select>
          <Select
            v-if="props.type === 'users'"
            attribute="tags"
            :no-data-text="$t('label.no_data_text')"
            :label="handleDisplayName(userTagsAttribute.displayName ?? '')"
            :items="userTags"
            v-model="selectedUserTags"
            item-title="name"
            item-value="id"
            persistent-hint
            multiple
          ></Select>
        </v-form>
      </v-card-text>
      <UIAlert
        v-if="hasLoadingError || hasRequestError"
        v-bind:message="applicationStore.httpErrorMessage"
      />
      <v-divider></v-divider>
      <v-card-actions>
        <v-spacer></v-spacer>
        <v-btn color="accent" @click="() => editTags()">
          {{ $t("button.add") }}
        </v-btn>
        <v-btn color="accent" @click="() => editTags(true)">
          {{ $t("button.remove") }}
        </v-btn>
        <v-btn color="accent" @click="props.close">
          {{ $t("button.cancel") }}
        </v-btn>
      </v-card-actions>
    </v-card>
  </v-dialog>
</template>

<script setup>
import { onBeforeMount, onUpdated, ref } from "vue";
import { HTTP } from "@/lib/http";
import { useForm } from "@/lib/use-form";
import { useNotification } from "@/lib/use-notification";
import { updateInstitution } from "@/components/Institution/institution";
import { handleDisplayName, updateUser } from "@/components/User/user";
import { useApplicationStore } from "@/stores/application";
import { useInstitutionStore } from "@/stores/institution";
import { useProfileStore } from "@/stores/profile";
import { useUserStore } from "@/stores/user";
import Select from "@/components/Form/Select";

const props = defineProps(["close", "isActive", "type"]);

const applicationStore = useApplicationStore();
const institutionStore = useInstitutionStore();
const profileStore = useProfileStore();
const userStore = useUserStore();

const { hasLoadingError, hasRequestError, resetNotification } =
  useNotification();
const {
  form,
  valid,
  handleValidationErrorFromServer,
  isServerValidationError,
} = useForm();
const institutionTags = ref([]);
const selectedInstitutionTags = ref([]);
const userTagsAttribute = ref("");
const userTags = ref([]);
const selectedUserTags = ref([]);

const getInstitutionTags = () => {
  HTTP.get("institution/tag")
    .then((response) => {
      institutionTags.value = response.data;
    })
    .catch(() => {
      hasLoadingError.value = true;
    });
};

onBeforeMount(() => {
  applicationStore.setForm(form);
});

onUpdated(async () => {
  if (props.type === "institutions") {
    getInstitutionTags();
  } else if (props.type === "users") {
    if (!profileStore.attributes) {
      await profileStore.loadUserProfileMetadata();
    }
    userTagsAttribute.value = profileStore.attributes.find(
      (attribute) => attribute.name === "tags"
    );
    userTags.value = userTagsAttribute.value.validations.options.options;
  }
});

const editInstitutionTags = (remove) => {
  let successful = true;
  institutionStore.selectedInstitutions.forEach((id) => {
    const institutionToEdit = structuredClone(
      institutionStore.foundInstitutions.find((inst) => inst.id === id)
    );
    if (remove) {
      institutionToEdit.tags = institutionToEdit.tags.filter(
        (tag) => !selectedInstitutionTags.value.includes(tag)
      );
    } else {
      institutionToEdit.tags = selectedInstitutionTags.value;
    }
    const result = updateInstitution(
      institutionToEdit,
      true,
      isServerValidationError,
      handleValidationErrorFromServer,
      hasRequestError
    );
    if (!result) successful = false;
  });
  return successful;
};

const editUserTags = (remove) => {
  let successful = true;
  userStore.selectedUsers.forEach((id) => {
    const userToEdit = structuredClone(
      userStore.foundUsers.find((user) => user.id === id)
    );
    if (remove) {
      userToEdit.attributes.tags = userToEdit.attributes.tags.filter(
        (tag) => !selectedUserTags.value.includes(tag)
      );
    } else {
      userToEdit.attributes.tags = selectedUserTags.value;
    }
    const result = updateUser(
      userToEdit,
      resetNotification,
      isServerValidationError,
      handleValidationErrorFromServer,
      hasRequestError
    );
    if (!result) successful = false;
  });
  return successful;
};

const editTags = (remove) => {
  let successful;
  if (props.type === "institutions") {
    successful = editInstitutionTags(remove);
  } else if (props.type === "users") {
    successful = editUserTags(remove);
  }
  if (successful) props.close();
};
</script>
