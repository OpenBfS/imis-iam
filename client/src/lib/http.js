import axios from "axios";

const HTTP = axios.create({
  baseURL: "/backend/realms/imis3",
});

const ShibHTTP = axios.create({
  baseURL: "/",
});

export { HTTP, ShibHTTP };
