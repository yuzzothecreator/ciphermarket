# Security Limitations

CipherMarket documents security capabilities and limitations honestly.

## Content protection

- **Not absolute:** Once a buyer receives readable content (PDF text, source code, design files), they can copy it. The platform reduces casual sharing and helps trace leaks; it does not make piracy impossible.
- **PDF viewing:** Browser-based viewers cannot fully prevent screenshots, printing, or OS-level capture.
- **Source code:** Downloaded archives can be copied and modified. Signed manifests verify integrity at delivery time only.
- **Forensic watermarks:** No "invisible forensic watermark" is advertised unless a tested implementation exists.

## Cryptography

- Envelope encryption uses established libraries (Google Tink Streaming AEAD via Vault Transit wrapping).
- Local development uses Vault in dev mode — **unsuitable for production**.
- Encryption keys never reach the browser.

## Confidential disclosure

- Document hashing and disclosure records provide evidence of existence and history; they do not automatically create copyright, patent protection, or legally binding NDAs.
- Confidential disclosure terms require explicit acceptance; legal review is the creator's responsibility.
- Recipients cannot download until terms are accepted, and creators may revoke future access.

## Legal

- Document hashing and disclosure records provide evidence of existence and history; they do not automatically create copyright, patent protection, or legally binding NDAs.

## Identity

- MFA enforcement for sellers and administrators is configured in Keycloak; operational verification is required at deployment.

## Payments

- Frontend payment redirects are never treated as proof of payment.
- Entitlements are granted only after verified server-to-server webhooks (Phase 3).
