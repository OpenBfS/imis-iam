<template>
  <div>
    <v-alert v-if="isSuccessful" type="success" dense color="blue" text>
      {{ message }}
    </v-alert>
    <v-alert v-else type="error" dense text>
      {{ message }}
    </v-alert>
  </div>
</template>

<script>
export default {
  props: {
    isSuccessful: Boolean,
    message: String,
  },
};
</script>
