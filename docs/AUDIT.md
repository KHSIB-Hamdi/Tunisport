# Tunisport — Repository Audit

Audit date: 2026-09-06. Every finding below was verified against the files in this repository. Nothing here is inferred from prior documentation.

This document records what is broken, ambiguous or risky, and is deliberately more detailed than the [README](../README.md). It is a snapshot of known state, not a task list that has been executed — most items are **documented, not fixed**, because fixing them would require product decisions or would change application behavior.

---

## 1. Architecture

### Components

| Component | Path | Responsibility |
| --- | --- | --- |
| Symfony 5.4 web application | `Symfony/` | Front office (public Twig pages), back office (admin Twig pages), and JSON endpoints for a mobile client. Owns the Doctrine schema. |
| JavaFX desktop admin client | `Java/` | Native admin view over match data. NetBeans/Ant project, source level 1.8. |
| MySQL database `tunisport` | external | The sole integration point between the two applications. |

### Communication

- Browser → Symfony over HTTP; server-rendered Twig.
- Mobile client → Symfony `*MobileController` actions returning `JsonResponse` built with the Symfony Serializer (`BlogMobileController`, `HebergementmobileController`). There is no content negotiation and no OpenAPI/Swagger definition — the mobile API surface is only discoverable by reading those two controllers.
- Symfony → MySQL through Doctrine ORM.
- JavaFX client → the **same** MySQL database directly over JDBC (`Java/src/Utils/DataBase.java`). It never calls the Symfony application.

Consequence: the two applications are coupled through table and column names with no shared definition and no automated check. `Java/src/Services/MatchFCRUD.java` hardcodes `match_f (id, equipe_a_id, equipe_b_id, date_match, type_match_id, stade_id, tournoi_id, resultat_a, resultat_b, prix, image, image2)`, which is Doctrine's naming for `App\Entity\MatchF`. Any Doctrine migration that renames a column silently breaks the desktop client.

### External services

| Service | Where | Credentials from |
| --- | --- | --- |
| Stripe | `src/Service/StripeService.php`, `src/Entity/Traits/StripeTrait.php` | `.env` (`STRIPE_*`) |
| Google OAuth | `config/packages/security.yaml`, `src/Controller/GoogleController.php` | `.env` (`GOOGLE_*`) |
| Facebook OAuth | `config/packages/security.yaml` | `.env` (`OAUTH_FACEBOOK_*`) |
| SMTP mail | Symfony Mailer | `.env` (`MAILER_DSN`) |
| RapidAPI text/sentiment analysis | `src/Controller/SentimentController.php` | **hardcoded in source** |
| Giphy | `src/Controller/ChatBotController.php` | **hardcoded in source** |
| BotMan web chatbot | `src/Controller/ChatBotController.php`, `templates/front.html.twig` | n/a |

### Intended vs. actual layering

The intended pattern is a thin slice: `App\Manager\ReservationManager` plus `App\Service\StripeService`. That is the only place business logic is factored out. Most controllers are fat — they inject repositories directly and write to the entity manager inline. For example `FrontController::front` mutates and persists `Equipe` rows during a plain GET page render, which is a side effect on a read request. New payment and reservation logic belongs in the manager/service pair.

---

## 2. Blocking problems

### 2.1 Merge conflicts (RESOLVED during this audit)

Eight files carried literal `<<<<<<< Updated upstream` / `>>>>>>> Stashed changes` markers from an unresolved `git stash pop`. `config/packages/security.yaml` was therefore invalid YAML and `.env` invalid dotenv, so the application could not boot or even parse.

All eight were resolved toward the **"Stashed changes"** side, which is the feature-complete one the rest of the code expects (paginator, 2FA, OAuth, Stripe, TCPDF, BotMan, `app_user_provider`, role-based `access_control`). The "Updated upstream" side was close to a bare Symfony skeleton; resolving toward it would have broken most controllers.

| File | Conflict blocks |
| --- | --- |
| `Symfony/.env` | 1 |
| `Symfony/composer.json` | 3 |
| `Symfony/composer.lock` | 14 |
| `Symfony/symfony.lock` | 1 |
| `Symfony/config/bundles.php` | 1 |
| `Symfony/config/packages/security.yaml` | 2 |
| `Symfony/src/Entity/User.php` | 4 |
| `Symfony/templates/front.html.twig` | 3 |

Two follow-ups from that resolution:

