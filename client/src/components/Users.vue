<template>
  <div class="ml-4 mr-4 mt-10 pa-2 text-h6 bg-secondary">
    {{ $t("user.title") }}
  </div>
  <div class="ma-2 pa-2">
    <v-btn
      color="accent"
      @click="
        resetUser();
        resetNotification();
        showCreateDialog = true;
      "
    >
      <v-icon>mdi-plus</v-icon>
      {{ $t("button.add") }}
    </v-btn>
    <v-table class="ma-2 pa-2">
      <thead>
        <th class="text-left">{{ $t("label.id") }}</th>
        <th class="text-left">{{ $t("label.username") }}</th>
        <th class="text-left">{{ $t("label.firstname") }}</th>
        <th class="text-left">{{ $t("label.lastname") }}</th>
        <th class="text-left">{{ $t("label.email") }}</th>
        <th class="text-left">{{ $t("label.actions") }}</th>
      </thead>
      <tbody>
        <tr v-for="user in users" :key="user.id">
          <td>{{ user.id }}</td>
          <td>{{ user.username }}</td>
          <td>{{ user.firstName }}</td>
          <td>{{ user.lastName }}</td>
          <td>{{ user.email }}</td>
          <td class="d-flex">
            <v-tooltip>
              <template v-slot:activator="{ props }">
                <v-btn
                  variant="plain"
                  icon="mdi-account-edit-outline"
                  size="small"
                  v-bind="props"
                  @click="
                    resetNotification();
                    onEditClicked(user.id);
                    showEditDialog = true;
                  "
                ></v-btn>
              </template>
              <span>{{ $t("label.edit") }}</span>
            </v-tooltip>
            <v-tooltip>
              <template v-slot:activator="{ props }">
                <v-btn
                  variant="plain"
                  icon="mdi-content-copy"
                  size="small"
                  v-bind="props"
                  @click="
                    process = 'copy';
                    resetNotification();
                    onCopyClicked(user.id);
                    showCreateDialog = true;
                  "
                ></v-btn>
              </template>
              <span>{{ $t("label.copy") }}</span>
            </v-tooltip>
          </td>
        </tr>
      </tbody>
    </v-table>
    <UIAlert
      v-if="hasLoadingError"
      v-bind:isSuccessful="!hasLoadingError"
      v-bind:message="$store.state.application.httpErrorMessage"
    />
    <v-dialog v-model="showCreateDialog">
      <v-card min-width="500">
        <v-card-title>
          <span class="text-h5">{{ $t("user.create_title") }}</span>
        </v-card-title>
        <v-divider></v-divider>
        <v-container>
          <v-col jsutify="start" cols="10">
            <v-form v-model="valid" ref="addForm">
              <v-col>
                <v-text-field
                  variant="underlined"
                  :label="$t('label.username')"
                  v-model="user.username"
                ></v-text-field>
                <v-text-field
                  variant="underlined"
                  :label="$t('label.firstname')"
                  v-model="user.firstName"
                  :rules="[(v) => !!v || $t('form.required_firstname')]"
                ></v-text-field>
                <v-text-field
                  variant="underlined"
                  :label="$t('label.lastname')"
                  :rules="[(v) => !!v || $t('form.required_lastname')]"
                  v-model="user.lastName"
                ></v-text-field>
                <v-text-field
                  variant="underlined"
                  :label="$t('label.email')"
                  v-model="user.email"
                  :rules="[
                    (v) => !!v || $t('form.required_email'),
                    (v) => /.+@.+/.test(v) || $t('form.valid_email'),
                  ]"
                ></v-text-field>
                <v-select
                  return-object
                  dense
                  clearable
                  :label="$t('user.label_institutions')"
                  :items="institutions"
                  v-model="user.groups"
                  item-title="name"
                  item-value="id"
                  persistent-hint
                  multiple
                >
                </v-select>
              </v-col>
            </v-form>
            <UIAlert
              v-if="hasHttpError"
              v-bind:isSuccessful="!hasHttpError"
              v-bind:message="$store.state.application.httpErrorMessage"
            />
          </v-col>
        </v-container>
        <v-divider></v-divider>
        <v-card-actions>
          <v-spacer></v-spacer>
          <v-btn
            :color="`${valid ? 'accent' : 'grey'}`"
            :disabled="!valid"
            @click="createUser()"
          >
            {{ $t("button.create") }}
          </v-btn>
          <v-btn
            color="accent"
            @click="
              showCreateDialog = false;
              hasHttpError = false;
            "
          >
            {{ $t("button.cancel") }}
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>
    <v-dialog v-model="showEditDialog">
      <v-card min-width="500">
        <v-card-title>
          <span class="text-h5">{{
            $t("user.edit_title", { name: user.username })
          }}</span>
        </v-card-title>
        <v-divider></v-divider>
        <v-container>
          <v-col cols="10">
            <v-form v-model="editValid" ref="editForm">
              <v-col cols="10">
                <v-text-field
                  variant="plain"
                  :label="$t('label.id')"
                  readonly
                  v-model="user.id"
                ></v-text-field>
                <v-text-field
                  variant="plain"
                  :label="$t('label.username')"
                  readonly
                  v-model="user.username"
                ></v-text-field>
                <v-text-field
                  variant="underlined"
                  :label="$t('label.firstname')"
                  :rules="[(v) => !!v || $t('form.required_firstname')]"
                  v-model="user.firstName"
                ></v-text-field>
                <v-text-field
                  variant="underlined"
                  :label="$t('label.lastname')"
                  :rules="[(v) => !!v || $t('form.required_lastname')]"
                  v-model="user.lastName"
                ></v-text-field>
                <v-text-field
                  variant="underlined"
                  :label="$t('label.email')"
                  v-model="user.email"
                  :rules="[
                    (v) => !!v || $t('form.required_email'),
                    (v) => /.+@.+/.test(v) || $t('form.valid_email'),
                  ]"
                ></v-text-field>
                <v-select
                  return-object
                  dense
                  clearable
                  :label="$t('user.label_institutions')"
                  :items="institutions"
                  v-model="user.groups"
                  item-title="name"
                  item-value="id"
                  persistent-hint
                  multiple
                >
                </v-select>
              </v-col>
            </v-form>
            <UIAlert
              v-if="hasHttpError"
              v-bind:isSuccessful="!hasHttpError"
              v-bind:message="$store.state.application.httpErrorMessage"
            />
          </v-col>
        </v-container>
        <v-divider></v-divider>
        <v-card-actions>
          <v-spacer></v-spacer>
          <!-- TODO: Check if the style of a disabled button
          is fixed by upstream  -->
          <v-btn
            @click="storeUser()"
            :color="`${editValid ? 'accent' : 'grey'}`"
            :disabled="!editValid"
          >
            {{ $t("button.save") }}
          </v-btn>
          <v-btn
            color="accent"
            @click="
              showEditDialog = false;
              hasHttpError = false;
            "
          >
            {{ $t("button.cancel") }}
          </v-btn>
          <v-btn
            color="accent"
            @click="
              () => {
                user = { ...savedUser };
              }
            "
          >
            {{ $t("button.reset") }}
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>
  </div>
