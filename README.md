# Mac Duo Enhanced

把 iPhone Duo 的合盖折叠玻璃动画带到 MacBook，效果更明显、触发更灵敏。

An enhanced fork of [sumimakito/Mac-Duo](https://github.com/sumimakito/Mac-Duo).

安卓版 / Android version：[android/README.md](android/README.md)

## 应用图标

仓库内含 Apple 风格图标（白底黑 Apple 标）：

- `assets/AppIcon.icns`：macOS 应用图标。
- `assets/AppIcon-preview.png`：1024×1024 预览图。
- `tools/make_icon.swift`：图标生成脚本。

生成方式：

```sh
swift tools/make_icon.swift AppIcon.iconset
iconutil -c icns AppIcon.iconset -o AppIcon.icns
```

---

## 中文说明（macOS 26 及以上）

### 一、安装全过程

#### 方式 A：官方签名 DMG（推荐，免编译）

1. 下载已签名、已公证的官方 DMG：

   [下载 Mac-Duo-dev.dmg](https://github.com/sumimakito/Mac-Duo/releases/download/dev/Mac-Duo-dev.dmg)

2. 双击 DMG，把 `Mac Duo.app` 拖进「应用程序」（`/Applications`）。
3. 首次打开时，如果系统提示「无法验证开发者」，请右键点击 App →「打开」，再点一次「打开」即可（官方版本已公证，通常直接双击就能打开）。
4. 打开后，菜单栏会出现 Mac Duo 图标。

> 官方 DMG 自带的是原版参数（触发角度 90° 等）。想直接用本分支的「增强参数」而不重新编译，可在终端执行下面的命令，然后退出并重新打开 Mac Duo：

```sh
defaults write to.maki.MacDuo thresholdAngle -float 112
defaults write to.maki.MacDuo blurSpan -float 28
defaults write to.maki.MacDuo maxBlurRadius -float 220
defaults write to.maki.MacDuo recession -float 1.8
defaults write to.maki.MacDuo blurEvenness -float 0.3
defaults write to.maki.MacDuo dimReach -float 0.8
defaults write to.maki.MacDuo viewingDistance -float 4.2
```

想恢复原版默认参数：

```sh
defaults delete to.maki.MacDuo
```

#### 方式 B：从源码编译（获得完整增强效果）

完整增强版还包含更快的传感器轮询、更陡的模糊/变暗曲线等编译期改动，需要自己编译：

1. 安装 Xcode（含 Swift 6.0 及以上）。
2. 克隆本仓库：

```sh
git clone https://github.com/zengpu987-ctrl/mac-duo-enhanced.git
cd mac-duo-enhanced
```

3. 编译并运行：

```sh
./build.sh --run
```

产物是 `build/Mac Duo.app`。本地编译是 ad-hoc 签名，系统会要求重新授权「屏幕录制」。

#### 授权「屏幕录制」

效果需要实时捕捉屏幕画面，必须授权：

1. 打开「系统设置」→「隐私与安全性」→「屏幕录制」。
2. 在列表里打开 **Mac Duo** 的开关。
3. 系统通常会提示「退出并重新打开」，照做即可；也可以在 App 设置面板里点「打开系统设置」。

### 二、使用过程

1. 点击菜单栏的 Mac Duo 图标，打开设置面板。
2. 确认「深度效果」（Depth effect）开关为开启状态。
3. 可选开启「实时渲染」（Live rendering）；关闭时使用触发那一刻的定格画面。
4. 慢慢把屏幕盖往下压（不用完全合上）：
   - 增强版默认在 **112°** 左右开始触发。
   - 继续合到 **28°** 左右达到满效果。
   - 画面会向后倾斜、磨砂模糊、并逐渐变暗。
5. 在设置面板里可以调整：
   - 开始角度（Start angle）
   - 满效果行程（Full effect after）
   - 模糊强度（Blur）
   - 模糊范围 / 变暗（Blur spread / Dimming）
   - 透视折叠幅度（Lean back / Perspective）
6. 「在菜单栏显示角度」（Show angle in menu bar）可实时显示当前合盖角度，方便调参。

### 三、故障处理流程

1. **完全没有效果**
   - 检查「深度效果」开关是否打开。
   - 检查「屏幕录制」权限是否已授权给 Mac Duo（授权后要退出并重开 App）。
   - 确认这台 MacBook 有可用的合盖角度传感器（App 会提示「这台 Mac 没有合盖角度传感器」）。

2. **完全合盖后动画消失**
   - 这是正常现象：MacBook 完全合盖会进入休眠，动画随休眠停止。
   - 想看完整折叠动画：外接显示器让机器保持唤醒，或用 `caffeinate` 暂时禁止休眠，再慢慢合盖。

3. **源码编译后屏幕录制授权失效**
   - 本地编译是 ad-hoc 签名，与原公证签名不同，macOS 会要求重新授权。
   - 重新到「系统设置 → 隐私与安全性 → 屏幕录制」打开 Mac Duo，然后退出并重开。

4. **提示「这台 Mac 没有合盖角度传感器」**
   - 只有部分 MacBook 机型内置该传感器；外接显示器上的同类传感器会被忽略。

5. **效果太夸张 / 太灵敏**
   - 在设置面板里把「开始角度」调小、把「模糊」「变暗」「透视」调低；
   - 或执行 `defaults delete to.maki.MacDuo` 恢复原版默认参数。

6. **外接显示器上没有效果**
   - 该效果只作用于 MacBook 内置屏幕。

7. **反复弹「屏幕录制」授权 / 想根治**
   - 根本原因：只要重新签名 App（例如 ad-hoc `codesign -s -`），代码签名身份就变了，macOS 会把它当成新程序重新要授权。
   - 根治办法：使用官方公证 DMG（Developer ID 签名，身份稳定），授权一次后永久记住，不再重签。
   - 不要把文件直接塞进 `.app` 包体（例如自定义图标），因为改包体就必须重签名。
   - 想要自定义图标又不破坏签名：用访达「显示简介 → 把图片拖到左上角图标处」（存的是元数据，不改变包体），或直接使用默认图标。

---

## English Guide (macOS 26 and later)

### Installation

#### Option A: Official signed DMG (recommended, no build)

1. Download the notarized DMG:

   [Download Mac-Duo-dev.dmg](https://github.com/sumimakito/Mac-Duo/releases/download/dev/Mac-Duo-dev.dmg)

2. Open the DMG and drag `Mac Duo.app` into `/Applications`.
3. If macOS shows an "unidentified developer" warning, right-click the app and choose **Open**.
4. The Mac Duo icon appears in the menu bar.

> The official DMG ships with the original tuning (90° trigger, etc.). To apply this fork's enhanced tuning without rebuilding, run the following in Terminal, then quit and reopen Mac Duo:

```sh
defaults write to.maki.MacDuo thresholdAngle -float 112
defaults write to.maki.MacDuo blurSpan -float 28
defaults write to.maki.MacDuo maxBlurRadius -float 220
defaults write to.maki.MacDuo recession -float 1.8
defaults write to.maki.MacDuo blurEvenness -float 0.3
defaults write to.maki.MacDuo dimReach -float 0.8
defaults write to.maki.MacDuo viewingDistance -float 4.2
```

To restore the original defaults:

```sh
defaults delete to.maki.MacDuo
```

#### Option B: Build from source (full enhanced behavior)

1. Install Xcode with Swift 6.0 or later.
2. Clone this repository:

```sh
git clone https://github.com/zengpu987-ctrl/mac-duo-enhanced.git
cd mac-duo-enhanced
```

3. Build and launch:

```sh
./build.sh --run
```

The output is `build/Mac Duo.app`. A local build is ad-hoc signed, so macOS will ask for Screen Recording permission again.

#### Grant Screen Recording

1. Open **System Settings → Privacy & Security → Screen Recording**.
2. Enable **Mac Duo**.
3. Quit and reopen the app when prompted.

### Usage

1. Click the Mac Duo icon in the menu bar.
2. Make sure **Depth effect** is enabled.
3. Optionally enable **Live rendering** (off = use the frame captured when the effect started).
4. Slowly close the lid without fully shutting it:
   - The enhanced build triggers around **112°**.
   - It reaches full strength around **28°**.
   - The screen tilts back, blurs, and dims.
5. Tune **Start angle**, **Full effect after**, **Blur**, **Blur spread / Dimming**, and **Lean back / Perspective** in the settings panel.
6. Enable **Show angle in menu bar** to watch the current lid angle live.

### Troubleshooting

1. **No effect at all**
   - Make sure **Depth effect** is on.
   - Grant **Screen Recording** permission and reopen the app.
   - Make sure this MacBook has a built-in lid angle sensor.

2. **The effect disappears when the lid is fully closed**
   - Expected: macOS sleeps when the lid closes, which stops the effect.
   - Keep the Mac awake with an external display or `caffeinate`, then close the lid slowly.

3. **Screen Recording permission is lost after building from source**
   - The ad-hoc signature differs from the notarized release. Re-grant Screen Recording and reopen.

4. **"This Mac has no lid angle sensor"**
   - Only some MacBook models have the sensor; equivalent sensors on external displays are ignored.

5. **The effect is too aggressive or too sensitive**
   - Lower the sliders, or run `defaults delete to.maki.MacDuo` to restore original defaults.

6. **No effect on an external display**
   - The effect only applies to the built-in display.

7. **Screen Recording keeps asking / how to fix it permanently**
   - Root cause: re-signing the app (for example ad-hoc `codesign -s -`) changes its code-signing identity, so macOS treats it as a new app and asks again.
   - Permanent fix: use the official notarized DMG (Developer ID signature, stable identity); grant once and it sticks. Do not re-sign.
   - Do not write files directly into the `.app` bundle (e.g. custom icons), because changing the bundle requires re-signing.
   - To set a custom icon without breaking the signature, use Finder → Get Info → drag an image onto the icon at the top-left corner, or keep the default icon.

---

## 本分支改动 / Changes

更灵敏 / More sensitive：

- 触发角度 `90° → 112°`
- 传感器轮询 `8/30 Hz → 15/60 Hz`
- 判定合盖速度 `2 °/s → 1.2 °/s`
- 预测提前量更大

更明显 / More obvious：

- 最大模糊 `135 → 220 pt`
- 折叠幅度 `1.0 → 1.8`
- 全屏磨砂 `0 → 0.3`
- 变暗范围 `0.5 → 0.8`
- 透视距离 `6.0 → 4.2`
- 满效果行程 `60° → 28°`
- 模糊 / 变暗曲线更陡

## 感谢原作者 / Acknowledgements

本项目基于 [sumimakito/Mac-Duo](https://github.com/sumimakito/Mac-Duo)（Apache-2.0，Copyright 2026 Makito）。衷心感谢原作者 **Makito** 的优秀设计与完整实现，以及 [Moeru AI](https://github.com/moeru-ai) 在签名、公证与发布上的支持。原项目的合盖角度传感器驱动、Metal 渲染器、ScreenCaptureKit 集成等核心工作均出自原项目，本分支只做了动画参数增强。

## License

Apache License 2.0。详见 [LICENSE](LICENSE)，以及 [NOTICE](NOTICE) 中的上游署名。