- **`composer.lock` was resolved, not merged.** A hand-resolved lock file is not trustworthy. It is now valid JSON, but it should be regenerated with `composer update` before being relied on.
- **`MESSENGER_TRANSPORT_DSN` was restored.** The "Stashed" side of `.env` had no active value for it, but `config/packages/messenger.yaml` requires it, so resolving faithfully would have introduced a new boot failure. It was set to the Flex default `doctrine://default?auto_setup=0`, consistent with the `failed: 'doctrine://default?queue_name=failed'` transport already configured.

### 2.2 Referenced classes that do not exist — 24 (NOT FIXED)

`src/` defines 83 classes. The following 24 are referenced but absent, so the application still cannot boot. Line numbers are the first reference.

**Security — named in `config/packages/security.yaml`, so these break boot immediately**

| Class | Referenced at |
| --- | --- |
| `App\Security\GoogleAuthenticator` | `config/packages/security.yaml:21` |
| `App\Security\FacebookAuthenticator` | `config/packages/security.yaml:22` |
| `App\Security\UserChecker` | `config/packages/security.yaml:31` |
| `App\Security\LoginSecurityAuthenticator` | `src/Controller/GoogleController.php:9` |

Only `App\Security\LoginAuthenticator` actually exists.

**Doctrine repositories — named in `#[ORM\Entity(repositoryClass: ...)]` attributes, so these break the ORM mapping**

| Class | Referenced at |
| --- | --- |
| `App\Repository\CategoryHebergementRepository` | `src/Entity/CategoryHebergement.php:5`, `src/Controller/CategoryHebergementController.php:4` |
| `App\Repository\CategoryTransportRepository` | `src/Entity/CategoryTransport.php:5`, `src/Controller/CategoryTransportController.php:4` |
| `App\Repository\LocalisationRepository` | `src/Entity/Localisation.php:5`, `src/Controller/LocalisationController.php:7` |
| `App\Repository\ReclamationRepository` | `src/Entity/Reclamation.php:5` |
| `App\Repository\ReponseRepository` | `src/Entity/Reponse.php:5` |
| `App\Repository\CategoryRepository` | `src/Controller/HebergementController.php:21`, `src/Controller/TransportController.php:26` |
| `App\Repository\ProductRepository` | `src/Controller/HebergementController.php:20` |
| `App\Repository\UtilisateurRepository` | `src/Controller/GoogleController.php:8` |

**Form types**

| Class | Referenced at |
| --- | --- |
| `App\Form\UserType` | `src/Controller/ClientController.php:23`, `src/Controller/RegistrationaaaController.php:8`, `src/Form/ReservationType.php:9` |
| `App\Form\AdminType` | `src/Controller/AdminController.php:14` |
| `App\Form\MatchFilterType` | `src/Controller/MatchFController.php:14` |
| `App\Form\ForgotPaswordType` | `src/Controller/ClientController.php:36` |
| `App\Form\ResetPassType` | `src/Controller/ClientController.php:38` |
| `App\Form\FbGoogleType` | `src/Controller/GoogleController.php:6` |
| `App\Form\UtilisateurType` | `src/Controller/GoogleController.php:7` |
| `App\Form\Entitytype` | `src/Form/CategoryTransportType.php:5` |

**Other**

| Class | Referenced at |
| --- | --- |
| `App\Entity\Utilisateur` | `src/Controller/GoogleController.php:5` |
| `App\Service\Mailer` | `src/Controller/ClientController.php:39`, `src/Controller/RegistrationaaaController.php:26` |
| `App\ChatBot\Conversation\OnBoardingConversation` | `src/Controller/ChatBotController.php:4` |
| `App\ChatBot\Conversation\QuestionConversation` | `src/Controller/ChatBotController.php:5` |

A 25th reference, `App\Controller\DefaultController` in `config/routes.yaml:3`, is harmless — the whole file is commented out.

Note the `Utilisateur` cluster (`App\Entity\Utilisateur`, `UtilisateurType`, `UtilisateurRepository`, `LoginSecurityAuthenticator`, `FbGoogleType`), all referenced only by `GoogleController`. This looks like an abandoned parallel user model that was superseded by `App\Entity\User`. See ambiguity A2.

### 2.3 Enabled bundles with no package and no configuration (NOT FIXED)

`config/bundles.php` enables both of these, but neither appears in `composer.json`:

- `Vich\UploaderBundle\VichUploaderBundle` — no `vich/uploader-bundle` requirement, no `config/packages/vich_uploader.yaml`
- `Doctrine\Bundle\FixturesBundle\DoctrineFixturesBundle` — no `doctrine/doctrine-fixtures-bundle` requirement, no `src/DataFixtures/`

