import { expect, test } from "@playwright/test";

test.describe("public pages", () => {
  test("home page renders marketplace copy", async ({ page }) => {
    await page.goto("/");
    await expect(page.getByRole("heading", { level: 1 })).toContainText(
      "Secure digital product distribution",
    );
    await expect(page.getByRole("link", { name: "Browse catalogue" })).toBeVisible();
  });

  test("catalogue page is reachable", async ({ page }) => {
    await page.goto("/catalogue");
    await expect(page.getByRole("heading", { name: "Product catalogue" })).toBeVisible();
  });

  test("security, privacy, and terms pages render", async ({ page }) => {
    await page.goto("/security");
    await expect(page.getByRole("heading", { name: /Security & buyer protection/i })).toBeVisible();

    await page.goto("/privacy");
    await expect(page.getByRole("heading", { name: "Privacy Policy" })).toBeVisible();

    await page.goto("/terms");
    await expect(page.getByRole("heading", { name: "Terms of Service" })).toBeVisible();
  });
});
