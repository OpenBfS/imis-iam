<template>
  <div>
    <v-alert
      v-if="!isWarning"
      dense
      border="start"
      border-color="green"
      text
      title="Current Issue"
    >
      <div class="ml-2">
        {{ message }}
      </div>
    </v-alert>
    <v-alert
      v-else
      dense
      text
      color="#E57373"
      border="start"
      title="fault report"
    >
      <div class="ml-2">
        {{ message }}
      </div>
    </v-alert>
  </div>
</template>
<script>
export default {
  //props: ["message", "isWarning"],
  props: {
    message: String,
    isWarning: Boolean,
  },
};
</script>