</template>
<script>
/* Copyright (C) 2022 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY!
 */

import { computed, onMounted, ref, defineAsyncComponent, watch } from "vue";
import { useStore } from "vuex";
import { HTTP } from "@/lib/http";
import { useNotification } from "@/lib/use-notification";

export default {
  components: {
    UIAlert: defineAsyncComponent(() => import("./UI/UIAlert.vue")),
  },
  setup() {
    const store = useStore();
    const {
      hasRequestError: hasHttpError,
      hasLoadingError,
      resetNotification,
    } = useNotification();

    const showCreateDialog = ref(false);
    const showEditDialog = ref(false);

    const newUser = computed(() => {
      return store.state.user.newUser;
    });
    const users = computed(() => {
      return store.state.user.users;
    });
    const institutions = computed(() => {
      return store.state.institution.institutions;
    });

    onMounted(() => {
      store
        .dispatch("user/loadUsers")
        .then()
        .catch(() => {
          hasLoadingError.value = true;
        });
      store
        .dispatch("institution/loadInstitutions")
        .then()
        .catch(() => {
          hasLoadingError.value = true;
        });
    });

    const onCopyClicked = (id) => {
      savedUser.value = users.value.filter((u) => id === u.id)[0];
      user.value = { ...savedUser.value };
      delete user.value["id"];
    };
    const onEditClicked = (id) => {
      user.value = {
        ...users.value.filter((u) => id === u.id)[0],
      };
      // Save original user data for "reset" button
      savedUser.value = { ...user.value };
    };

    const user = ref({
      id: "",
      username: "",
      firstName: "",
      lastName: "",
      email: "",
      groups: [],
    });

    const resetUser = () => {
      user.value.id = "";
      user.value.username = "";
      user.value.firstName = "";
      user.value.lastName = "";
      user.value.email = "";
      user.value.groups = [];
    };
    const getInstitutionIds = (institution) => {
      return institution.map((i) => i.id);
    };
    const createUser = () => {
      const payload = { ...user.value };
      payload["groups"] = user.value.groups.length
        ? getInstitutionIds(user.value.groups)
        : [];
      HTTP.post("/iamuser", payload)
        .then(() => {
          resetUser();
          showCreateDialog.value = false;
          store.dispatch("user/loadUsers");
        })
        .catch(() => {
          hasHttpError.value = true;
        });
    };

    const savedUser = ref();
    const storeUser = () => {
      const payload = { ...user.value };
      payload["groups"] = user.value.groups.length
        ? getInstitutionIds(user.value.groups)
        : [];
      HTTP.put("/iamuser", payload)
        .then(() => {
          showEditDialog.value = false;
          resetUser();
          store.dispatch("user/loadUsers");
          showEditDialog.value = false;
        })
        .catch(() => {
          hasHttpError.value = true;
        });
    };
    const process = ref("");
    // Form
    const valid = ref(false);
    const editValid = ref(false);
    const editForm = ref(null);
    const addForm = ref(null);
    // This is necessary as the form value is not change to true with valid inputs.
    // TODO: Check if this is fixed by upstream with the next release.
    watch(
      () => showEditDialog.value,
      () => {
        if (showEditDialog.value) {
          setTimeout(() => {
            editForm.value.validate();
          }, 100);
        }
      }
    );
    watch(
      () => showCreateDialog.value,
      () => {
        if (showCreateDialog.value && process.value == "copy") {
          setTimeout(() => {
            addForm.value.validate();
          }, 100);
        }
      }
    );
    return {
      resetNotification,
      hasLoadingError,
      addForm,
      process,
      valid,
      editForm,
      editValid,
      savedUser,
      user,
      resetUser,
      hasHttpError,
      showCreateDialog,
      showEditDialog,
      newUser,
      institutions,
      users,
      createUser,
      storeUser,
      onCopyClicked,
      onEditClicked,
    };
  },
};
</script>
