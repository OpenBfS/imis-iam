<!--
 Copyright (C) 2026 by Bundesamt fuer Strahlenschutz
 Software engineering by Intevation GmbH

 This file is Free Software under the GNU GPL (v>=3)
 and comes with ABSOLUTELY NO WARRANTY!
 -->
<template>
  <div @click="emit('raise')" ref="draggable-dialog">
    <v-card :elevation="8" :style="dialogStyle">
      <div
        @dblclick="maximize"
        ref="draggable-dialog-header"
        class="draggable-dialog-header"
        draggable="true"
        :class="{ dragging: isDragging }"
      >
        <v-card-title class="bg-accent-lighten-5">
          <slot name="header"></slot>
        </v-card-title>
      </div>
      <div class="overflow-auto">
        <slot></slot>
      </div>
      <v-divider></v-divider>
      <v-card-actions>
        <v-spacer></v-spacer>
        <slot name="actions"></slot>
      </v-card-actions>
      <v-spacer></v-spacer>
      <div class="draggable-dialog-footer">
        <div
          ref="resize-icon-container"
          class="resize-icon-container"
          draggable="true"
        >
          <v-icon icon="mdi-resize-bottom-right" size="x-large"></v-icon>
        </div>
      </div>
    </v-card>
  </div>
</template>

<style global>
.draggable-dialog-header {
  cursor: grab;
}
.draggable-dialog-header.dragging {
  cursor: grabbing;
}
.draggable-dialog-footer {
  display: flex;
  justify-content: end;
}
.resize-icon-container {
  cursor: nwse-resize;
}
</style>

<script setup>
import { computed, inject, ref, useTemplateRef } from "vue";
import { useDraggable, useWindowSize } from "@vueuse/core";
import { useApplicationStore } from "@/stores/application";
import { getDimensionsByEdge } from "./windowManager";

const applicationStore = useApplicationStore();

const { zIndex } = defineProps(["zIndex"]);
const emit = defineEmits(["raise"]);

const { setDialogWidth } = inject("useForm");

const draggableDialogHeader = useTemplateRef("draggable-dialog-header");
const resizeIconContainer = useTemplateRef("resize-icon-container");

const dialogHeight = ref(Math.round(window.innerHeight * 0.8));
const dialogWidth = ref(600);
const maximized = ref(false);
const previousDimensions = ref(undefined);

const PADDING = 20;

const dialogStyle = computed(() => {
  const left = `${Math.min(Math.max(x.value, -dialogWidth.value + PADDING), windowWidth.value - PADDING)}px`;
  const top = `${Math.min(Math.max(y.value, -dialogHeight.value + PADDING), windowHeight.value - PADDING)}px`;
  return {
    position: "fixed",
    display: "flex",
    flexDirection: "column",
    height: `${dialogHeight.value}px`,
    minHeight: "500px",
    width: `${dialogWidth.value}px`,
    minWidth: "380px",
    left,
    top,
    transition: isDragging
      ? "none"
      : "width 200ms ease-in-out, height 200ms ease-in-out",
    "z-index": zIndex + 1005,
  };
});

const { height: windowHeight, width: windowWidth } = useWindowSize();
const { x, y, isDragging } = useDraggable(draggableDialogHeader, {
  initialValue: {
    x: windowWidth.value / 2 - dialogWidth.value / 2,
    y: Math.round(window.innerHeight / 2) - dialogHeight.value / 2,
  },
  preventDefault: true,
  onMove: (_position, event) => {
    let touchesTop = event.y < PADDING;
    let touchesBottom = event.y > windowHeight.value - PADDING;
    let touchesLeft = event.x < PADDING;
    let touchesRight = event.x > windowWidth.value - PADDING;
    let edge = undefined;
    if (touchesTop && touchesLeft) {
      edge = "top-left";
    } else if (touchesTop && touchesRight) {
      edge = "top-right";
    } else if (touchesBottom && touchesLeft) {
      edge = "bottom-left";
    } else if (touchesBottom && touchesRight) {
      edge = "bottom-right";
    } else if (touchesTop) {
      edge = "top";
    } else if (touchesBottom) {
      edge = "bottom";
    } else if (touchesLeft) {
      edge = "left";
    } else if (touchesRight) {
      edge = "right";
    }
    applicationStore.touchedEdge = edge;
  },
  onEnd: () => {
    if (!applicationStore.touchedEdge) return;
    const {
      height,
      width,
      x: newX,
      y: newY,
    } = getDimensionsByEdge(
      applicationStore.touchedEdge,
      windowHeight.value,
      windowWidth.value
    );
    dialogHeight.value = height;
    dialogWidth.value = width;
    x.value = newX;
    y.value = newY;
    applicationStore.touchedEdge = undefined;
  },
  onStart: () => {
    emit("raise");
  },
});

const maximize = () => {
  if (!maximized.value) {
    previousDimensions.value = {
      x: x.value,
      y: y.value,
      height: dialogHeight.value,
      width: dialogWidth.value,
    };
    x.value = 0;
    y.value = 0;
    dialogHeight.value = windowHeight.value;
    dialogWidth.value = windowWidth.value;
  } else if (previousDimensions.value) {
    x.value = previousDimensions.value.x;
    y.value = previousDimensions.value.y;
    dialogHeight.value = previousDimensions.value.height;
    dialogWidth.value = previousDimensions.value.width;
  }
  maximized.value = !maximized.value;
};

useDraggable(resizeIconContainer, {
  preventDefault: true,
  onMove: (position) => {
    const iconWidth = resizeIconContainer.value.clientWidth;
    dialogHeight.value = position.y - y.value + iconWidth;
    dialogWidth.value = position.x - x.value + iconWidth;
    if (setDialogWidth) setDialogWidth(dialogWidth.value);
  },
  onStart: () => {
    if (maximized.value) previousDimensions.value = undefined;
    maximized.value = false;
  },
});
</script>
