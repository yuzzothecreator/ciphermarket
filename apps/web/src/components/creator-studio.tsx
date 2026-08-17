"use client";

import type { Organisation, Product, ProductType, ProductVersion } from "@ciphermarket/contracts";
import { Badge, Button, Card, CardContent, CardDescription, CardHeader, CardTitle } from "@ciphermarket/ui";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { ArrowLeft, Loader2, Plus, Upload } from "lucide-react";
import Link from "next/link";
import { useMemo, useState } from "react";
import { useAuth } from "@/lib/auth-context";
import {
  createOrganisation,
  createProduct,
  createUploadSession,
  createVersion,
  getUploadSession,
  listOrganisations,
  listProducts,
  listVersions,
  publishVersion,
  submitForReview,
  uploadSessionFile,
} from "@/lib/creator-api";

const inputClass =
  "flex h-10 w-full rounded-lg border border-border bg-background px-3 text-sm outline-none focus-visible:ring-2 focus-visible:ring-ring";

const textareaClass =
  "flex min-h-24 w-full rounded-lg border border-border bg-background px-3 py-2 text-sm outline-none focus-visible:ring-2 focus-visible:ring-ring";

const productTypes: ProductType[] = ["PDF", "SOURCE_CODE", "DESIGN", "GENERAL"];

function slugify(value: string): string {
  return value
    .toLowerCase()
    .trim()
    .replace(/[^a-z0-9]+/g, "-")
    .replace(/^-+|-+$/g, "");
}

function statusVariant(status: string): "default" | "accent" | "outline" {
  if (status === "PUBLISHED" || status === "COMPLETED") return "accent";
  if (status === "FAILED" || status === "SUSPENDED") return "outline";
  return "default";
}

function formatPrice(cents: number, currency: string): string {
  return new Intl.NumberFormat("en-GB", {
    style: "currency",
    currency: currency || "GBP",
  }).format(cents / 100);
}

