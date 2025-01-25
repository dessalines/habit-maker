# Contributing to Habit-Maker

<!-- prettier-ignore-start -->

<!-- toc -->

- [Ways to contribute](#ways-to-contribute)
- [Application structure](#application-structure)
- [Code contributions](#code-contributions)
  * [Kotlin](#kotlin)
  * [Code quality](#code-quality)
- [Adding translations](#adding-translations)

<!-- tocstop -->

<!-- prettier-ignore-end -->

## Ways to contribute

- Participate here and start answering questions.
- File new bug reports for issues you find.
- Add missing translations.
- Code contributions.

## Application structure

- Basic [Modern Android Development](https://developer.android.com/series/mad-skills) tech stack (Compose, Navigation, Coroutines, AndroidX)
- Guide to [App Architecture](https://developer.android.com/topic/architecture), without domain layer. Basically, MVVM + Repositories for data access.

## Code contributions

You can open in AndroidStudio, version 2022.3.1 or later (Giraffe).

Use Java 11+, preferably Java 17

### Kotlin

This project is full Kotlin. Please do not write Java classes.

### Code quality

The code must be formatted to a [common standard](https://pinterest.github.io/ktlint/0.49.1/rules/standard/).

To check for violations

```shell
./gradlew lintKotlin
```

Or just run this to fix them

```shell
./gradlew formatKotlin
```

Markdown and yaml files are formatted according to prettier.

You can install prettier either through the plugin, or globally using npm `npm install -g prettier`

To check for violations

```shell
prettier -c "*.md" "*.yml"
```

To fix the violations

```shell
prettier --write "*.md" "*.yml"
```

## Adding translations

You can add translation via [weblate](https://hosted.weblate.org/engage/habit-maker/), or add translations manually in the `app/src/main/res/values-{locale}/strings.xml` file.

You can open it in android studio, right click and click open translations editor or you can
directly edit the files.

If you add a new locale. Also add it in `locales_config.xml`. Don't forget to escape `'` in translations.
