<!--
 Copyright (C) 2022 by Bundesamt fuer Strahlenschutz
 Software engineering by Intevation GmbH

 This file is Free Software under the GNU GPL (v>=3)
 and comes with ABSOLUTELY NO WARRANTY!
 -->
<template>
  <v-table class="ma-2 pa-2">
    <thead>
      <th class="text-left">{{ $t("label.name") }}</th>
      <th class="text-left">{{ $t("institution.shortname") }}</th>
      <th class="text-left">{{ $t("institution.imis_Id") }}</th>
      <th class="text-left">{{ $t("label.actions") }}</th>
    </thead>
    <tbody>
      <tr v-for="item in props.institutions" :key="item.id">
        <td>{{ item.name }}</td>
        <td>{{ item.shortName }}</td>
        <td>{{ item.imisId }}</td>
        <td class="d-flex">
          <v-tooltip location="top">
            <template v-slot:activator="{ props }">
              <v-btn
                variant="plain"
                :icon="`${
                  $store.state.profile.isAllowedToManage
                    ? 'mdi-pencil'
                    : 'mdi-information-outline'
                }`"
                size="small"
                v-bind="props"
                @click="
                  $store.commit('application/setManagedItem', {
                    ...item,
                  });
                  $store.commit('application/setProcessType', 'edit');
                  $store.commit(
                    'application/setShowManageInstitutionDialog',
                    true
                  );
                "
              ></v-btn>
            </template>
            <span>{{ $t("label.edit") }}</span>
          </v-tooltip>
          <v-tooltip location="top">
            <template v-slot:activator="{ props }">
              <v-btn
                v-if="$store.state.profile.isAllowedToManage"
                variant="plain"
                icon="mdi-delete"
                size="small"
                v-bind="props"
                @click="
                  $store.commit('application/setManagedItem', {
                    ...item,
                  });
                  $store.commit('application/setProcessType', 'delet');
                  $store.commit(
                    'application/setShowManageInstitutionDialog',
                    true
                  );
                "
              ></v-btn>
            </template>
            <span>{{ $t("label.delete") }}</span>
          </v-tooltip>
        </td>
      </tr>
      <tr v-if="institutions && !institutions.length">
        <td colspan="3">
          {{ $t("institution.no_institutions_available") }}
        </td>
      </tr>
    </tbody>
  </v-table>
</template>

<script setup>
const props = defineProps({
  institutions: Array,
});
</script>
