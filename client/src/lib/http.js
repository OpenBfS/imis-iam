import axios from "axios";

const HTTP = axios.create({
  baseURL: "/backend/realms/imis3"
});

export {HTTP}
