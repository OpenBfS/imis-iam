<template>
  <div class="ma-2 pa-2">Institutions</div>
  <v-table class="ma-2 pa-2">
    <thead>
      <th class="text-left">ID</th>
      <th class="text-left">Name</th>
    </thead>
    <tbody>
      <tr v-for="item in items" :key="item.id">
        <v-dialog
          activator="parent"
          scrollable
          @update:modelValue="
            (newValue) => dialogVisibilityChanged(item.id, newValue)
          "
        >
          <v-card>
            <v-card-title>
              <span class="text-h5">TEST {{ item.id }}</span>
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
import { computed, onMounted } from "vue";
import institutionStore from "../store";
export default {
  setup() {
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

    //Functions
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

    //Functions
    return {
      currentInstitution,
      dialogVisibilityChanged,
      headers,
      items,
      saveInstitution,
    };
  },
};
</script>
