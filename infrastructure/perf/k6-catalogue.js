import http from "k6/http";
import { check, sleep } from "k6";

const BASE = __ENV.API_BASE_URL || "http://localhost:8080";

export const options = {
  scenarios: {
    catalogue_smoke: {
      executor: "constant-vus",
      vus: Number(__ENV.VUS || 10),
      duration: __ENV.DURATION || "30s",
    },
  },
  thresholds: {
    http_req_failed: ["rate<0.05"],
    http_req_duration: ["p(95)<800"],
  },
};

export default function catalogueLoad() {
  const health = http.get(`${BASE}/actuator/health`);
  check(health, {
    "health is 200": (response) => response.status === 200,
  });

  const categories = http.get(`${BASE}/api/v1/categories`);
  check(categories, {
    "categories is 200": (response) => response.status === 200,
  });

  const catalogue = http.get(`${BASE}/api/v1/catalogue/products`);
  check(catalogue, {
    "catalogue is 200": (response) => response.status === 200,
  });

  sleep(1);
}
