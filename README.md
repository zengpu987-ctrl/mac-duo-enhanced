# Mac Duo Enhanced

把 iPhone Duo 的合盖折叠玻璃动画带到 MacBook，并让动画更明显、更灵敏。

An enhanced fork of [sumimakito/Mac-Duo](https://github.com/sumimakito/Mac-Duo).

---

## 中文说明

本项目是 [sumimakito/Mac-Duo](https://github.com/sumimakito/Mac-Duo) 的增强分支，在原版「合盖时桌面倾斜、磨砂玻璃模糊、渐隐」的折叠动画基础上，把效果调得更夸张、触发更早，一眼就能看到。

### 更灵敏

- 触发角度 `90° → 112°`，几乎一压盖就开始。
- 传感器轮询 `8/30 Hz → 15/60 Hz`。
- 判定合盖速度 `2 °/s → 1.2 °/s`。
- 预测提前量更大，快速合盖也能立刻触发。

### 更明显

- 最大模糊 `135 → 220 pt`
- 折叠幅度 `1.0 → 1.8`
- 全屏磨砂占比 `0 → 0.3`
- 变暗范围 `0.5 → 0.8`
- 透视距离 `6.0 → 4.2`
- 满效果行程 `60° → 28°`
- 模糊 / 变暗曲线更陡，起效更快

这些默认值位于 `Sources/MacDuo/Preferences.swift`，运行时参数位于 `Sources/MacDuo/LidController.swift` 和 `Sources/MacDuo/BlurGradient.swift`。

### 构建

需要 Xcode 与 Swift 6.0 以上：

```sh
./build.sh
```

构建产物为 `build/Mac Duo.app`。本地重编译为 ad-hoc 签名，macOS 会再次要求「屏幕录制」授权。

## 感谢原作者 / Acknowledgements

本项目基于 [sumimakito/Mac-Duo](https://github.com/sumimakito/Mac-Duo)（Apache-2.0，Copyright 2026 Makito）。衷心感谢原作者 **Makito** 的优秀设计与完整实现，以及 [Moeru AI](https://github.com/moeru-ai) 在签名、公证与发布上的支持。原项目中的合盖角度传感器驱动、Metal 渲染器、ScreenCaptureKit 集成等核心工作均出自原项目，本分支只做了动画参数增强。

---

## English

This is an enhanced fork of [sumimakito/Mac-Duo](https://github.com/sumimakito/Mac-Duo)
(Apache-2.0, Copyright 2026 Makito). All credit for the original design, Metal
renderer, lid-angle sensor, and ScreenCaptureKit integration goes to the
upstream author and [Moeru AI](https://github.com/moeru-ai).

### What changed

- Trigger angle `90° → 112°`; sensor polling `8/30 Hz → 15/60 Hz`; closing-speed
  trigger `2 °/s → 1.2 °/s`; earlier prediction.
- Max blur `135 → 220 pt`, fold recession `1.0 → 1.8`, whole-picture frost
  `0 → 0.3`, dimming reach `0.5 → 0.8`, viewing distance `6.0 → 4.2`,
  full-effect travel `60° → 28°`, steeper blur/dim curves.

See `Sources/MacDuo/Preferences.swift`, `LidController.swift`, and
`BlurGradient.swift` for the exact values.

### Build

Xcode with Swift 6.0 or later:

```sh
./build.sh
```

The result is `build/Mac Duo.app`. A local ad-hoc rebuild requires granting
Screen Recording permission again.

## License

Apache License 2.0. See [LICENSE](LICENSE) and the upstream attribution in
[NOTICE](NOTICE).
