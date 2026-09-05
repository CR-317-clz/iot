import axios from "axios";
// 登录接口
export function loginApi(data) {
  return axios.post("/api/login", data);
}
