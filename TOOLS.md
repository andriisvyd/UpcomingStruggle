# AI tool usage

## Claude Fable 5 (browser)

Crafting concrete requirements against a deliberate app design. Output:
`design/specification.html`.

## Claude Opus 5 (Claude Code CLI)

- **Provider research** — summarizing API documentation, retrieving fixtures from live
  responses
- **`:core:designsystem`** — tokens, typography, icons, domain-free primitives, previews,
  gallery. Effective against the specification
- **`:feature:forecast`, `:feature:search`** — screens, components, UI state. Effective
  against the design system and the specification
- **All modules** — code scaffolding, Koin wiring, mappers, KDoc, test coverage

## Where it did not help
The more ambiguous the context - the less useful an LLM is.

`:core:domain` and `:core:data`. Proposals arrived plausible yet wrong:
- one repository over a single `DataStore`
- use cases that only forwarded a call
- abstractions with no consumer anywhere in the app

## What was not delegated
- Modularization scheme
- Structural separation: platform-agnostic :domain
- Location and reverse geocoding owned by :data
- Encapsulation of providers
- Rules boundaries
- Cross-feature communication
- App's failure modes
