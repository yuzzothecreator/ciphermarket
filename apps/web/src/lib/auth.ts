const TOKEN_KEY = "ciphermarket.access_token";
const TOKEN_EXPIRY_KEY = "ciphermarket.token_expires_at";
const PKCE_VERIFIER_KEY = "ciphermarket.pkce_verifier";

export function getKeycloakConfig() {
  return {
    url: process.env.NEXT_PUBLIC_KEYCLOAK_URL ?? "http://localhost:8180",
    realm: process.env.NEXT_PUBLIC_KEYCLOAK_REALM ?? "ciphermarket",
    clientId: process.env.NEXT_PUBLIC_KEYCLOAK_CLIENT_ID ?? "ciphermarket-web",
    appUrl: process.env.NEXT_PUBLIC_APP_URL ?? "http://localhost:3000",
  };
}

function base64UrlEncode(bytes: Uint8Array): string {
  let binary = "";
  for (const byte of bytes) {
    binary += String.fromCharCode(byte);
  }
  return btoa(binary).replace(/\+/g, "-").replace(/\//g, "_").replace(/=+$/, "");
}

export async function createPkcePair(): Promise<{ verifier: string; challenge: string }> {
  const verifierBytes = crypto.getRandomValues(new Uint8Array(32));
  const verifier = base64UrlEncode(verifierBytes);
  const digest = await crypto.subtle.digest("SHA-256", new TextEncoder().encode(verifier));
  const challenge = base64UrlEncode(new Uint8Array(digest));
  return { verifier, challenge };
}

export function storePkceVerifier(verifier: string): void {
  sessionStorage.setItem(PKCE_VERIFIER_KEY, verifier);
}

export function consumePkceVerifier(): string | null {
  const verifier = sessionStorage.getItem(PKCE_VERIFIER_KEY);
  sessionStorage.removeItem(PKCE_VERIFIER_KEY);
  return verifier;
}

export function storeAccessToken(token: string, expiresInSeconds: number): void {
  sessionStorage.setItem(TOKEN_KEY, token);
  sessionStorage.setItem(TOKEN_EXPIRY_KEY, String(Date.now() + expiresInSeconds * 1000));
}

export function getAccessToken(): string | null {
  if (typeof window === "undefined") {
    return null;
  }
  const token = sessionStorage.getItem(TOKEN_KEY);
  const expiresAt = sessionStorage.getItem(TOKEN_EXPIRY_KEY);
  if (!token || !expiresAt) {
    return null;
  }
  if (Date.now() >= Number(expiresAt)) {
    clearAccessToken();
    return null;
  }
  return token;
}

export function parseJwtRoles(token: string | null): string[] {
  if (!token) {
    return [];
  }
  try {
    const payloadPart = token.split(".")[1];
    const json = atob(payloadPart.replace(/-/g, "+").replace(/_/g, "/"));
    const payload = JSON.parse(json) as { realm_access?: { roles?: string[] } };
    return payload.realm_access?.roles ?? [];
  } catch {
    return [];
  }
}

export function clearAccessToken(): void {
  sessionStorage.removeItem(TOKEN_KEY);
  sessionStorage.removeItem(TOKEN_EXPIRY_KEY);
}

export async function buildSignInUrl(returnTo = "/creator"): Promise<string> {
  const { url, realm, clientId, appUrl } = getKeycloakConfig();
  const redirectUri = `${appUrl}/auth/callback`;
  const { verifier, challenge } = await createPkcePair();
  storePkceVerifier(verifier);
  sessionStorage.setItem("ciphermarket.auth_return_to", returnTo);

  const params = new URLSearchParams({
    client_id: clientId,
    response_type: "code",
    scope: "openid profile email",
    redirect_uri: redirectUri,
    code_challenge: challenge,
    code_challenge_method: "S256",
  });

  return `${url}/realms/${realm}/protocol/openid-connect/auth?${params.toString()}`;
}

export async function exchangeAuthorizationCode(code: string): Promise<void> {
  const { url, realm, clientId, appUrl } = getKeycloakConfig();
  const verifier = consumePkceVerifier();
  if (!verifier) {
    throw new Error("Missing PKCE verifier — sign in again.");
  }

  const body = new URLSearchParams({
    grant_type: "authorization_code",
    client_id: clientId,
    code,
    redirect_uri: `${appUrl}/auth/callback`,
    code_verifier: verifier,
  });

  const response = await fetch(`${url}/realms/${realm}/protocol/openid-connect/token`, {
    method: "POST",
    headers: { "Content-Type": "application/x-www-form-urlencoded" },
    body,
  });

  if (!response.ok) {
    throw new Error(`Token exchange failed (${response.status})`);
  }

  const payload = (await response.json()) as { access_token: string; expires_in: number };
  storeAccessToken(payload.access_token, payload.expires_in);
}

export function consumeAuthReturnTo(): string {
  const returnTo = sessionStorage.getItem("ciphermarket.auth_return_to") ?? "/creator";
  sessionStorage.removeItem("ciphermarket.auth_return_to");
  return returnTo;
}

export function buildSignOutUrl(): string {
  const { url, realm, clientId, appUrl } = getKeycloakConfig();
  clearAccessToken();
  const params = new URLSearchParams({
    client_id: clientId,
    post_logout_redirect_uri: appUrl,
  });
  return `${url}/realms/${realm}/protocol/openid-connect/logout?${params.toString()}`;
}
