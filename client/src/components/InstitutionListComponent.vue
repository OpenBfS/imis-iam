<template>
  <div class="ml-4 mr-4 mt-10 pa-2 text-h6 bg-secondary">Institutions</div>
  <div class="ma-2 pa-2">
    <v-btn color="accent" @click="createVisible = true">
      <v-icon>mdi-plus </v-icon>
      Add
      <!-- Create institution dialog -->
      <v-dialog activator="parent">
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
            <v-btn color="accent" @click="createInstitution()"> Create </v-btn>
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
    </thead>
    <tbody>
      <tr v-for="item in items" :key="item.id">
        <!-- Edit institution dialog -->
        <v-dialog
          activator="parent"
          v-model="dialog"
          scrollable
          @update:modelValue="
            (newValue) => dialogVisibilityChanged(item.id, newValue)
          "
        >
          <v-card min-width="500">
            <v-card-title>
              <span class="text-h5">
                Edit Institution
                <span class="font-italic">{{ item.name }}</span>
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
                    <v-text-field
                      class="mt-2 ml-2 mb-0"
                      v-for="key in Object.keys(currentInstitution.attributes)"
                      :key="key"
                      v-model="currentInstitution.attributes[key]"
                      :label="key"
                    ></v-text-field>
                  </v-col>
                </v-row>
              </v-container>
            </v-card-text>
            <v-card-actions>
              <v-spacer></v-spacer>
              <v-btn color="accent" @click="saveInstitution()"> Save </v-btn>
            </v-card-actions>
          </v-card>
        </v-dialog>
        <td>{{ item.id }}</td>
        <td>{{ item.name }}</td>
      </tr>
    </tbody>
  </v-table>
</template>

<script>
import { computed, onMounted, ref } from "vue";
import institutionStore from "../store";
export default {
  setup() {
    var createVisible = false;
    //Load store
    onMounted(() => {
      institutionStore.dispatch("institution/loadInstitutions");
    });

    //Table headers
    const headers = computed(() => [
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
    const items = computed(() => {
      return institutionStore.state.institution.institutions;
    });

    const currentInstitution = computed(() => {
      return institutionStore.state.institution.institution;
    });

    const newInstitution = computed(() => {
      return institutionStore.state.institution.newInstitution;
    });

    const dialog = ref(false);

    //Functions
    const createInstitution = () => {
      const then = () => {
        //Reload list items
        institutionStore.dispatch("institution/loadInstitutions");
      };
      institutionStore.dispatch("institution/createInstitution", then);
    };
    const dialogVisibilityChanged = (id, newValue) => {
      if (newValue) {
        institutionStore.dispatch("institution/loadInstitutionById", id);
      }
    };
    const saveInstitution = () => {
      const then = () => {
        //Reload list items
        institutionStore.dispatch("institution/loadInstitutions");
      };
      //Store edited institution
      institutionStore.dispatch("institution/storeInstitution", then);
    };

    return {
      createVisible,
      dialog,
      currentInstitution,
      headers,
      items,
      newInstitution,
      createInstitution,
      dialogVisibilityChanged,
      saveInstitution,
    };
  },
};
</script>
