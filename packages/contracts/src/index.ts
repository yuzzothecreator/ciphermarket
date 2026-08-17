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