export function CreatorStudio() {
  const { accessToken, isAuthenticated, isLoading: authLoading } = useAuth();
  const queryClient = useQueryClient();
  const [selectedOrgId, setSelectedOrgId] = useState<string>("");
  const [selectedProductId, setSelectedProductId] = useState<string | null>(null);
  const [showCreateOrg, setShowCreateOrg] = useState(false);
  const [showCreateProduct, setShowCreateProduct] = useState(false);

  const orgsQuery = useQuery({
    queryKey: ["organisations"],
    queryFn: () => listOrganisations(accessToken!),
    enabled: Boolean(accessToken),
  });

  const activeOrgId = selectedOrgId || orgsQuery.data?.[0]?.id || "";

  const productsQuery = useQuery({
    queryKey: ["products", activeOrgId],
    queryFn: () => listProducts(accessToken!, activeOrgId),
    enabled: Boolean(accessToken && activeOrgId),
  });

  const selectedProduct = useMemo(
    () => productsQuery.data?.find((p) => p.id === selectedProductId) ?? null,
    [productsQuery.data, selectedProductId],
  );

  if (authLoading) {
    return <StudioShell message="Checking authentication…" />;
  }

  if (!isAuthenticated || !accessToken) {
    return (
      <StudioShell
        title="Creator Studio"
        message="Sign in with Keycloak to manage products, secure uploads, and publishing."
        action={
          <Button asChild>
            <Link href="/auth/sign-in">Sign in with Keycloak</Link>
          </Button>
        }
      />
    );
  }

  if (orgsQuery.isLoading) {
    return <StudioShell message="Loading organisations…" />;
  }

  if (orgsQuery.isError) {
    return (
      <StudioShell
        title="Creator Studio"
        message="Unable to load organisations. Ensure the API and Keycloak are running."
        error
      />
    );
  }

  if (!orgsQuery.data?.length) {
    return (
      <div className="mx-auto max-w-7xl px-4 py-12 sm:px-6">
        <PageHeader />
        {showCreateOrg ? (
          <CreateOrganisationForm
            token={accessToken}
            onCreated={(org) => {
              queryClient.invalidateQueries({ queryKey: ["organisations"] });
              setSelectedOrgId(org.id);
              setShowCreateOrg(false);
            }}
            onCancel={() => setShowCreateOrg(false)}
          />
        ) : (
          <Card className="mt-8">
            <CardHeader>
              <CardTitle>Create your organisation</CardTitle>
              <CardDescription>
                Products belong to an organisation. Create one to start listing digital goods.
              </CardDescription>
            </CardHeader>
            <CardContent>
              <Button onClick={() => setShowCreateOrg(true)}>
                <Plus className="mr-2 size-4" />
                New organisation
              </Button>
            </CardContent>
          </Card>
        )}
      </div>
    );
  }

  if (selectedProduct) {
    return (
      <div className="mx-auto max-w-7xl px-4 py-12 sm:px-6">
        <ProductDetail
          token={accessToken}
          organisationId={activeOrgId}
          product={selectedProduct}
          onBack={() => setSelectedProductId(null)}
        />
      </div>
    );
  }

  return (
    <div className="mx-auto max-w-7xl px-4 py-12 sm:px-6">
      <PageHeader />

      <div className="mt-8 flex flex-wrap items-center gap-4">
        <label className="text-sm text-muted-foreground">
          Organisation
          <select
            className={`${inputClass} mt-1 min-w-56`}
            value={activeOrgId}
            onChange={(e) => setSelectedOrgId(e.target.value)}
          >
            {orgsQuery.data.map((org) => (
              <option key={org.id} value={org.id}>
                {org.name}
              </option>
            ))}
          </select>
        </label>
        <Button variant="secondary" onClick={() => setShowCreateOrg((v) => !v)}>
          {showCreateOrg ? "Cancel" : "New organisation"}
        </Button>
        <Button onClick={() => setShowCreateProduct((v) => !v)}>
          <Plus className="mr-2 size-4" />
          New product
        </Button>
      </div>

      {showCreateOrg && (
        <div className="mt-6">
          <CreateOrganisationForm
            token={accessToken}
            onCreated={(org) => {
              queryClient.invalidateQueries({ queryKey: ["organisations"] });
              setSelectedOrgId(org.id);
              setShowCreateOrg(false);
            }}
            onCancel={() => setShowCreateOrg(false)}
          />
        </div>
      )}

      {showCreateProduct && (
        <div className="mt-6">
          <CreateProductForm
            token={accessToken}
            organisationId={activeOrgId}
            onCreated={(product) => {
              queryClient.invalidateQueries({ queryKey: ["products", activeOrgId] });
              setShowCreateProduct(false);
              setSelectedProductId(product.id);
            }}
            onCancel={() => setShowCreateProduct(false)}
          />
        </div>
      )}

      <div className="mt-10">
        {productsQuery.isLoading && <p className="text-sm text-muted-foreground">Loading products…</p>}
        {productsQuery.isError && (
          <p className="text-sm text-destructive">Unable to load products for this organisation.</p>
        )}
        {productsQuery.data && productsQuery.data.length === 0 && (
          <Card>
            <CardContent className="py-10 text-center">
              <p className="text-sm text-muted-foreground">No products yet. Create your first listing.</p>
            </CardContent>
          </Card>
        )}
        {productsQuery.data && productsQuery.data.length > 0 && (
          <ul className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
            {productsQuery.data.map((product) => (
              <li key={product.id}>
                <button
                  type="button"
                  className="w-full rounded-xl border border-border bg-card p-5 text-left transition-colors hover:border-accent/40"
                  onClick={() => setSelectedProductId(product.id)}
                >
                  <div className="flex items-start justify-between gap-2">
                    <h3 className="font-medium">{product.name}</h3>
                    <Badge variant={statusVariant(product.status)}>{product.status}</Badge>
                  </div>
                  <p className="mt-2 text-sm text-muted-foreground">{product.shortDescription || product.slug}</p>
                  <p className="mt-3 text-sm font-medium">{formatPrice(product.priceCents, product.currency)}</p>
                </button>
              </li>
            ))}
          </ul>
        )}
      </div>
    </div>
  );
}

function PageHeader() {
  return (
    <>
      <Badge variant="outline" className="mb-4">
        Phase 2
      </Badge>
      <h1 className="text-3xl font-semibold tracking-tight">Creator Studio</h1>
      <p className="mt-3 max-w-2xl text-muted-foreground">
        Create products, upload assets through the secure quarantine pipeline, and publish versions when ready.
      </p>
    </>
  );
}

