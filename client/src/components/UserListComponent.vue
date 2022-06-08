<template>
  <div class="ml-4 mr-4 mt-10 pa-2 text-h6 bg-secondary">Users</div>
  <div class="ma-2 pa-2">
    <v-btn
      color="accent"
      @click="
        resetUser();
        showCreateDialog = true;
      "
    >
      <v-icon>mdi-plus</v-icon>
      Add
      <!-- Create user dialog -->
    </v-btn>
    <v-table class="ma-2 pa-2">
      <thead>
        <th class="text-left">ID</th>
        <th class="text-left">Username</th>
        <th class="text-left">First Name</th>
        <th class="text-left">Last Name</th>
        <th class="text-left">Email</th>
        <th class="text-left">Actions</th>
      </thead>
      <tbody>
        <tr v-for="user in users" :key="user.id">
          <td>{{ user.id }}</td>
          <td>{{ user.username }}</td>
          <td>{{ user.firstName }}</td>
          <td>{{ user.lastName }}</td>
          <td>{{ user.email }}</td>
          <td>
            <v-tooltip>
              <template v-slot:activator="{ props }">
                <v-btn
                  variant="plain"
                  icon="mdi-account-edit-outline"
                  size="small"
                  v-bind="props"
                  @click="
                    onEditClicked(user.id);
                    showEditDialog = true;
                  "
                ></v-btn>
              </template>
              <span>Edit</span>
            </v-tooltip>
            <v-tooltip>
              <template v-slot:activator="{ props }">
                <v-btn
                  variant="plain"
                  icon="mdi-content-copy"
                  size="small"
                  v-bind="props"
                  @click="
                    onCopyClicked(user.id);
                    showCreateDialog = true;
                  "
                ></v-btn>
              </template>
              <span>Copy</span>
            </v-tooltip>
          </td>
        </tr>
      </tbody>
    </v-table>
    <!-- Edit user dialog -->
    <v-dialog v-model="showCreateDialog">
      <v-card min-width="500">
        <v-card-title>
          <span class="text-h5">Create new User</span>
        </v-card-title>
        <v-divider></v-divider>
        <v-container>
          <v-row jsutify="start" class="ml-2">
            <v-col cols="10">
              <v-text-field
                variant="underlined"
                label="Username"
                v-model="user.username"
              ></v-text-field>
              <v-text-field
                variant="underlined"
                label="First Name"
                v-model="user.firstName"
              ></v-text-field>
              <v-text-field
                variant="underlined"
                label="Last Name"
                v-model="user.lastName"
              ></v-text-field>
              <v-text-field
                variant="underlined"
                label="Email"
                v-model="user.email"
              ></v-text-field>
              <v-select
                return-object
                dense
                clearable
                label="institutions"
                :items="institutions"
                v-model="user.groups"
                item-title="name"
                item-value="id"
                persistent-hint
                multiple
              >
              </v-select>
            </v-col>
          </v-row>
        </v-container>
        <v-divider></v-divider>
        <v-card-actions>
          <UIAlert
            v-if="hasHttpError"
            v-bind:isSuccessful="!hasHttpError"
            v-bind:message="httpErrorMsg"
          />
          <v-spacer></v-spacer>
          <v-btn color="accent" @click="createUser()"> Create </v-btn>
          <v-btn
            color="accent"
            @click="
              showCreateDialog = false;
              hasHttpError = false;
            "
          >
            Cancel
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>
    <v-dialog v-model="showEditDialog">
      <v-card min-width="500">
        <v-card-title>
          <span class="text-h5">Edit User {{ user.username }}</span>
        </v-card-title>
        <v-divider></v-divider>
        <v-container>
          <v-row jsutify="start" class="ml-2 pt-2">
            <v-col cols="10">
              <v-text-field
                variant="plain"
                label="ID"
                readonly
                v-model="user.id"
              ></v-text-field>
              <v-text-field
                variant="plain"
                label="Username"
                readonly
                v-model="user.username"
              ></v-text-field>
              <v-text-field
                variant="underlined"
                label="First Name"
                v-model="user.firstName"
              ></v-text-field>
              <v-text-field
                variant="underlined"
                label="Last Name"
                v-model="user.lastName"
              ></v-text-field>
              <v-text-field
                variant="underlined"
                label="Email"
                v-model="user.email"
              ></v-text-field>
              <v-select
                return-object
                dense
                clearable
                label="institutions"
                :items="institutions"
                v-model="user.groups"
                item-title="name"
                item-value="id"
                persistent-hint
                multiple
              >
              </v-select>
            </v-col>
          </v-row>
        </v-container>
        <v-divider></v-divider>
        <v-card-actions>
          <UIAlert
            v-if="hasHttpError"
            v-bind:isSuccessful="!hasHttpError"
            v-bind:message="httpErrorMsg"
          />
          <v-spacer></v-spacer>
          <v-btn color="accent" @click="storeUser()"> Save </v-btn>
          <v-btn
            color="accent"
            @click="
              showEditDialog = false;
              hasHttpError = false;
            "
          >
            Cancel
          </v-btn>
          <v-btn
            color="accent"
            @click="
              () => {
                user = { ...savedUser };
              }
            "
          >
            Reset
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>
  </div>
</template>
<script>
import { computed, onMounted, ref, defineAsyncComponent } from "vue";
import { useStore } from "vuex";
import { HTTP } from "../lib/http";

export default {
  components: {
    UIAlert: defineAsyncComponent(() => import("../components/UI/UIAlert.vue")),
  },
  setup() {
    const store = useStore();
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
      store.dispatch("user/loadUsers");
      store.dispatch("institution/loadInstitutions");
    });

    const onCopyClicked = (id) => {
      savedUser.value = users.value.filter((u) => id === u.id)[0];
      user.value = { ...savedUser.value };
      delete user.value["id"];
    };
    const onEditClicked = (id) => {
      savedUser.value = {
        ...(user.value = users.value.filter((u) => id === u.id)[0]),
      };
    };

    const user = ref({
      id: "",
      username: "",
      firstName: "",
      lastName: "",
      email: "",
      groups: [],
    });

    const hasHttpError = ref(false);
    const httpErrorMsg = ref("");

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
      hasHttpError.value = false;
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
        .catch((error) => {
          hasHttpError.value = true;
          httpErrorMsg.value = error.response.statusText;
        });
    };

    const savedUser = ref();
    const resetUserForm = (id) => {
      store.dispatch("user/loadUserById", id);
    };

    const storeUser = () => {
      hasHttpError.value = false;
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
        .catch((error) => {
          hasHttpError.value = true;
          httpErrorMsg.value = error.response.statusText;
        });
    };

    return {
      savedUser,
      user,
      resetUser,
      hasHttpError,
      httpErrorMsg,
      showCreateDialog,
      showEditDialog,
      newUser,
      institutions,
      users,
      createUser,
      resetUserForm,
      storeUser,
      onCopyClicked,
      onEditClicked,
    };
  },
};
</script>
