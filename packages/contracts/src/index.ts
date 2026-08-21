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
  organisationId: string;
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
  organisationSlug: string | null;
  fullDescription: string | null;
  usageTerms: string | null;
  refundPolicy: string | null;
  coverImageUrl: string | null;
}

export type CatalogueSort = "NEWEST" | "PRICE_ASC" | "PRICE_DESC" | "NAME_ASC";

export interface CatalogueSearchParams {
  q?: string;
  categoryId?: string;
  organisationId?: string;
  productType?: ProductType;
  minPriceCents?: number;
  maxPriceCents?: number;
  sort?: CatalogueSort;
}

export interface CreatorStorefront {
  organisation: {
    id: string;
    name: string;
    slug: string;
    description: string | null;
  };
  products: CatalogueProduct[];
}

export interface SalesAnalytics {
  paidOrderCount: number;
  unitsSold: number;
  revenueCents: number;
  currency: string;
  products: Array<{
    productId: string;
    productName: string;
    unitsSold: number;
    revenueCents: number;
    currency: string;
  }>;
}

export type SuspiciousReportCategory = "LEAK" | "FRAUD" | "ABUSE" | "MALWARE" | "OTHER";

export interface CreateSuspiciousReportRequest {
  category: SuspiciousReportCategory;
  summary: string;
  resourceType?: string;
  resourceId?: string;
  details?: string;
}

export interface SuspiciousReportResponse {
  eventType: string;
  category: string;
  summary: string;
  resourceId: string | null;
  submittedAt: string;
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
  productName: string;
  productType: ProductType;
  orderId: string;
  status: EntitlementStatus;
  grantedAt: string;
  hasLicence: boolean;
}

export interface Licence {
  id: string;
  entitlementId: string;
  productId: string;
  productVersionId: string;
  signedToken: string;
  issuedAt: string;
  expiresAt: string;
  status: string;
}

export interface AccessGrant {
  id: string;
  accessToken: string;
  expiresAt: string;
  maxUses: number;
  useCount: number;
}

export interface RegisteredDevice {
  id: string;
  label: string;
  status: string;
  registeredAt: string;
  lastSeenAt: string | null;
}

export interface RegisterDeviceRequest {
  fingerprint: string;
  label: string;
}

export interface CreateAccessGrantRequest {
  deviceId?: string | null;
}

export type SecurityEventSeverity = "INFO" | "LOW" | "MEDIUM" | "HIGH" | "CRITICAL";
export type SecurityEventStatus = "OPEN" | "ACKNOWLEDGED" | "CLOSED";
export type ApprovalActionType =
  | "PRODUCT_SUSPEND"
  | "ENTITLEMENT_REVOKE"
  | "LICENCE_REVOKE"
  | "REFUND_APPROVE";
export type ApprovalStatus = "PENDING" | "APPROVED" | "REJECTED" | "CANCELLED";

export interface SecurityEvent {
  id: string;
  organisationId: string | null;
  actorUserId: string | null;
  eventType: string;
  severity: SecurityEventSeverity;
  status: SecurityEventStatus;
  resourceType: string | null;
  resourceId: string | null;
  summary: string;
  details?: Record<string, unknown> | null;
  correlationId: string | null;
  createdAt: string;
}

export interface AuditEventRecord {
  id: string;
  organisationId: string | null;
  actorUserId: string | null;
  actorKeycloakSub: string | null;
  action: string;
  resourceType: string;
  resourceId: string | null;
  correlationId: string;
  eventHash: string;
  previousHash: string;
  createdAt: string;
}

export interface AuditVerifyResult {
  intact: boolean;
  eventCount: number;
  headHash: string;
  brokenAtEventId: string | null;
  detail: string;
}

export interface AuditBatch {
  id: string;
  firstEventId: string;
  lastEventId: string;
  eventCount: number;
  rootHash: string;
  previousBatchHash: string | null;
  sealedByUserId: string | null;
  sealedAt: string;
}

export interface ApprovalRequestRecord {
  id: string;
  actionType: ApprovalActionType;
  resourceType: string;
  resourceId: string;
  organisationId: string | null;
  payload?: Record<string, unknown> | null;
  reason: string;
  status: ApprovalStatus;
  requestedBy: string;
  decidedBy: string | null;
  decisionReason: string | null;
  requestedAt: string;
  decidedAt: string | null;
}

export interface CreateApprovalRequest {
  actionType: ApprovalActionType;
  resourceId: string;
  reason: string;
  payload?: Record<string, unknown> | null;
}

export type DisclosureDocumentStatus = "PROCESSING" | "READY" | "FAILED" | "REVOKED";

export type DisclosureRequestStatus =
  | "PENDING"
  | "ACCEPTED"
  | "REJECTED"
  | "REVOKED"
  | "EXPIRED";

export interface DisclosureDocument {
  id: string;
  organisationId: string;
  title: string;
  description: string | null;
  originalFileName: string;
  sha256Checksum: string | null;
  documentVersion: number;
  status: DisclosureDocumentStatus;
  fileSizeBytes: number | null;
  createdAt: string;
}

export interface DisclosureRequestRecord {
  id: string;
  organisationId: string;
  documentId: string;
  documentTitle: string | null;
  documentSha256: string | null;
  documentVersion: number | null;
  createdByUserId: string;
  recipientUserId: string;
  recipientEmail: string;
  confidentialityTerms: string;
  status: DisclosureRequestStatus;
  expiresAt: string | null;
  acceptedAt: string | null;
  rejectedAt: string | null;
  revokedAt: string | null;
  disclosedAt: string;
  createdAt: string;
}

export interface CreateDisclosureRequest {
  recipientEmail: string;
  confidentialityTerms: string;
  expiresAt?: string | null;
}

export type RefundRequestStatus =
  | "REQUESTED"
  | "UNDER_REVIEW"
  | "APPROVED"
  | "REJECTED"
  | "CANCELLED"
  | "COMPLETED";

export interface RefundRequestRecord {
  id: string;
  orderId: string;
  paymentId: string;
  buyerUserId: string;
  organisationId: string | null;
  amountCents: number;
  currency: string;
  reason: string;
  status: RefundRequestStatus;
  rejectionReason: string | null;
  approvalRequestId: string | null;
  providerRefundRef: string | null;
  requestedAt: string;
  decidedAt: string | null;
  completedAt: string | null;
}

export interface CreateRefundRequest {
  reason: string;
}
