import { expect, test } from "@playwright/test";

const apiBase = process.env.CIPHERMARKET_API_URL;

test.describe("optional API smoke", () => {
  test.skip(!apiBase, "Set CIPHERMARKET_API_URL to run API checks");

  test("health and public catalogue endpoints respond", async ({ request }) => {
    const health = await request.get(`${apiBase}/actuator/health`);
    expect(health.ok()).toBeTruthy();

    const categories = await request.get(`${apiBase}/api/v1/categories`);
    expect(categories.ok()).toBeTruthy();

    const catalogue = await request.get(`${apiBase}/api/v1/catalogue/products`);
    expect(catalogue.ok()).toBeTruthy();
  });
});
