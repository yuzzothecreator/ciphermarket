export type UserStatus = "ACTIVE" | "SUSPENDED" | "DELETED";

export type OrganisationStatus = "ACTIVE" | "SUSPENDED" | "ARCHIVED";

export type OrganisationRole =
  | "OWNER"
  | "ADMINISTRATOR"
  | "PRODUCT_MANAGER"
  | "FINANCE_OFFICER"
  | "SUPPORT_OFFICER"
  | "SECURITY_VIEWER";

export type PlatformRole =
  | "buyer"
  | "creator"
  | "marketplace_admin"
  | "security_auditor"
  | "support_officer";

export interface UserProfile {
  id: string;
  email: string;
  displayName: string;
  avatarUrl?: string | null;
  locale: string;
  timezone: string;
  status: UserStatus;
  mfaEnabled: boolean;
  createdAt: string;
}

export interface Organisation {
  id: string;
  name: string;
  slug: string;
  description?: string | null;
  status: OrganisationStatus;
  ownerUserId: string;
  createdAt: string;
}

export interface Membership {
  id: string;
  organisationId: string;
  userId: string;
  role: OrganisationRole;
  status: string;
  joinedAt: string;
}

export interface Category {
  id: string;
  name: string;
  slug: string;
  description?: string | null;
  sortOrder: number;
}

export interface ProblemDetail {
  type?: string;
  title?: string;
  status?: number;
  detail?: string;
  correlationId?: string;
}

export interface HealthStatus {
  status: "UP" | "DOWN";
  components?: Record<string, { status: string }>;
}

export type ProductType = "PDF" | "SOURCE_CODE" | "DESIGN" | "GENERAL";

export type ProductStatus =
  | "DRAFT"
  | "UPLOADING"
  | "SCANNING"
  | "PROCESSING"
  | "UNDER_REVIEW"
  | "PUBLISHED"
  | "SUSPENDED"
  | "ARCHIVED";

export type ProductVersionStatus =
  | "DRAFT"
  | "UPLOADING"
  | "SCANNING"
  | "PROCESSING"
  | "UNDER_REVIEW"
  | "PUBLISHED"
  | "SUSPENDED"
  | "REVOKED"
  | "ARCHIVED";

export type UploadSessionStatus =
  | "INITIATED"
  | "UPLOADING"
  | "UPLOADED"
  | "PROCESSING"
  | "COMPLETED"
  | "FAILED"
  | "EXPIRED";

export interface Product {
  id: string;
  organisationId: string;
  categoryId: string | null;
  name: string;
  slug: string;
  shortDescription: string | null;
  fullDescription: string | null;
  productType: ProductType;
  status: ProductStatus;
  priceCents: number;
  currency: string;
  licenceType: string | null;
  currentVersionId: string | null;
  createdAt: string;
  updatedAt: string;
}

export interface ProductVersion {
  id: string;
  productId: string;
  versionLabel: string;
  changelog: string | null;
  status: ProductVersionStatus;
  publishedAt: string | null;
  createdAt: string;
}

export interface UploadSession {
  id: string;
  productId: string;
  productVersionId: string;
  assetId: string;
  status: UploadSessionStatus;
  quarantineObjectKey: string | null;
  maxSizeBytes: number;
  expiresAt: string;
}

export interface CreateProductRequest {
  name: string;
  slug: string;
  productType: ProductType;
  categoryId?: string | null;
  shortDescription?: string;
  priceCents?: number;
  currency?: string;
}

export interface CreateProductVersionRequest {
  versionLabel: string;
  changelog?: string;
}

export interface CreateUploadSessionRequest {
  productVersionId: string;
  fileName: string;
  contentType: string;
}

export interface CreateOrganisationRequest {
  name: string;
  slug: string;
  description?: string;
}

export type OrderStatus = "PENDING_PAYMENT" | "PAID" | "FAILED" | "CANCELLED" | "REFUNDED";

export type EntitlementStatus = "ACTIVE" | "REVOKED" | "EXPIRED";

export interface CatalogueProduct {
  id: string;
  categoryId: string | null;
  name: string;
  slug: string;
  shortDescription: string | null;
  productType: ProductType;
  priceCents: number;
  currency: string;
  licenceType: string | null;
  publishedAt: string;
}

export interface CatalogueProductDetail extends CatalogueProduct {
  fullDescription: string | null;
  usageTerms: string | null;
  refundPolicy: string | null;
  coverImageUrl: string | null;
}

export interface CartItem {
  id: string;
  productId: string;
  productName: string;
  productSlug: string;
  unitPriceCents: number;
  currency: string;
  quantity: number;
  lineTotalCents: number;
}

export interface Cart {
  id: string | null;
  items: CartItem[];
  subtotalCents: number;
  currency: string;
  itemCount: number;
}

export interface AddToCartRequest {
  productId: string;
  quantity: number;
}

export interface CheckoutResponse {
  orderId: string;
  paymentId: string;
  paymentProvider: string;
  amountCents: number;
  currency: string;
  checkoutUrl: string;
  requiresPayment: boolean;
}

export interface OrderItem {
  id: string;
  productId: string;
  productName: string;
  productSlug: string;
  unitPriceCents: number;
  currency: string;
  quantity: number;
  lineTotalCents: number;
}

export interface Order {
  id: string;
  status: OrderStatus;
  subtotalCents: number;
  currency: string;
  paidAt: string | null;
  createdAt: string;
  items: OrderItem[];
}

export interface Entitlement {
  id: string;
  productId: string;
  orderId: string;
  status: EntitlementStatus;
  grantedAt: string;
}
