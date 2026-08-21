# Discovery & creator insights (Phase 9)

## Catalogue search

`GET /api/v1/catalogue/products` accepts:

| Param | Description |
|-------|-------------|
| `q` | Case-insensitive match on name, slug, short description |
| `categoryId` | Category filter |
| `organisationId` | Creator filter |
| `productType` | `PDF`, `SOURCE_CODE`, `DESIGN`, `GENERAL` |
| `minPriceCents` / `maxPriceCents` | Inclusive price bounds |
| `sort` | `NEWEST`, `PRICE_ASC`, `PRICE_DESC`, `NAME_ASC` |

## Creator storefront

`GET /api/v1/catalogue/creators/{slug}` returns the active organisation profile and its published products. Web route: `/creators/{slug}`.

## Sales analytics

`GET /api/v1/organisations/{id}/analytics/sales` (org members with at least finance/product rank) returns paid-order totals and per-product breakdown. Refunded orders are excluded.

## Suspicious activity reports

Authenticated buyers can `POST /api/v1/reports/suspicious` with category `LEAK|FRAUD|ABUSE|MALWARE|OTHER`. This writes a high-severity `BUYER_SUSPICIOUS_REPORT` security event for operators — it does not auto-suspend products or users.
