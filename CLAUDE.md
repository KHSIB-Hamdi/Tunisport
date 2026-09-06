# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Repository shape

Tunisport is a match-reservation application split into two independent, unrelated-toolchain projects that share **one MySQL database named `tunisport`**:

- [Symfony/](Symfony/) — Symfony 5.4 web app (PHP >= 7.2.5), also serves JSON endpoints for the mobile client.
- [Java/](Java/) — NetBeans/Ant JavaFX desktop admin client (source level 1.8), talking to the same schema over raw JDBC.

There is no git repository here (no `.git`), so no history to consult.

## CRITICAL: unresolved merge conflicts

Eight files still contain literal `<<<<<<< Updated upstream` / `>>>>>>> Stashed changes` markers, so **the Symfony app cannot currently boot or even `composer install`**:

`Symfony/.env`, `Symfony/composer.json`, `Symfony/composer.lock`, `Symfony/symfony.lock`, `Symfony/config/bundles.php`, `Symfony/config/packages/security.yaml`, `Symfony/src/Entity/User.php`, `Symfony/templates/front.html.twig`

Before doing anything else in the Symfony project, check the file you are editing for markers (`grep -rn '^<<<<<<< ' Symfony --exclude-dir=vendor`). The "Stashed changes" side is the feature-complete one — it is what the rest of the code expects (paginator, 2FA, OAuth, Stripe, TCPDF, BotMan, `app_user_provider`, role-based `access_control`). The "Updated upstream" side is close to a bare Symfony skeleton. Resolving toward upstream will break most controllers.

Two further gaps in the "Stashed changes" side, which must be fixed as part of any resolution:

- `config/packages/security.yaml` registers `App\Security\GoogleAuthenticator`, `App\Security\FacebookAuthenticator` and `App\Security\UserChecker`; only `App\Security\LoginAuthenticator` exists in [Symfony/src/Security/](Symfony/src/Security/).
- `config/bundles.php` enables `VichUploaderBundle` and `DoctrineFixturesBundle`, but neither is in `composer.json`. `MatchFilterType` is imported by `MatchFController` and does not exist either.

## Commands

Symfony (run from `Symfony/`; `vendor/` is not committed):

```bash
composer install
php bin/console cache:clear
symfony server:start            # or: php -S 127.0.0.1:8000 -t public
php bin/console doctrine:migrations:migrate
php bin/console make:migration
php bin/console debug:router     # routes come from annotations/attributes only
php bin/console doctrine:schema:validate
```

Tests use PHPUnit 9.5 via the Symfony bridge. `Symfony/tests/` currently holds only `bootstrap.php` — there are no test cases yet.

```bash
php bin/phpunit                                   # whole suite
php bin/phpunit tests/Path/To/SomeTest.php        # one file
php bin/phpunit --filter testMethodName           # one test
```

Java (run from `Java/`):

```bash
ant clean jar        # builds dist/tunisport.jar
ant run              # main class tunisport.Tunisport
ant test
```

`Java/nbproject/build-impl.xml` is NetBeans-generated — put custom Ant targets in the empty `-pre-*`/`-post-*` hooks in `build.xml`, never in `build-impl.xml`.

## Symfony architecture notes

- **Persistence**: Doctrine ORM, `DATABASE_URL` points at local MySQL `root` with no password (`docker-compose.yml` is the untouched Postgres skeleton and does not match — ignore it). Migrations in [Symfony/migrations/](Symfony/migrations/) stop at March 2023 and are behind the entities; prefer `doctrine:schema:validate` over trusting them.
- **Routing is annotation/attribute-only** (`config/routes.yaml` is fully commented out). Both styles coexist, sometimes in the same class — PHP 8 `#[Route(...)]` and legacy `/** @Route(...) */` docblocks. Match whichever the surrounding class uses.
- **Controller layering is inconsistent by design of history, not intent.** Most controllers are fat: they inject repositories directly and often write to the entity manager inline (e.g. `FrontController::front` mutates and persists `Equipe` rows on a GET page render). The intended pattern is the thin slice — `App\Manager\ReservationManager` + `App\Service\StripeService` — which is the only place business logic is factored out. Put new payment/reservation logic there.
- **Dual front-end/back-office/mobile split**: `Front*`/`Pages*` render public Twig, `Admin*`/`Back*` render the back office (`templates/admin/`, `back.html.twig`), and `*MobileController` (`BlogMobileController`, `HebergementmobileController`) return `JsonResponse` built with the Serializer for the mobile client. Adding a mobile endpoint means a new `*mobile` controller action, not content negotiation.
- **External services** are called with hardcoded credentials in source, not env vars, in `SentimentController` (RapidAPI text-analysis) and `ChatBotController` (BotMan web driver, referencing an `App\ChatBot\Conversation\*` namespace that does not exist in `src/`). Stripe keys do come from `.env` via `StripeService`, keyed on `APP_ENV === 'dev'` choosing `*_KEY_TEST` vs `*_KEY_LIVE`.
- **Front-end assets** are committed, not built: `public/assets`, `public/assetsFront`, `public/assetsLogin`, uploads in `public/uploads`, plus a Bower-style `Symfony/components/` (jQuery, moment, fullcalendar, RequireJS). There is no npm/webpack/Encore step.
- `.env` in this working copy contains real-looking Google/Facebook/Mailtrap/Stripe-test secrets. Treat them as compromised; do not propagate them into new files.

## Java architecture notes

Layered by French-named packages: `Entités` (POJOs: `MatchF`, `Equipe`) → `Interfaces` (`InterfaceMatch<T>` with `add`/`read`) → `Services` (`MatchFCRUD` implements it) → `Controlleurs` (JavaFX controllers for `Views/*.fxml`). `Utils.DataBase` is a lazily-initialized singleton wrapping one `java.sql.Connection` to `jdbc:mysql://localhost:3306/tunisport`.

Note when touching `Services`: the existing CRUD builds SQL by string concatenation of entity fields (`MatchFCRUD.add`), which is injection-prone and breaks on quotes/dates. Use `PreparedStatement` in new code — the fields are already declared on the class.

Entity/table naming must stay in sync with the Symfony side by hand: e.g. Java writes to `match_f (id, equipe_a_id, equipe_b_id, date_match, type_match_id, stade_id, tournoi_id, resultat_a, resultat_b, prix, image, image2)`, which is Doctrine's naming for `App\Entity\MatchF`. A Doctrine migration that renames a column silently breaks the Java client.
