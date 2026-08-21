import type {
  AccessGrant,
  AddToCartRequest,
  Cart,
  CatalogueProduct,
  CatalogueProductDetail,
  CatalogueSearchParams,
  CheckoutResponse,
  CreateAccessGrantRequest,
  CreateSuspiciousReportRequest,
  CreatorStorefront,
  Entitlement,
  Licence,
  Order,
  RefundRequestRecord,
  RegisterDeviceRequest,
  RegisteredDevice,
  SuspiciousReportResponse,
} from "@ciphermarket/contracts";
import { clientFetch } from "@/lib/client-api";

export function listCatalogueProducts(params?: CatalogueSearchParams | string) {
  const search = new URLSearchParams();
  if (typeof params === "string") {
    if (params) search.set("categoryId", params);
  } else if (params) {
    if (params.q) search.set("q", params.q);
    if (params.categoryId) search.set("categoryId", params.categoryId);
    if (params.organisationId) search.set("organisationId", params.organisationId);
    if (params.productType) search.set("productType", params.productType);
    if (params.minPriceCents != null) search.set("minPriceCents", String(params.minPriceCents));
    if (params.maxPriceCents != null) search.set("maxPriceCents", String(params.maxPriceCents));
    if (params.sort) search.set("sort", params.sort);
  }
  const query = search.toString() ? `?${search}` : "";
  return clientFetch<CatalogueProduct[]>(`/api/v1/catalogue/products${query}`);
}

export function getCreatorStorefront(slug: string) {
  return clientFetch<CreatorStorefront>(`/api/v1/catalogue/creators/${encodeURIComponent(slug)}`);
}

export function reportSuspiciousActivity(token: string, body: CreateSuspiciousReportRequest) {
  return clientFetch<SuspiciousReportResponse>("/api/v1/reports/suspicious", {
    method: "POST",
    token,
    json: body,
  });
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

export function requestRefund(token: string, orderId: string, reason: string) {
  return clientFetch<RefundRequestRecord>(`/api/v1/orders/${orderId}/refund-requests`, {
    method: "POST",
    token,
    json: { reason },
  });
}

export function listMyRefunds(token: string) {
  return clientFetch<RefundRequestRecord[]>("/api/v1/refund-requests", { token });
}

export function cancelRefund(token: string, refundId: string) {
  return clientFetch<RefundRequestRecord>(`/api/v1/refund-requests/${refundId}/cancel`, {
    method: "POST",
    token,
  });
}
