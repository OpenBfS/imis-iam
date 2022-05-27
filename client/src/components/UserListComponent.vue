<template>
  <div class="ml-4 mr-4 mt-10 pa-2 text-h6 bg-secondary">Users</div>
  <div class="ma-2 pa-2">
    <v-btn color="accent">
      <v-icon>mdi-plus </v-icon>
      Add
      <!-- Create user dialog -->
      <v-dialog activator="parent" v-model="createVisible">
        <v-card min-width="500">
          <v-card-title>
            <span class="text-h5">Create new User</span>
          </v-card-title>
          <v-card-text>
            <v-row>
              <v-col>
                <v-text-field
                  variant="underlined"
                  label="Username"
                  v-model="newUser.username"
                ></v-text-field>
              </v-col>
            </v-row>
            <v-row>
              <v-col>
                <v-text-field
                  variant="underlined"
                  label="First Name"
                  v-model="newUser.firstName"
                ></v-text-field>
              </v-col>
              <v-col>
                <v-text-field
                  variant="underlined"
                  label="Last Name"
                  v-model="newUser.lastName"
                ></v-text-field>
              </v-col>
            </v-row>
            <v-row>
              <v-col>
                <v-text-field
                  variant="underlined"
                  label="Email"
                  v-model="newUser.email"
                ></v-text-field>
              </v-col>
            </v-row>
            <v-row>
              <v-col>
                <v-combobox
                  v-model="newUser.groups"
                  :items="institutions"
                  label="Institutions"
                  chips
                  closable-chips
                  multiple
                ></v-combobox>
              </v-col>
            </v-row>
          </v-card-text>
          <v-card-actions>
            <v-spacer></v-spacer>
            <v-btn
              color="accent"
              @click="
                createUser();
                createVisible = false;
              "
            >
              Create
            </v-btn>
            <v-btn color="accent" @click="createVisible = false">
              Cancel
            </v-btn>
          </v-card-actions>
        </v-card>
      </v-dialog>
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
                    editVisible = true;
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
                    createVisible = true;
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
    <v-dialog v-model="editVisible">
      <v-card min-width="700" min-height="800">
        <v-card-title>
          <span class="text-h5">Edit User {{ currentUser.username }}</span>
        </v-card-title>
        <v-card-text>
          <v-container>
            <v-row>
              <v-col>
                <v-text-field
                  variant="plain"
                  label="ID"
                  readonly
                  v-model="currentUser.id"
                ></v-text-field>
                <v-text-field
                  variant="plain"
                  label="Username"
                  readonly
                  v-model="currentUser.username"
                ></v-text-field>
              </v-col>
            </v-row>
            <v-row>
              <v-col>
                <v-text-field
                  variant="underlined"
                  label="First Name"
                  v-model="currentUser.firstName"
                ></v-text-field>
              </v-col>
              <v-col>
                <v-text-field
                  variant="underlined"
                  label="Last Name"
                  v-model="currentUser.lastName"
                ></v-text-field>
              </v-col>
            </v-row>
            <v-row>
              <v-col>
                <v-text-field
                  variant="underlined"
                  label="Email"
                  v-model="currentUser.email"
                ></v-text-field>
              </v-col>
            </v-row>
            <v-row>
              <v-col> Institutions </v-col>
            </v-row>
            <v-combobox
              v-model="currentUser.groups"
              :items="institutions"
              label="Institutions"
              chips
              closable-chips
              multiple
            ></v-combobox>
          </v-container>
        </v-card-text>
        <v-card-actions>
          <v-spacer></v-spacer>
          <v-btn color="accent" @click="storeUser()"> Save </v-btn>
          <v-btn color="accent" @click="editVisible = false"> Cancel </v-btn>
          <v-btn color="accent" @click="resetUserForm(currentUser.id)">
            Reset
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>
  </div>
</template>
<script>
import { computed, onMounted, ref } from "vue";
import { useStore } from "vuex";
export default {
  setup() {
    const store = useStore();
    const createVisible = ref(false);
    const editVisible = ref(false);
    const currentUser = computed(() => {
      return store.state.user.currentUser;
    });
    const newUser = computed(() => {
      return store.state.user.newUser;
    });
    const users = computed(() => {
      return store.state.user.users;
    });
    const institutions = computed(() => {
      return store.state.institution.institutionNames;
    });
    onMounted(() => {
      store.dispatch("user/loadUsers");
      store.dispatch("institution/loadInstitutionNames");
    });

    const onCopyClicked = (id) => {
      store.dispatch("user/loadUserById", id).then(() => {
        store.dispatch("user/copyUser");
      });
    };
    const onEditClicked = (id) => {
      store.dispatch("user/loadUserById", id);
    };
    const createUser = () => {
      store.dispatch("user/createUser").then(() => {
        store.dispatch("user/loadUsers");
      });
    };
    const resetUserForm = (id) => {
      store.dispatch("user/loadUserById", id);
    };
    const storeUser = () => {
      store.dispatch("user/storeUser").then(() => {
        store.dispatch("user/loadUsers");
      });
    };

    return {
      createVisible,
      editVisible,
      currentUser,
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
