import type {
  AddToCartRequest,
  AccessGrant,
  Cart,
  CatalogueProduct,
  CatalogueProductDetail,
  CheckoutResponse,
  CreateAccessGrantRequest,
  Entitlement,
  Licence,
  Order,
  RegisterDeviceRequest,
  RegisteredDevice,
} from "@ciphermarket/contracts";
import { clientFetch } from "@/lib/client-api";

export function listCatalogueProducts(categoryId?: string) {
  const query = categoryId ? `?categoryId=${categoryId}` : "";
  return clientFetch<CatalogueProduct[]>(`/api/v1/catalogue/products${query}`);
}

export function getCatalogueProduct(productId: string) {
  return clientFetch<CatalogueProductDetail>(`/api/v1/catalogue/products/${productId}`);
}

export function getCart(token: string) {
  return clientFetch<Cart>("/api/v1/cart", { token });
}

export function addToCart(token: string, body: AddToCartRequest) {
  return clientFetch<Cart>("/api/v1/cart/items", { method: "POST", token, json: body });
}

export function updateCartItem(token: string, itemId: string, quantity: number) {
  return clientFetch<Cart>(`/api/v1/cart/items/${itemId}`, {
    method: "PUT",
    token,
    json: { quantity },
  });
}

export function removeCartItem(token: string, itemId: string) {
  return clientFetch<Cart>(`/api/v1/cart/items/${itemId}`, { method: "DELETE", token });
}

export function checkout(token: string) {
  return clientFetch<CheckoutResponse>("/api/v1/checkout", { method: "POST", token });
}

export function simulatePayment(token: string, paymentId: string) {
  return clientFetch<void>(`/api/v1/payments/${paymentId}/simulate`, { method: "POST", token });
}

export function listOrders(token: string) {
  return clientFetch<Order[]>("/api/v1/orders", { token });
}

export function getOrder(token: string, orderId: string) {
  return clientFetch<Order>(`/api/v1/orders/${orderId}`, { token });
}

export function listEntitlements(token: string) {
  return clientFetch<Entitlement[]>("/api/v1/entitlements", { token });
}

export function issueLicence(token: string, entitlementId: string) {
  return clientFetch<Licence>(`/api/v1/entitlements/${entitlementId}/licence`, {
    method: "POST",
    token,
  });
}

export function createAccessGrant(
  token: string,
  entitlementId: string,
  body?: CreateAccessGrantRequest,
) {
  return clientFetch<AccessGrant>(`/api/v1/entitlements/${entitlementId}/access-grants`, {
    method: "POST",
    token,
    json: body ?? {},
  });
}

export function getDownloadUrl(accessToken: string): string {
  const API_URL = process.env.NEXT_PUBLIC_API_URL ?? "http://localhost:8080";
  return `${API_URL}/api/v1/delivery/download?token=${encodeURIComponent(accessToken)}`;
}

export function registerDevice(token: string, body: RegisterDeviceRequest) {
  return clientFetch<RegisteredDevice>("/api/v1/devices", { method: "POST", token, json: body });
}

export function listDevices(token: string) {
  return clientFetch<RegisteredDevice[]>("/api/v1/devices", { token });
}
