<template>
  <v-dialog v-model="show">
    <v-card min-width="600" v-if="['add', 'edit'].indexOf(processType) !== -1">
      <v-card-title v-if="processType === 'add'">
        <span class="text-h5">{{ $t("institution.create_title") }}</span>
      </v-card-title>
      <v-card-title v-if="processType === 'edit'">
        <span class="text-h5">
          {{ $t("institution.edit_title", { name: currentInstitution.name }) }}
        </span>
      </v-card-title>
      <v-divider></v-divider>
      <v-container>
        <v-col jsutify="start" cols="10">
          <v-form v-model="valid" ref="form">
            <v-col>
              <v-text-field
                variant="underlined"
                density="compact"
                :label="$t('label.name')"
                v-model="institution.name"
                :rules="[(v) => !!v || $t('form.required_name')]"
              ></v-text-field>
              <v-text-field
                variant="underlined"
                density="compact"
                :label="$t('institution.shortname')"
                :rules="[(v) => !!v || $t('institution.required_shortname')]"
                v-model="institution.shortName"
              ></v-text-field>
              <v-text-field
                variant="underlined"
                density="compact"
                :label="$t('institution.service_building_street')"
                :rules="[
                  (v) =>
                    !!v || $t('institution.required_service_building_street'),
                ]"
                v-model="institution.SBStreet"
              ></v-text-field>
              <v-text-field
                variant="underlined"
                density="compact"
                :label="$t('institution.service_building_location')"
                :rules="[
                  (v) =>
                    !!v || $t('institution.required_service_building_location'),
                ]"
                v-model="institution.SBLocation"
              ></v-text-field>
              <v-text-field
                variant="underlined"
                density="compact"
                :label="$t('institution.service_building_postalcode')"
                :rules="[
                  (v) =>
                    !!v ||
                    $t('institution.required_service_building_postalcode'),
                ]"
                type="number"
                v-model="institution.SBPostalCode"
              ></v-text-field>

              <v-text-field
                variant="underlined"
                density="compact"
                :label="$t('institution.central_phone')"
                :rules="[(v) => !!v || $t('form.required_central_phone')]"
                v-model="institution.CPhone"
                type="number"
              ></v-text-field>
              <!-- <v-text-field
                variant="underlined"
                density="compact"
                :label="$t('institution.central_fax')"
                v-model="institution.CFax"
                type="number"
              ></v-text-field> -->
              <v-text-field
                variant="underlined"
                density="compact"
                :label="$t('institution.central_email')"
                :rules="[
                  (v) => !!v || $t('institution.required_central_mail'),
                  (v) => /.+@.+/.test(v) || $t('form.valid_email'),
                ]"
                v-model="institution.CMail"
              ></v-text-field>
              <v-select
                return-object
                dense
                clearable
                :label="$t('institution.categories')"
                :items="categories"
                v-model="institution.categories"
                item-title="name"
                item-value="id"
                persistent-hint
                multiple
                :rules="[
                  (v) =>
                    !!(v && v.length) || $t('institution.required_category'),
                ]"
              >
              </v-select>
            </v-col>
          </v-form>
        </v-col>
        <UIAlert
          v-if="hasLoadingError || hasRequestError"
          v-bind:isSuccessful="false"
          v-bind:message="$store.state.application.httpErrorMessage"
        />
      </v-container>
      <v-divider></v-divider>
      <v-card-actions>
        <v-spacer></v-spacer>
        <v-btn
          :color="`${valid ? 'accent' : 'grey'}`"
          :disabled="!valid"
          @click="createInstitution()"
        >
          {{ $t("button.create") }}
        </v-btn>
        <v-btn
          color="accent"
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
    <v-card v-else>
      <v-card-text>
        <span class="text-h5">{{
          $t("label.confirm_deletion", { name: institution.name })
        }}</span>
      </v-card-text>
      <v-divider></v-divider>
      <v-card-actions>
        <v-spacer></v-spacer>
        <v-btn color="accent" @click="deleteInstitution()">
          {{ $t("button.delete") }}
        </v-btn>
        <v-btn
          color="accent"
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

<script>
import { onMounted, ref, defineAsyncComponent } from "vue";
import { HTTP } from "@/lib/http";
import { useNotification } from "@/lib/use-notification";
export default {
  components: {
    UIAlert: defineAsyncComponent(() => import("@/components/UI/UIAlert.vue")),
  },
  props: {
    item: Object,
    copiedItem: Object,
    processType: String,
  },
  setup(props, { emit }) {
    const show = true;
    const institution = ref(props.item);
    const { hasLoadingError, hasRequestError } = useNotification();
    const categories = ref([]);
    const getCategories = () => {
      HTTP.get("institution/category")
        .then((response) => {
          categories.value = response.data;
        })
        .catch(() => {
          hasLoadingError.value = true;
        });
    };
    onMounted(() => {
      getCategories();
    });
    const valid = ref(null);
    const createInstitution = () => {
      HTTP.post("/institution", {
        name: institution.value.name,
        shortName: institution.value.shortName,
        serviceBuildingStreet: institution.value.SBStreet,
        serviceBuildingPostalCode: institution.value.SBPostalCode,
        serviceBuildingLocation: institution.value.SBLocation,
        centralPhone: institution.value.CPhone,
        centralMail: institution.value.CMail,
      })
        .then(() => {
          emit("child-object", { closeDialog: true, hasChanges: true });
        })
        .catch(() => {
          hasRequestError.value = true;
        });
    };
    const deleteInstitution = () => {
      HTTP.delete("institution/" + institution.value.id)
        .then(() => {
          emit("child-object", { closeDialog: true, hasChanges: true });
        })
        .catch(() => {
          hasRequestError.value = true;
        });
    };
    return {
      deleteInstitution,
      hasLoadingError,
      hasRequestError,
      createInstitution,
      valid,
      show,
      institution,
      categories,
    };
  },
};
</script>