Each is a fatal error at kernel boot. Either add the package or remove the line.

Conversely, `knpuniversity/oauth2-client-bundle` and `mercuryseries/flashy-bundle` are required and enabled but have no configuration file under `config/packages/`; the OAuth bundle in particular needs a `knpu_oauth2_client.yaml` defining the Google and Facebook clients before OAuth login can work.

### 2.4 Undefined security provider (NOT FIXED — see ambiguity A1)

`config/packages/security.yaml` sets `provider: app_user_provider` on the `main` firewall and `user_checker: App\Security\UserChecker`, but the `providers` section defines only `users_in_memory: { memory: null }`. `app_user_provider` is never defined, which is a configuration error.

The same file also contains an empty `guard:` key (a leftover from the pre-authenticator-manager Symfony security system, which is disabled here by `enable_authenticator_manager: true`), and two `access_control` entries use the key `role:` instead of the valid `roles:`.

### 2.5 Java project is not buildable on any other machine (NOT FIXED)

`Java/nbproject/project.properties` contains:

```
file.reference.fontawesomefx-8.5.jar=C:\Users\ASUS\Downloads\jar_files (2)\fontawesomefx-8.5.jar
```

— an absolute path on a different developer's machine. It also depends on `${libs.MySQLDriver.classpath}`, a NetBeans *global* library that is not part of the project. No `lib/` directory and no JARs are committed, so there is no way to resolve either dependency from the repository alone. `application.vendor=ASUS` is a related leftover.

---

## 3. Configuration problems

| Problem | Location | Impact |
| --- | --- | --- |
| **Postgres vs MySQL contradiction** | `Symfony/docker-compose.yml` + `.override.yml` provision PostgreSQL 15; `DATABASE_URL` and the Java client both use MySQL | Anyone who runs `docker compose up` expecting the app database gets the wrong engine. The compose files are the untouched Flex skeleton and are unused. |
| **`VAR_DUMPER_SERVER` undefined** | required by `config/packages/debug.yaml:5`, absent from `.env` | Boot failure in the `dev` environment. Added to `.env.example`; **left out of `.env`** to avoid changing current behavior. |
| **Migrations are far behind the entities** | `Symfony/migrations/` — 5 files, all dated 2023-03-08/09, against 20 entities | `doctrine:migrations:migrate` alone will not produce a working schema. Use `doctrine:schema:validate` as the source of truth. |
| **Stripe environment switch** | `src/Service/StripeService.php:15-19` | Selects `STRIPE_*_LIVE` whenever `APP_ENV !== 'dev'`, and those variables are empty. Any non-dev environment fails at the first payment. |
| **Bogus PSR-4 autoload entry** (FIXED) | `composer.json` mapped `BotMan\Drivers\Web\` to `src/` | Composer would have looked for vendor classes in application code. Removed; `App\` → `src/` is the only mapping now. |
| **No API definition** | — | The mobile JSON contract exists only as code in two controllers. |

---

## 4. Security findings

No secret values are reproduced here. Locations only.

### 4.1 Committed credentials — REQUIRES OWNER ACTION

`Symfony/.env` is present in the working copy and contains **real, non-placeholder values** for:

- `APP_SECRET` (32 hex characters)
- `DATABASE_URL` (local MySQL DSN including credentials)
- `GOOGLE_CLIENT_ID`, `GOOGLE_CLIENT_SECRET`
- `OAUTH_FACEBOOK_ID`, `OAUTH_FACEBOOK_SECRET`
- `MAILER_DSN` (SMTP DSN including username and password)
- `STRIPE_PUBLIC_KEY_TEST`, `STRIPE_SECRET_KEY_TEST`

`STRIPE_*_LIVE` are empty, so no live payment credentials are exposed.

Actions taken during this audit: `/.env` was added to `Symfony/.gitignore` and to the root `.gitignore`, and `Symfony/.env.example` was created with placeholder values only. **The file itself was left untouched on disk at the owner's instruction, so the local setup keeps working.**

Actions still required by the repository owner — these are deliberately **not** performed here:

1. Rotate the Google OAuth client secret in the Google Cloud console.
2. Rotate the Facebook app secret in the Meta developer console.
3. Rotate the SMTP mailbox password.
4. Roll the Stripe **test** secret key from the Stripe dashboard.
5. Regenerate `APP_SECRET` (invalidates existing `remember_me` cookies and signed URLs).
6. Change the MySQL password if the DSN reflects a real, non-local account.

**These values have been publicly exposed on GitHub.** The working copy audited here had no `.git` directory, but a repository already existed at `https://github.com/KHSIB-Hamdi/Tunisport.git` containing this snapshot — `Symfony/.env` was tracked there and publicly readable. Rotation is therefore **urgent, not precautionary**.

