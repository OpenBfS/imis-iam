<!--
 Copyright (C) 2022 by Bundesamt fuer Strahlenschutz
 Software engineering by Intevation GmbH

 This file is Free Software under the GNU GPL (v>=3)
 and comes with ABSOLUTELY NO WARRANTY!
 -->
<template>
  <div id="imis-footer" class="text-caption">
    <a disabled size="small" variant="text"
      >&copy;{{ $t("label.copyright") }}
    </a>
    <a size="small" :href="links.contact" variant="text">{{
      $t("label.contact")
    }}</a>
    <a size="small" :href="links.privacy" variant="text">
      {{ $t("label.data_privacy") }}</a
    >
    <a size="small" :href="links.accessibility" variant="text">
      {{ $t("label.accessibility") }}</a
    >
    <a size="small" :href="links.help" variant="text">
      {{ $t("label.help") }}
    </a>
    <a size="small" :href="links.socialMedia" variant="text">
      {{ $t("label.social_media") }}
    </a>
  </div>
</template>
<style scoped>
#imis-footer {
  display: flex;
  flex-flow: row wrap;
  justify-content: flex-start;
  position: fixed;
  box-shadow: 0 2px rgba(3, 3, 3, 0.2);
  bottom: 5px;
  left: 5px;
  right: 5px;
  background-color: white;
  padding: 0 20px;
  padding-left: 5px;
  border-top: 4px solid;
  border-color: #5b8445;
}
#imis-footer > a {
  color: black;
  padding-left: 15px;
  font-size: 12px;
  line-height: 1.666667;
  cursor: pointer;
}
</style>

<script>
import { computed, onMounted } from "vue";
import { useApplicationStore } from "@/stores/application.js";
export default {
  setup() {
    const applicationStore = useApplicationStore();
    const links = computed(() => {
      return applicationStore.footerLinks;
    });
    onMounted(() => {
      links.value;
    });
    return {
      links,
    };
  },
};
</script>
