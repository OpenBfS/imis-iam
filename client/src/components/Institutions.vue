<!--
/* Copyright (C) 2022 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY!
 */
 -->
<template>
  <v-container>
    <v-row>
      <v-col cols="12" class="mt-10 pa-2 text-h6 bg-secondary">
        {{ $t("institution.title") }}
      </v-col>
    </v-row>
    <v-row justify="end" class="mt-6">
      <v-tooltip location="top">
        <template v-slot:activator="{ props }">
          <v-btn
            color="accent"
            class="mr-4"
            v-bind="props"
            @click="createVisible = true"
            icon="mdi-plus"
          >
          </v-btn>
        </template>
        <span>{{ $t("institution.add_institution") }}</span>
      </v-tooltip>
    </v-row>
    <v-row>
      <v-col cols="12" class="t-6">
        <v-table class="ma-2 pa-2">
          <thead>
            <th class="text-left">{{ $t("label.id") }}</th>
            <th class="text-left">{{ $t("label.name") }}</th>
            <th class="text-left">{{ $t("label.actions") }}</th>
          </thead>
          <tbody>
            <tr v-for="item in institutions" :key="item.id">
              <!-- Edit institution dialog -->
              <td>{{ item.id }}</td>
              <td>{{ item.name }}</td>
              <td>
                <v-tooltip location="top">
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
                  <span>{{ $t("label.edit") }}</span>
                </v-tooltip>
              </td>
            </tr>
          </tbody>
        </v-table>
      </v-col>
    </v-row>
  </v-container>
  <v-dialog v-model="createVisible">
    <v-card>
      <v-card-title>
        <span class="text-h5">{{ $t("institution.create_title") }}</span>
      </v-card-title>
      <v-card-text>
        <v-container>
          <v-row>
            <v-col>
              <v-text-field
                variant="underlined"
                :label="$t('label.name')"
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
          {{ $t("button.create") }}
        </v-btn>
        <v-btn color="accent" @click="createVisible = false">
          {{ $t("button.cancel") }}
        </v-btn>
      </v-card-actions>
    </v-card>
  </v-dialog>
  <v-dialog v-model="editVisible" scrollable>
    <v-card min-width="500">
      <v-card-title>
        <span class="text-h5">
          {{ $t("institution.edit_title", { name: currentInstitution.name }) }}
        </span>
      </v-card-title>
      <v-card-text>
        <v-container>
          <v-row>
            <v-col>
              <v-text-field
                variant="plain"
                :label="$t('label.id')"
                readonly
                v-model="currentInstitution.id"
              ></v-text-field>
              <v-text-field
                variant="underlined"
                :label="$t('label.name')"
                v-model="currentInstitution.name"
              ></v-text-field>
              <span class="text-h8">{{ $t("label.attributes") }}</span>

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
          {{ $t("button.save") }}
        </v-btn>
        <v-btn color="accent" @click="editVisible = false">
          {{ $t("button.cancel") }}
        </v-btn>
        <v-btn
          color="accent"
          @click="resetInstitutionForm(currentInstitution.id)"
        >
          {{ $t("button.reset") }}
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