Republishing a clean history does **not** undo the exposure: GitHub can retain unreferenced commits after a force-push, and the repository may already have been cloned, forked or scraped by automated secret crawlers. Treat every value listed above as compromised and rotate it at the provider, in the order given. Rotating is the only action that actually revokes access; the Git cleanup is secondary.

### 4.2 Hardcoded API keys in source — REQUIRES OWNER ACTION

| File | Kind |
| --- | --- |
| `Symfony/src/Controller/ChatBotController.php:154` | Giphy API key, embedded in a URL string |
| `Symfony/src/Controller/SentimentController.php` | RapidAPI key and host, embedded in the request headers |

Both should be moved to environment variables. They were **left in place** because removing them changes application behavior, which is outside the scope of a documentation and organization pass. Both keys should be considered compromised and rotated.

### 4.3 Hardcoded database credentials — Java client

`Java/src/Utils/DataBase.java:20-22` hardcodes the JDBC URL, the username `root`, and a password literal (empty). There is no external configuration mechanism for the desktop client at all. This is a local development convenience, but it means the client cannot point at another database without recompiling, and it commits a root-user connection pattern into source.

### 4.4 SQL injection in the Java client

`Java/src/Services/MatchFCRUD.java` builds SQL by string concatenation of entity field values rather than using `PreparedStatement`. Beyond the injection risk this breaks on any value containing a quote, and on date formatting. The fields are already declared on the class, so converting to prepared statements is mechanical. **Not fixed** — it is application logic, outside this pass.

### 4.5 Leaked local paths

- `Java/nbproject/private/private.properties` references `C:\Users\ASUS\AppData\Roaming\NetBeans\8.2\build.properties` — another developer's machine layout. `nbproject/private/` is now git-ignored.
- `Java/nbproject/project.properties` leaks the same user's Downloads folder (section 2.5). This one is **not** in a private directory and is required for the build, so it remains visible.

---

## 5. Repository hygiene

### Files that should not be under version control

All are now covered by `.gitignore`. At the owner's instruction **nothing was deleted from disk** — these remain locally and are simply excluded from commits.

| Path | Why | Recoverable? |
| --- | --- | --- |
| `Symfony/node_modules/` (~7.6 MB) | No `package.json` exists at the Symfony root and there is no build step | **No** — `npm install` cannot restore it. Delete only when you are certain nothing references a path inside it. |
| `Java/build/` | 24 `.class` files plus copied resources | Yes — `ant clean jar` |
| `Java/nbproject/private/` | Per-developer NetBeans state, leaks local paths | Yes — regenerated by NetBeans |
| `Symfony/.env` | Real credentials (section 4.1) | n/a — keep locally, never commit |

### Dead, orphaned and duplicate files

| Path | Finding |
| --- | --- |
| `Java/build/classes/GUI/FirstWindow$1.class` | Orphan — no `GUI` package exists in `src/`. Stale artifact from a renamed package. |
| `Java/build/classes/Services/ServiceMatchF.rs` | Orphan NetBeans resource marker referencing `services.ServiceMatchF`, a class that does not exist. |
| `Java/src/tunisport/Tunisport.java` | The declared main class, but its `main` is a scratch driver: it inserts an empty `MatchF` row and prints, with commented-out blocks. |
| `Java/src/Tests/FirstWindow.java` | A standalone JavaFX `Application` with its own `main`, despite living in a `Tests` package. Not a test. |
| `Java/src/Controlleurs/MatchController.java`, `UIController.java` | Empty stubs — only `initialize`. |
| `Java/src/Views/getData.java` | Three public static mutable `String` fields used as global state. Header comment and `@author MarcoMan` are copy-pasted from a video tutorial. |
| `Java/src/Controlleurs/DashboardController.java` | 632-line god class holding all combo population, table binding and CRUD wiring. |
| `Symfony/templates/*.html` (`index.html`, `banner.html`, `shopcart.html`, `tables-data.html`, `forms-layouts.html`) | Raw theme files left over from the purchased template; not Twig, not rendered. |
| `Symfony/templates/base1.html.twig`, `frontMatch/frontTest/frontUser.html.twig` | Multiple near-duplicate layout variants; unclear which are live. |
| `tunisport.png` | Duplicated at the repo root, `Java/src/Images/`, and `Java/build/classes/Images/`. |
| All Java source files | Still carry the default NetBeans "To change this license header..." template comment; `@author` tags read `House` and `MarcoMan`. |

