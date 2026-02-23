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
          ? $t('search.editTagsInstitutions')
          : $t('search.editTagsUsers')
      "
    >
      <v-card-text>
        <v-form v-model="valid" class="v-col v-col-10">
          <Select
            :no-data-text="$t('label.noDataText')"
            :label="$t('institution.tags')"
            :items="
              props.type === 'institutions'
                ? institutionStore.institutionTags
                : userTags
            "
            v-model="selectedTags"
            item-title="name"
            item-value="id"
            persistent-hint
            multiple
          ></Select>
        </v-form>
        <v-alert
          v-for="message in errorMessages"
          v-bind:key="message.key"
          type="error"
          variant="text"
        >
          {{ message.key }}: {{ message.value }}
        </v-alert>
      </v-card-text>
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
import { onUpdated, ref, watch } from "vue";
import { useForm } from "@/lib/use-form.js";
import { useNotification } from "@/lib/use-notification.js";
import { updateInstitution } from "@/components/Institution/institution.js";
import { updateUser } from "@/components/User/user.js";
import { useInstitutionStore } from "@/stores/institution.js";
import { useProfileStore } from "@/stores/profile.js";
import { useUserStore } from "@/stores/user.js";
import Select from "@/components/Form/Select.vue";
import { useI18n } from "vue-i18n";

const props = defineProps(["close", "isActive", "type"]);

const institutionStore = useInstitutionStore();
const profileStore = useProfileStore();
const userStore = useUserStore();

const { t } = useI18n();
const { hasLoadingError, hasRequestError, resetNotification } =
  useNotification();
const {
  translateError,
  valid,
  handleValidationErrorFromServer,
  isServerValidationError,
} = useForm({});
const selectedTags = ref([]);
const userTagsAttribute = ref(null);
const userTags = ref([]);
const errorMessages = ref([]);

const isInstitutionType = () => {
  return props.type === "institutions";
};

const isUserType = () => {
  return props.type === "users";
};

watch(
  () => props.isActive,
  () => {
    resetMessages();
    resetSelection();
  }
);

const loadUserTags = async () => {
  if (!profileStore.attributes) {
    await profileStore.loadUserProfileMetadata();
  }
  userTagsAttribute.value = profileStore.attributes.find(
    (attribute) => attribute.name === "tags"
  );
  userTags.value = userTagsAttribute.value.validations.options.options;
};

onUpdated(() => {
  resetMessages();
  if (isInstitutionType()) {
    institutionStore.loadInstitutionTags().catch(() => {
      hasLoadingError.value = true;
    });
  } else if (isUserType()) {
    loadUserTags();
  }
});

const resetMessages = () => {
  errorMessages.value = [];
};

const resetSelection = () => {
  selectedTags.value = [];
};

const editTags = async (remove) => {
  resetMessages();
  const newErrorMessages = [];
  let selectedItems,
    foundItems,
    areTagsRequired = true;
  if (isInstitutionType()) {
    selectedItems = institutionStore.selectedInstitutions;
    foundItems = institutionStore.foundInstitutions;
  } else if (isUserType()) {
    selectedItems = userStore.selectedUsers;
    foundItems = userStore.foundUsers;
    areTagsRequired = userTagsAttribute.value.required?.roles?.includes("user");
  }
  for (let i = 0; i < selectedItems.length; i++) {
    const id = selectedItems[i];
    const itemToEdit = structuredClone(
      foundItems.find((item) => item.id === id)
    );
    let newTags = [];
    const oldTags =
      (isInstitutionType() ? itemToEdit.tags : itemToEdit.attributes.tags) ??
      [];
    if (remove) {
      newTags = oldTags.filter((tag) => !selectedTags.value.includes(tag));
      if (newTags.length === 0 && areTagsRequired) {
        newTags.push(oldTags[0]);
        let key, translatedType;
        if (isInstitutionType()) {
          key = itemToEdit.name;
          translatedType = t("institutions");
        } else if (isUserType()) {
          key = itemToEdit.attributes.username[0];
          translatedType = t("user_term");
        }
        newErrorMessages.push({
          key: key,
          value: t("search.didntRemove", {
            tag: oldTags[0],
            type: translatedType,
          }),
        });
      }
    } else {
      for (let i = 0; i < oldTags.length; i++) {
        newTags.push(oldTags[i]);
      }
      for (let i = 0; i < selectedTags.value.length; i++) {
        const tag = selectedTags.value[i];
        if (!oldTags.includes(tag)) {
          newTags.push(tag);
        }
      }
    }
    if (isInstitutionType()) {
      itemToEdit.tags = newTags;
    } else if (isUserType()) {
      itemToEdit.attributes.tags = newTags;
    }

    let result;
    if (isInstitutionType()) {
      result = await updateInstitution(
        itemToEdit,
        true,
        isServerValidationError,
        handleValidationErrorFromServer,
        hasRequestError
      );
    } else if (isUserType()) {
      result = updateUser(
        itemToEdit,
        resetNotification,
        isServerValidationError,
        handleValidationErrorFromServer,
        hasRequestError
      );
    }
    if (result.response !== 200) {
      if (isServerValidationError(result)) {
        const errors = result.response.data;
        const errorObject = errors[0];
        const attribute = errorObject.messageParameters[0];
        const translatedMessage = translateError(
          errorObject.message,
          attribute
        );
        newErrorMessages.push({
          key: itemToEdit.name,
          value: translatedMessage,
        });
      }
    }
  }

  errorMessages.value = newErrorMessages;
  if (newErrorMessages.length === 0) props.close();
};
</script>
