import type {
  CreateOrganisationRequest,
  CreateProductRequest,
  CreateProductVersionRequest,
  CreateUploadSessionRequest,
  Organisation,
  Product,
  ProductVersion,
  SalesAnalytics,
  UploadSession,
} from "@ciphermarket/contracts";
import { clientFetch, clientUpload } from "@/lib/client-api";

export function listOrganisations(token: string) {
  return clientFetch<Organisation[]>("/api/v1/organisations", { token });
}

export function getSalesAnalytics(token: string, organisationId: string) {
  return clientFetch<SalesAnalytics>(
    `/api/v1/organisations/${organisationId}/analytics/sales`,
    { token },
  );
}

export function createOrganisation(token: string, body: CreateOrganisationRequest) {
  return clientFetch<Organisation>("/api/v1/organisations", {
    method: "POST",
    token,
    json: body,
  });
}

export function listProducts(token: string, organisationId: string) {
  return clientFetch<Product[]>(`/api/v1/organisations/${organisationId}/products`, { token });
}

export function createProduct(token: string, organisationId: string, body: CreateProductRequest) {
  return clientFetch<Product>(`/api/v1/organisations/${organisationId}/products`, {
    method: "POST",
    token,
    json: body,
  });
}

export function getProduct(token: string, organisationId: string, productId: string) {
  return clientFetch<Product>(
    `/api/v1/organisations/${organisationId}/products/${productId}`,
    { token },
  );
}

export function listVersions(token: string, organisationId: string, productId: string) {
  return clientFetch<ProductVersion[]>(
    `/api/v1/organisations/${organisationId}/products/${productId}/versions`,
    { token },
  );
}

export function createVersion(
  token: string,
  organisationId: string,
  productId: string,
  body: CreateProductVersionRequest,
) {
  return clientFetch<ProductVersion>(
    `/api/v1/organisations/${organisationId}/products/${productId}/versions`,
    { method: "POST", token, json: body },
  );
}

export function submitForReview(token: string, organisationId: string, productId: string) {
  return clientFetch<Product>(
    `/api/v1/organisations/${organisationId}/products/${productId}/submit-review`,
    { method: "POST", token },
  );
}

export function publishVersion(
  token: string,
  organisationId: string,
  productId: string,
  versionId: string,
) {
  return clientFetch<Product>(
    `/api/v1/organisations/${organisationId}/products/${productId}/versions/${versionId}/publish`,
    { method: "POST", token },
  );
}

export function createUploadSession(
  token: string,
  organisationId: string,
  productId: string,
  body: CreateUploadSessionRequest,
) {
  return clientFetch<UploadSession>(
    `/api/v1/organisations/${organisationId}/products/${productId}/uploads/sessions`,
    { method: "POST", token, json: body },
  );
}

export function uploadSessionFile(
  token: string,
  organisationId: string,
  productId: string,
  sessionId: string,
  file: File,
) {
  return clientUpload<UploadSession>(
    `/api/v1/organisations/${organisationId}/products/${productId}/uploads/sessions/${sessionId}/file`,
    file,
    token,
  );
}

export function getUploadSession(
  token: string,
  organisationId: string,
  productId: string,
  sessionId: string,
) {
  return clientFetch<UploadSession>(
    `/api/v1/organisations/${organisationId}/products/${productId}/uploads/sessions/${sessionId}`,
    { token },
  );
}