Three separate `main`/entry points exist in the Java project (`Tunisport`, `FirstWindow`, plus the JavaFX launch path), which makes the actual entry point ambiguous to a newcomer. `build.xml` declares `tunisport.Tunisport`.

### Naming inconsistencies

- Package names mix French (`Entités`, `Controlleurs` — also a misspelling of `Contrôleurs`) with English (`Services`, `Interfaces`, `Utils`), and `Entités` contains a non-ASCII character in a path, which is fragile across toolchains.
- Symfony controllers mix casing conventions: `HebergementmobileController` vs `BlogMobileController`; `RegistrationaaaController` is plainly a placeholder name.
- Routing uses both PHP 8 `#[Route(...)]` attributes and legacy `/** @Route(...) */` docblocks, sometimes in the same class. Match whichever style the surrounding class uses.
- Domain vocabulary is mixed French/English throughout (`Reclamation`, `Reponse`, `Billet`, `Hebergement` alongside `Event`, `Blog`).

### Testing

`Symfony/tests/` contains only `bootstrap.php` — **zero test cases**, despite PHPUnit 9.5 being configured with `phpunit.xml.dist` and dev dependencies installed. `ant test` exists as a NetBeans-generated target with no JUnit tests behind it. There is no linter, formatter, or static analysis configured for either project.

---

## 6. Ambiguities

Classified as `RESOLVED FROM CODE` (determined and acted on), `SAFE TO IMPROVE` (low-risk, actioned or clearly actionable), or `REQUIRES USER DECISION` (a genuine product or architecture choice — current behavior preserved).

### A1 — Which `User` property is the login identifier? · REQUIRES USER DECISION

**What is ambiguous.** Defining the missing `app_user_provider` requires naming the entity property used to look up users. The evidence conflicts: `User::$username` is the column declared `unique: true` (length 180), while `User::$email` is nullable and has no uniqueness constraint. But `LoginAuthenticator` reads the login form's `email` field and passes it straight into the `UserBadge`, and stores it as `Security::LAST_USERNAME`.

**Why it matters.** Choosing `email` makes the provider query a nullable, non-unique column — Doctrine will throw on duplicates or silently match the wrong account. Choosing `username` means users authenticate with their username typed into a field labelled "email".

**What the repository currently does.** Nothing works: `app_user_provider` is referenced but undefined, so login fails at configuration load.

**Decision required.** Whether the login identifier is the email address or the username. If email, `User::$email` also needs `unique: true`, `nullable: false`, and a migration.

### A2 — `User` or `Utilisateur`? · REQUIRES USER DECISION

**What is ambiguous.** `GoogleController` references an entire parallel user stack (`App\Entity\Utilisateur`, `UtilisateurType`, `UtilisateurRepository`, `FbGoogleType`, `LoginSecurityAuthenticator`), none of which exists. Everything else in the codebase uses `App\Entity\User`.

**Why it matters.** It determines whether `GoogleController` is dead code to be deleted or a half-finished migration to be completed. Eight of the 24 missing classes belong to this cluster.

**What the repository currently does.** `GoogleController` cannot be loaded.

**Decision required.** Delete `GoogleController` and consolidate on `User`, or restore the `Utilisateur` model. The rest of the code strongly suggests the former, but deleting a controller is a functionality change and was not done here.

### A3 — Is two-factor authentication meant to be active? · REQUIRES USER DECISION

**What is ambiguous.** `User.php` imports `TotpConfiguration`, `TotpConfigurationInterface` and `TwoFactorInterface`, and `config/packages/scheb_2fa.yaml` exists, and `access_control` has a `^/2fa` rule. But `User` does **not** implement `TwoFactorInterface`, and the `main` firewall has no `two_factor:` configuration.

**Why it matters.** The `^/2fa` access-control rule guards a flow that cannot currently be entered.

**What the repository currently does.** 2FA is inert — the imports are unused.

**Decision required.** Finish the integration or remove the scaffolding.

### A4 — Should the two applications keep sharing one database directly? · REQUIRES USER DECISION

**What is ambiguous.** The JavaFX client bypasses the Symfony application entirely and writes to its tables over JDBC. This is an architectural choice with no documented rationale.