function StudioShell({
  title = "Creator Studio",
  message,
  action,
  error,
}: {
  title?: string;
  message: string;
  action?: React.ReactNode;
  error?: boolean;
}) {
  return (
    <div className="mx-auto max-w-7xl px-4 py-12 sm:px-6">
      <h1 className="text-3xl font-semibold tracking-tight">{title}</h1>
      <p className={`mt-3 max-w-2xl text-sm ${error ? "text-destructive" : "text-muted-foreground"}`}>
        {message}
      </p>
      {action && <div className="mt-8">{action}</div>}
    </div>
  );
}

function CreateOrganisationForm({
  token,
  onCreated,
  onCancel,
}: {
  token: string;
  onCreated: (org: Organisation) => void;
  onCancel: () => void;
}) {
  const [name, setName] = useState("");
  const [slug, setSlug] = useState("");
  const [description, setDescription] = useState("");

  const mutation = useMutation({
    mutationFn: () =>
      createOrganisation(token, {
        name,
        slug: slug || slugify(name),
        description: description || undefined,
      }),
    onSuccess: onCreated,
  });

  return (
    <Card>
      <CardHeader>
        <CardTitle>New organisation</CardTitle>
      </CardHeader>
      <CardContent className="space-y-4">
        <label className="block text-sm">
          Name
          <input
            className={`${inputClass} mt-1`}
            value={name}
            onChange={(e) => {
              setName(e.target.value);
              if (!slug) setSlug(slugify(e.target.value));
            }}
            placeholder="Acme Digital"
          />
        </label>
        <label className="block text-sm">
          Slug
          <input
            className={`${inputClass} mt-1`}
            value={slug}
            onChange={(e) => setSlug(slugify(e.target.value))}
            placeholder="acme-digital"
          />
        </label>
        <label className="block text-sm">
          Description
          <textarea
            className={`${textareaClass} mt-1`}
            value={description}
            onChange={(e) => setDescription(e.target.value)}
          />
        </label>
        {mutation.isError && (
          <p className="text-sm text-destructive">
            {mutation.error instanceof Error ? mutation.error.message : "Failed to create organisation."}
          </p>
        )}
        <div className="flex gap-2">
          <Button disabled={!name || mutation.isPending} onClick={() => mutation.mutate()}>
            {mutation.isPending && <Loader2 className="mr-2 size-4 animate-spin" />}
            Create
          </Button>
          <Button variant="secondary" onClick={onCancel}>
            Cancel
          </Button>
        </div>
      </CardContent>
    </Card>
  );
}

function CreateProductForm({
  token,
  organisationId,
  onCreated,
  onCancel,
}: {
  token: string;
  organisationId: string;
  onCreated: (product: Product) => void;
  onCancel: () => void;
}) {
  const [name, setName] = useState("");
  const [slug, setSlug] = useState("");
  const [productType, setProductType] = useState<ProductType>("PDF");
  const [shortDescription, setShortDescription] = useState("");
  const [priceCents, setPriceCents] = useState("999");

  const mutation = useMutation({
    mutationFn: () =>
      createProduct(token, organisationId, {
        name,
        slug: slug || slugify(name),
        productType,
        shortDescription: shortDescription || undefined,
        priceCents: Number(priceCents) || 0,
        currency: "GBP",
      }),
    onSuccess: onCreated,
  });

  return (
    <Card>
      <CardHeader>
        <CardTitle>New product</CardTitle>
        <CardDescription>Draft products can receive versions and secure uploads.</CardDescription>
      </CardHeader>
      <CardContent className="space-y-4">
        <label className="block text-sm">
          Name
          <input
            className={`${inputClass} mt-1`}
            value={name}
            onChange={(e) => {
              setName(e.target.value);
              if (!slug) setSlug(slugify(e.target.value));
            }}
          />
        </label>
        <label className="block text-sm">
          Slug
          <input
            className={`${inputClass} mt-1`}
            value={slug}
            onChange={(e) => setSlug(slugify(e.target.value))}
          />
        </label>
        <label className="block text-sm">
          Type
          <select
            className={`${inputClass} mt-1`}
            value={productType}
            onChange={(e) => setProductType(e.target.value as ProductType)}
          >
            {productTypes.map((type) => (
              <option key={type} value={type}>
                {type.replace("_", " ")}
              </option>
            ))}
          </select>
        </label>
        <label className="block text-sm">
          Short description
          <textarea
            className={`${textareaClass} mt-1`}
            value={shortDescription}
            onChange={(e) => setShortDescription(e.target.value)}
          />
        </label>
        <label className="block text-sm">
          Price (pence)
          <input
            className={`${inputClass} mt-1`}
            type="number"
            min={0}
            value={priceCents}
            onChange={(e) => setPriceCents(e.target.value)}
          />
        </label>
        {mutation.isError && (
          <p className="text-sm text-destructive">
            {mutation.error instanceof Error ? mutation.error.message : "Failed to create product."}
          </p>
        )}
        <div className="flex gap-2">
          <Button disabled={!name || mutation.isPending} onClick={() => mutation.mutate()}>
            {mutation.isPending && <Loader2 className="mr-2 size-4 animate-spin" />}
            Create product
          </Button>
          <Button variant="secondary" onClick={onCancel}>
            Cancel
          </Button>
        </div>
      </CardContent>
    </Card>
  );
}

