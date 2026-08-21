import type {
  AccessGrant,
  AddToCartRequest,
  Cart,
  CatalogueProduct,
  CatalogueProductDetail,
  CatalogueSearchParams,
  Category,
  CheckoutResponse,
  CreateAccessGrantRequest,
  CreatorStorefront,
  Entitlement,
  HealthStatus,
  Licence,
  Order,
  ProblemDetail,
  SalesAnalytics,
} from "@ciphermarket/contracts";

export interface CipherMarketClientOptions {
  baseUrl: string;
  accessToken?: string;
  fetch?: typeof fetch;
}

export class CipherMarketApiError extends Error {
  readonly status: number;
  readonly problem?: ProblemDetail;

  constructor(message: string, status: number, problem?: ProblemDetail) {
    super(message);
    this.name = "CipherMarketApiError";
    this.status = status;
    this.problem = problem;
  }
}

export class CipherMarketClient {
  private readonly baseUrl: string;
  private readonly accessToken?: string;
  private readonly fetchImpl: typeof fetch;

  constructor(options: CipherMarketClientOptions) {
    this.baseUrl = options.baseUrl.replace(/\/$/, "");
    this.accessToken = options.accessToken;
    this.fetchImpl = options.fetch ?? fetch;
  }

  withAccessToken(accessToken: string): CipherMarketClient {
    return new CipherMarketClient({
      baseUrl: this.baseUrl,
      accessToken,
      fetch: this.fetchImpl,
    });
  }

  getHealth() {
    return this.request<HealthStatus>("/actuator/health");
  }

  listCategories() {
    return this.request<Category[]>("/api/v1/categories");
  }

  listCatalogue(params?: CatalogueSearchParams | string) {
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
    return this.request<CatalogueProduct[]>(`/api/v1/catalogue/products${query}`);
  }

  getCatalogueProduct(productId: string) {
    return this.request<CatalogueProductDetail>(`/api/v1/catalogue/products/${productId}`);
  }

  getCreatorStorefront(slug: string) {
    return this.request<CreatorStorefront>(
      `/api/v1/catalogue/creators/${encodeURIComponent(slug)}`,
    );
  }

  getSalesAnalytics(organisationId: string) {
    return this.request<SalesAnalytics>(
      `/api/v1/organisations/${organisationId}/analytics/sales`,
    );
  }

  getCart() {
    return this.request<Cart>("/api/v1/cart");
  }

  addToCart(body: AddToCartRequest) {
    return this.request<Cart>("/api/v1/cart/items", { method: "POST", json: body });
  }

  checkout() {
    return this.request<CheckoutResponse>("/api/v1/checkout", { method: "POST" });
  }

  listOrders() {
    return this.request<Order[]>("/api/v1/orders");
  }

  listEntitlements() {
    return this.request<Entitlement[]>("/api/v1/entitlements");
  }

  issueLicence(entitlementId: string) {
    return this.request<Licence>(`/api/v1/entitlements/${entitlementId}/licence`, {
      method: "POST",
    });
  }

  createAccessGrant(entitlementId: string, body: CreateAccessGrantRequest = {}) {
    return this.request<AccessGrant>(`/api/v1/entitlements/${entitlementId}/access-grants`, {
      method: "POST",
      json: body,
    });
  }

  downloadUrl(accessToken: string): string {
    return `${this.baseUrl}/api/v1/delivery/download?token=${encodeURIComponent(accessToken)}`;
  }

  private async request<T>(path: string, init?: RequestInit & { json?: unknown }): Promise<T> {
    const headers = new Headers(init?.headers);
    if (init?.json !== undefined) {
      headers.set("Content-Type", "application/json");
    }
    if (this.accessToken) {
      headers.set("Authorization", `Bearer ${this.accessToken}`);
    }

    const response = await this.fetchImpl(`${this.baseUrl}${path}`, {
      ...init,
      headers,
      body: init?.json !== undefined ? JSON.stringify(init.json) : init?.body,
    });

    if (!response.ok) {
      const problem = await parseProblem(response);
      throw new CipherMarketApiError(
        problem?.detail ?? problem?.title ?? `CipherMarket request failed (${response.status})`,
        response.status,
        problem,
      );
    }

    if (response.status === 204) {
      return undefined as T;
    }

    return (await response.json()) as T;
  }
}

async function parseProblem(response: Response): Promise<ProblemDetail | undefined> {
  try {
    return (await response.json()) as ProblemDetail;
  } catch {
    return undefined;
  }
}
