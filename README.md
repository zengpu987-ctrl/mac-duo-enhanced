# Mac Duo Enhanced

把 iPhone Duo 的合盖折叠玻璃动画带到 MacBook 上，并让动画更明显、更灵敏。

This is an enhanced fork of [sumimakito/Mac-Duo](https://github.com/sumimakito/Mac-Duo)
(Apache-2.0, Copyright 2026 Makito). All credit for the original design, Metal
renderer, lid-angle sensor, and ScreenCaptureKit integration goes to the upstream
author and [Moeru AI](https://github.com/moeru-ai).

## What changed

The fork re-tunes the animation for a stronger, earlier, more responsive fold.

### More sensitive

- Trigger angle: `90°` → `112°`, so barely closing the lid starts the effect.
- Sensor polling: idle `8 Hz` → `15 Hz`, active `30 Hz` → `60 Hz`.
- Closing-speed trigger: `2 °/s` → `1.2 °/s`.
- Prediction floor: `40 °/s` → `18 °/s`; latency `0.04 s` → `0.05 s`.
- Pre-warm closing speed: `8 °/s` → `5 °/s`.

### More obvious

- Max blur: `135 pt` → `220 pt`.
- Fold recession: `1.0` → `1.8`.
- Whole-picture frost: `0.0` → `0.3`.
- Dimming reach: `0.5` → `0.8`.
- Perspective viewing distance: `6.0` → `4.2`.
- Full-effect travel: `60°` → `28°`.
- Blur curve: `1.6` → `1.15`; dim curve: `0.7` → `0.55`.
- Slider ranges widened (`Blur` up to `320 pt`, `Lean back` up to `4×`).

These are the default values in `Sources/MacDuo/Preferences.swift` plus the
runtime constants in `Sources/MacDuo/LidController.swift` and
`Sources/MacDuo/BlurGradient.swift`.

## Requirements

- macOS 14 or later.
- A MacBook with a compatible built-in lid angle sensor.
- Screen Recording permission, granted when the app first starts the effect.

## Build

Xcode with Swift 6.0 or later:

```sh
./build.sh
```

Or build and relaunch in one step:

```sh
./build.sh --run
```

The result is `build/Mac Duo.app`. A local rebuild is ad-hoc signed, so macOS
will ask for Screen Recording permission again.

## License

Apache License 2.0. See [LICENSE](LICENSE) and the upstream attribution in
[NOTICE](NOTICE).
