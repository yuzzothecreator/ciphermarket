export const metadata = { title: "Sign in" };

export default function SignInPage() {
  const keycloakUrl = process.env.NEXT_PUBLIC_KEYCLOAK_URL ?? "http://localhost:8180";
  const realm = process.env.NEXT_PUBLIC_KEYCLOAK_REALM ?? "ciphermarket";
  const clientId = process.env.NEXT_PUBLIC_KEYCLOAK_CLIENT_ID ?? "ciphermarket-web";
  const redirectUri = encodeURIComponent(process.env.NEXT_PUBLIC_APP_URL ?? "http://localhost:3000");

  const authUrl =
    `${keycloakUrl}/realms/${realm}/protocol/openid-connect/auth` +
    `?client_id=${clientId}&response_type=code&scope=openid%20profile%20email&redirect_uri=${redirectUri}`;

  return (
    <div className="mx-auto flex max-w-md flex-col gap-6 px-4 py-20 sm:px-6">
      <h1 className="text-2xl font-semibold tracking-tight">Sign in</h1>
      <p className="text-sm text-muted-foreground">
        Authentication is handled by Keycloak with OpenID Connect. Email verification and MFA for
        sellers and administrators are enforced at the identity provider.
      </p>
      <a
        href={authUrl}
        className="inline-flex h-10 items-center justify-center rounded-lg bg-foreground px-4 text-sm font-medium text-background hover:bg-foreground/90"
      >
        Continue to Keycloak
      </a>
    </div>
  );
}
