<template>
  <v-app-bar dark id="theme" app clipped-left>
    <v-btn @click="$route.path == '/profile' ? '' : $router.push('/profile')">
      Profile
    </v-btn>
  </v-app-bar>
</template>
<style scoped></style>
<script>
export default {};
</script>