function ProductDetail({
  token,
  organisationId,
  product,
  onBack,
}: {
  token: string;
  organisationId: string;
  product: Product;
  onBack: () => void;
}) {
  const queryClient = useQueryClient();
  const [versionLabel, setVersionLabel] = useState("1.0.0");
  const [changelog, setChangelog] = useState("");
  const [selectedVersionId, setSelectedVersionId] = useState<string>("");
  const [selectedFile, setSelectedFile] = useState<File | null>(null);
  const [uploadStatus, setUploadStatus] = useState<string | null>(null);

  const versionsQuery = useQuery({
    queryKey: ["versions", organisationId, product.id],
    queryFn: () => listVersions(token, organisationId, product.id),
  });

  const activeVersionId = selectedVersionId || versionsQuery.data?.[0]?.id || "";

  const createVersionMutation = useMutation({
    mutationFn: () =>
      createVersion(token, organisationId, product.id, {
        versionLabel,
        changelog: changelog || undefined,
      }),
    onSuccess: (version) => {
      queryClient.invalidateQueries({ queryKey: ["versions", organisationId, product.id] });
      setSelectedVersionId(version.id);
      setChangelog("");
    },
  });

  const submitReviewMutation = useMutation({
    mutationFn: () => submitForReview(token, organisationId, product.id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["products", organisationId] });
    },
  });

  const publishMutation = useMutation({
    mutationFn: (versionId: string) => publishVersion(token, organisationId, product.id, versionId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["products", organisationId] });
      queryClient.invalidateQueries({ queryKey: ["versions", organisationId, product.id] });
    },
  });

  const uploadMutation = useMutation({
    mutationFn: async () => {
      if (!selectedFile || !activeVersionId) {
        throw new Error("Select a version and file first.");
      }
      setUploadStatus("Creating upload session…");
      const session = await createUploadSession(token, organisationId, product.id, {
        productVersionId: activeVersionId,
        fileName: selectedFile.name,
        contentType: selectedFile.type || "application/octet-stream",
      });
      setUploadStatus("Uploading to quarantine…");
      await uploadSessionFile(token, organisationId, product.id, session.id, selectedFile);
      setUploadStatus("Processing (scan & encrypt)…");
      for (let attempt = 0; attempt < 30; attempt++) {
        await new Promise((r) => setTimeout(r, 2000));
        const status = await getUploadSession(token, organisationId, product.id, session.id);
        if (status.status === "COMPLETED") {
          setUploadStatus("Upload complete — asset encrypted and stored.");
          return status;
        }
        if (status.status === "FAILED" || status.status === "EXPIRED") {
          throw new Error(`Upload ${status.status.toLowerCase()}.`);
        }
      }
      throw new Error("Processing timed out — check API workers and infrastructure.");
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["versions", organisationId, product.id] });
      queryClient.invalidateQueries({ queryKey: ["products", organisationId] });
      setSelectedFile(null);
    },
  });

  return (
    <>
      <Button variant="ghost" size="sm" className="mb-6 -ml-2" onClick={onBack}>
        <ArrowLeft className="mr-2 size-4" />
        Back to products
      </Button>

      <div className="flex flex-wrap items-start justify-between gap-4">
        <div>
          <div className="flex items-center gap-3">
            <h1 className="text-3xl font-semibold tracking-tight">{product.name}</h1>
            <Badge variant={statusVariant(product.status)}>{product.status}</Badge>
          </div>
          <p className="mt-2 text-muted-foreground">{product.shortDescription || product.slug}</p>
          <p className="mt-2 text-sm font-medium">{formatPrice(product.priceCents, product.currency)}</p>
        </div>
        <div className="flex flex-wrap gap-2">
          <Button
            variant="secondary"
            disabled={submitReviewMutation.isPending || product.status === "UNDER_REVIEW"}
            onClick={() => submitReviewMutation.mutate()}
          >
            Submit for review
          </Button>
          {activeVersionId && (
            <Button
              disabled={publishMutation.isPending}
              onClick={() => publishMutation.mutate(activeVersionId)}
            >
              Publish version
            </Button>
          )}
        </div>
      </div>

      <div className="mt-10 grid gap-6 lg:grid-cols-2">
        <Card>
          <CardHeader>
            <CardTitle>Versions</CardTitle>
            <CardDescription>Create a version before uploading assets.</CardDescription>
          </CardHeader>
          <CardContent className="space-y-4">
            {versionsQuery.data?.length ? (
              <ul className="space-y-2">
                {versionsQuery.data.map((version: ProductVersion) => (
                  <li key={version.id}>
                    <button
                      type="button"
                      className={`flex w-full items-center justify-between rounded-lg border px-3 py-2 text-left text-sm ${
                        version.id === activeVersionId ? "border-accent bg-accent/5" : "border-border"
                      }`}
                      onClick={() => setSelectedVersionId(version.id)}
                    >
                      <span>{version.versionLabel}</span>
                      <Badge variant={statusVariant(version.status)}>{version.status}</Badge>
                    </button>
                  </li>
                ))}
              </ul>
            ) : (
              <p className="text-sm text-muted-foreground">No versions yet.</p>
            )}

            <label className="block text-sm">
              New version label
              <input
                className={`${inputClass} mt-1`}
                value={versionLabel}
                onChange={(e) => setVersionLabel(e.target.value)}
              />
            </label>
            <label className="block text-sm">
              Changelog
              <textarea
                className={`${textareaClass} mt-1`}
                value={changelog}
                onChange={(e) => setChangelog(e.target.value)}
              />
            </label>
            <Button
              variant="secondary"
              disabled={!versionLabel || createVersionMutation.isPending}
              onClick={() => createVersionMutation.mutate()}
            >
              {createVersionMutation.isPending && <Loader2 className="mr-2 size-4 animate-spin" />}
              Add version
            </Button>
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle>Secure upload</CardTitle>
            <CardDescription>
              Files go to quarantine, are scanned and encrypted, then moved to protected storage.
            </CardDescription>
          </CardHeader>
          <CardContent className="space-y-4">
            {!activeVersionId ? (
              <p className="text-sm text-muted-foreground">Create a version before uploading.</p>
            ) : (
              <>
                <label className="block text-sm">
                  Asset file
                  <input
                    className="mt-2 block w-full text-sm"
                    type="file"
                    onChange={(e) => setSelectedFile(e.target.files?.[0] ?? null)}
                  />
                </label>
                {uploadStatus && <p className="text-sm text-muted-foreground">{uploadStatus}</p>}
                {uploadMutation.isError && (
                  <p className="text-sm text-destructive">
                    {uploadMutation.error instanceof Error
                      ? uploadMutation.error.message
                      : "Upload failed."}
                  </p>
                )}
                <Button
                  disabled={!selectedFile || uploadMutation.isPending}
                  onClick={() => uploadMutation.mutate()}
                >
                  {uploadMutation.isPending ? (
                    <Loader2 className="mr-2 size-4 animate-spin" />
                  ) : (
                    <Upload className="mr-2 size-4" />
                  )}
                  Upload securely
                </Button>
              </>
            )}
          </CardContent>
        </Card>
      </div>
    </>
  );
}
