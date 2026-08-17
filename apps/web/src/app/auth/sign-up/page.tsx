import Link from "next/link";

export const metadata = { title: "Create account" };

export default function SignUpPage() {
  const keycloakUrl = process.env.NEXT_PUBLIC_KEYCLOAK_URL ?? "http://localhost:8180";
  const realm = process.env.NEXT_PUBLIC_KEYCLOAK_REALM ?? "ciphermarket";
  const clientId = process.env.NEXT_PUBLIC_KEYCLOAK_CLIENT_ID ?? "ciphermarket-web";
  const redirectUri = encodeURIComponent(process.env.NEXT_PUBLIC_APP_URL ?? "http://localhost:3000");

  const registerUrl =
    `${keycloakUrl}/realms/${realm}/protocol/openid-connect/registrations` +
    `?client_id=${clientId}&response_type=code&scope=openid%20profile%20email&redirect_uri=${redirectUri}`;

  return (
    <div className="mx-auto flex max-w-md flex-col gap-6 px-4 py-20 sm:px-6">
      <h1 className="text-2xl font-semibold tracking-tight">Create account</h1>
      <p className="text-sm text-muted-foreground">
        New accounts are registered through Keycloak. Email verification is required before access
        to sensitive features.
      </p>
      <a
        href={registerUrl}
        className="inline-flex h-10 items-center justify-center rounded-lg bg-foreground px-4 text-sm font-medium text-background hover:bg-foreground/90"
      >
        Register with Keycloak
      </a>
      <p className="text-sm text-muted-foreground">
        Already have an account?{" "}
        <Link href="/auth/sign-in" className="text-accent hover:underline">
          Sign in
        </Link>
      </p>
    </div>
  );
}