**Why it matters.** It duplicates validation and business rules, bypasses `ReservationManager`, and makes every Doctrine migration a potential silent breakage of the desktop client.

**What the repository currently does.** Direct shared-database access, kept in sync manually.

**Decision required.** Keep the shared database (and add a schema-drift check), or move the desktop client onto the mobile JSON endpoints.

### A5 — Which Twig layout is canonical? · REQUIRES USER DECISION

**What is ambiguous.** `base.html.twig`, `base1.html.twig`, `front.html.twig`, `frontMatch.html.twig`, `frontTest.html.twig`, `frontUser.html.twig` and `back.html.twig` coexist with overlapping responsibilities, alongside raw non-Twig theme `.html` files.

**Why it matters.** A developer cannot tell which layout to extend for a new page.

**What the repository currently does.** All are present; usage varies by controller.

**Decision required.** Which layouts are live, and which are theme leftovers that can be deleted.

### A6 — Conflict resolution direction · RESOLVED FROM CODE

The "Stashed changes" side was chosen because the rest of the codebase depends on what it provides — the paginator, 2FA, OAuth, Stripe, TCPDF and BotMan packages, `app_user_provider`, and the role-based `access_control` rules. The "Updated upstream" side was near-skeleton and would have broken most controllers. Confirmed with the repository owner before applying.

### A7 — `MESSENGER_TRANSPORT_DSN` value · SAFE TO IMPROVE (actioned)

The Doctrine transport (`doctrine://default?auto_setup=0`) was chosen because it requires no extra infrastructure and matches the already-configured `failed: 'doctrine://default?queue_name=failed'` transport. Switching to AMQP or Redis is a deployment decision; both are present as commented alternatives in `.env`.

### A8 — `VAR_DUMPER_SERVER` in `.env` · SAFE TO IMPROVE (documented, not actioned)

Required by `debug.yaml` but absent from `.env`. It was added to `.env.example` but deliberately **not** to `.env`, to keep changes to that file limited to undoing a regression introduced by the conflict resolution.

### A9 — `composer.lock` trustworthiness · SAFE TO IMPROVE (documented, not actioned)

The lock file was resolved from 14 conflict blocks and is valid JSON, but a hand-resolved lock cannot be assumed consistent with `composer.json`. Regenerating it requires PHP and Composer, which were not available in the audit environment. Run `composer update` and commit the result.

### A10 — Deployment target · REQUIRES USER DECISION

There is no Dockerfile, no CI/CD, no infrastructure-as-code and no deployment script. The only container configuration is the unused Flex Postgres skeleton. No deployment process was documented because none exists in the repository, and inventing one would be guesswork.

---

## 7. What was deliberately not changed

- **No file or directory was moved or renamed.** The Symfony layout is framework-mandated and the NetBeans layout is `nbproject`-mandated; moving anything would break autoloading, `build-impl.xml` or Twig paths for purely cosmetic gain.
- **No files were deleted** — including `node_modules/`, `Java/build/` and `nbproject/private/`, per the owner's instruction. They are git-ignored instead.
- **No missing classes were written.** Supplying them requires the product decisions in A1-A3.
- **No credentials were rotated or revoked**, and no secret values were removed from `.env`.
- **No business logic was refactored** — including the SQL-injection-prone `MatchFCRUD`, the fat controllers, and the persisting GET handler in `FrontController::front`.
- **No LICENSE, CONTRIBUTING, CODE_OF_CONDUCT, SECURITY policy, CI workflow or issue templates were added.** The project has no contributors, no release process and no public consumers today, so these would be decoration rather than infrastructure. Add them when the repository is actually opened up.

---

## 8. Recommended next steps, in order

1. Rotate the credentials listed in sections 4.1 and 4.2.
2. Decide A1 and A2; that unblocks roughly half of the missing classes.
3. Add or remove `vich/uploader-bundle` and `doctrine/doctrine-fixtures-bundle` so `bundles.php` and `composer.json` agree.
4. Run `composer update` and commit a trustworthy `composer.lock`.
5. Reconcile the schema: `doctrine:schema:validate`, then generate a catch-up migration.
6. Fix the FontAwesomeFX path in `Java/nbproject/project.properties` — ideally by committing a `Java/lib/` directory and using a relative reference.
7. Move the Giphy and RapidAPI keys into environment variables.
8. Delete `Symfony/docker-compose.yml` and `.override.yml`, or replace them with a MySQL definition that matches the application.
9. Write the first real test. The harness is configured and unused.
10. Convert `MatchFCRUD` to `PreparedStatement`.
