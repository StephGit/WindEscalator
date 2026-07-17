---
name: deploy-functions
description: Validate and deploy the Firebase Functions service from the functions directory. Use when preparing or performing a Firebase Functions deployment.
---

# Deploy Firebase Functions

## Preconditions

- Work only in the `functions/` directory.
- Use Node 22.22.3 and pnpm 11.10.0.
- Use mise instead of nvm or other version managers.
- Do not add or upgrade dependencies as part of deployment.
- Never deploy without the user's explicit confirmation.

## Validate

1. Confirm that the intended Firebase project and Functions changes are clear.
2. Run `pnpm run lint` from `functions/`.
3. Run `pnpm run build` from `functions/`.
4. Review the working tree after the build. The build runs Prettier with `--write`; stop and ask the user how to handle any unexpected formatting changes.
5. Stop if linting or the build fails. Report the failure and do not deploy.

## Deploy

1. Summarize the validation result and the Functions that will be deployed.
2. Request explicit confirmation to deploy.
3. After confirmation, run `pnpm run deploy` from `functions/`.
4. Report the Firebase CLI deployment result, including any warnings or deployed endpoint URLs.

## Do Not

- Do not run `firebase deploy` directly from the repository root.
- Do not invoke npm directly; use pnpm scripts.
- Do not bypass lint or build failures.
- Do not deploy unrelated Firebase resources.
