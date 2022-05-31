<template>
  <div class="ml-4 mr-4 mt-10 pa-2 text-h6 bg-secondary">Institutions</div>
  <div class="ma-2 pa-2">
    <v-btn color="accent" @click="createVisible = true">
      <v-icon>mdi-plus </v-icon>
      Add
      <!-- Create institution dialog -->
      <v-dialog v-model="createVisible">
        <v-card>
          <v-card-title>
            <span class="text-h5">Create new Institution</span>
          </v-card-title>
          <v-card-text>
            <v-container>
              <v-row>
                <v-col>
                  <v-text-field
                    variant="underlined"
                    label="Name"
                    v-model="newInstitution.name"
                  ></v-text-field>
                </v-col>
              </v-row>
            </v-container>
          </v-card-text>
          <v-card-actions>
            <v-spacer></v-spacer>
            <v-btn
              color="accent"
              @click="
                createInstitution();
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
  </div>
  <v-table class="ma-2 pa-2">
    <thead>
      <th class="text-left">ID</th>
      <th class="text-left">Name</th>
      <th class="text-left">Actions</th>
    </thead>
    <tbody>
      <tr v-for="item in institutions" :key="item.id">
        <!-- Edit institution dialog -->
        <td>{{ item.id }}</td>
        <td>{{ item.name }}</td>
        <td>
          <v-tooltip>
            <template v-slot:activator="{ props }">
              <v-btn
                variant="plain"
                icon="mdi-account-edit-outline"
                size="small"
                v-bind="props"
                @click="
                  onEditClicked(item.id);
                  editVisible = true;
                "
              ></v-btn>
            </template>
            <span>Edit</span>
          </v-tooltip>
        </td>
      </tr>
    </tbody>
  </v-table>
  <v-dialog v-model="editVisible" scrollable>
    <v-card min-width="500">
      <v-card-title>
        <span class="text-h5">
          Edit Institution
          <span class="font-italic">{{ currentInstitution.name }}</span>
        </span>
      </v-card-title>
      <v-card-text>
        <v-container>
          <v-row>
            <v-col>
              <v-text-field
                variant="plain"
                label="ID"
                readonly
                v-model="currentInstitution.id"
              ></v-text-field>
              <v-text-field
                variant="underlined"
                label="Name"
                v-model="currentInstitution.name"
              ></v-text-field>
              <span class="text-h8">Attributes</span>

              <div v-if="instAttributes">
                <v-text-field
                  v-for="attr in Object.keys(instAttributes)"
                  v-model="instAttributes[attr]"
                  :label="attr"
                  :key="attr"
                  clearable
                ></v-text-field>
              </div>
            </v-col>
          </v-row>
        </v-container>
      </v-card-text>
      <v-card-actions>
        <v-spacer></v-spacer>
        <v-btn
          color="accent"
          @click="
            saveInstitution();
            editVisible = false;
          "
        >
          Save
        </v-btn>
        <v-btn color="accent" @click="editVisible = false"> Cancel </v-btn>
        <v-btn
          color="accent"
          @click="resetInstitutionForm(currentInstitution.id)"
        >
          Reset
        </v-btn>
      </v-card-actions>
    </v-card>
  </v-dialog>
</template>

<script>
import { computed, onMounted, ref } from "vue";
import { useStore } from "vuex";

export default {
  setup() {
    const store = useStore();
    const createVisible = ref(false);
    const editVisible = ref(false);
    //Load store
    onMounted(() => {
      store.dispatch("institution/loadInstitutions");
    });

    //Table headers
    const headers = ref([
      {
        text: "ID",
        align: "start",
        sortable: "false",
        value: "id",
      },
      {
        text: "Name",
        align: "start",
        sortable: true,
        value: "name",
      },
    ]);
    //Computed items
    const institutions = computed(() => {
      return store.state.institution.institutions;
    });

    const currentInstitution = computed(() => {
      return store.state.institution.institution;
    });

    const newInstitution = computed(() => {
      return store.state.institution.newInstitution;
    });

    const onEditClicked = (id) => {
      store.dispatch("institution/loadInstitutionById", id).then(() => {
        instAttributes.value = currentInstitution.value.attributes || [];
      }); // TODO: Handle HTTP Erorrs
    };

    //Functions
    const createInstitution = () => {
      const then = () => {
        //Reload list items
        store.dispatch("institution/loadInstitutions");
      };
      store.dispatch("institution/createInstitution", then);
    };
    const resetInstitutionForm = (id) => {
      store.dispatch("institution/loadInstitutionById", id).then(() => {
        instAttributes.value = currentInstitution.value.attributes || [];
      });
    };
    const saveInstitution = () => {
      //Store edited institution
      store.dispatch("institution/storeInstitution").then(() => {
        store.dispatch("institution/loadInstitutions");
      });
    };
    const instAttributes = ref();
    const FormAttribute = (key, value) => {
      return key + ": " + value.map((x) => x).join(", ");
    };
    return {
      FormAttribute,
      instAttributes,
      createVisible,
      editVisible,
      currentInstitution,
      headers,
      institutions,
      newInstitution,
      onEditClicked,
      createInstitution,
      resetInstitutionForm,
      saveInstitution,
    };
  },
};
</script>
