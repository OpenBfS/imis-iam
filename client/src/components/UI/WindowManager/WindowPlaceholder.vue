<!--
 Copyright (C) 2026 by Bundesamt fuer Strahlenschutz
 Software engineering by Intevation GmbH

 This file is Free Software under the GNU GPL (v>=3)
 and comes with ABSOLUTELY NO WARRANTY!
 -->

<template>
  <div :style="style" class="window-placeholder bg-accent"></div>
</template>

<style>
.window-placeholder {
  position: absolute;
  opacity: 0.5;
}
</style>

<script setup>
import { useApplicationStore } from "@/stores/application";
import { useWindowSize } from "@vueuse/core";
import { computed } from "vue";
import { getDimensionsByEdge } from "./windowManager";

const applicationStore = useApplicationStore();

const { height: windowHeight, width: windowWidth } = useWindowSize();

const style = computed(() => {
  // Get the item with the highest zIndex and place the placeholder beneath its dialog.
  const zIndex =
    structuredClone(applicationStore.managedItems)
      .filter((item) => item !== undefined)
      .sort((a, b) => {
        if (a.zIndex < b.zIndex) return -1;
        else if (a.zIndex > b.zIndex) return 1;
        return 0;
      })
      .pop().zIndex -
    1 +
    1005;

  const { height, width, x, y } = getDimensionsByEdge(
    applicationStore.touchedEdge,
    windowHeight.value,
    windowWidth.value
  );

  return {
    height: `${height}px`,
    width: `${width}px`,
    left: `${x}px`,
    top: `${y}px`,
    "z-index": zIndex,
  };
});
</script>
