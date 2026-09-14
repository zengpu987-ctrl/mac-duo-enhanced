import AppKit

let out = CommandLine.arguments.count > 1 ? CommandLine.arguments[1] : "AppIcon.iconset"
try? FileManager.default.createDirectory(atPath: out, withIntermediateDirectories: true)

let sizes: [(Int, String)] = [
    (16,"icon_16x16.png"), (32,"icon_16x16@2x.png"),
    (32,"icon_32x32.png"), (64,"icon_32x32@2x.png"),
    (128,"icon_128x128.png"), (256,"icon_128x128@2x.png"),
    (256,"icon_256x256.png"), (512,"icon_256x256@2x.png"),
    (512,"icon_512x512.png"), (1024,"icon_512x512@2x.png"),
]

func make(_ px: Int) -> NSImage {
    let s = CGFloat(px)
    let img = NSImage(size: NSSize(width: s, height: s))
    img.lockFocus()
    let r = s * 0.2237
    NSColor.white.setFill()
    NSBezierPath(roundedRect: NSRect(origin: .zero, size: NSSize(width: s, height: s)), xRadius: r, yRadius: r).fill()
    let font = NSFont(name: "Apple Symbols", size: s * 0.56) ?? NSFont.systemFont(ofSize: s * 0.56)
    let str = NSAttributedString(string: "\u{F8FF}", attributes: [.font: font, .foregroundColor: NSColor.black])
    let sz = str.size()
    str.draw(at: NSPoint(x: (s - sz.width)/2, y: (s - sz.height)/2))
    img.unlockFocus()
    return img
}

for (px, name) in sizes {
    if let tiff = make(px).tiffRepresentation,
       let rep = NSBitmapImageRep(data: tiff),
       let png = rep.representation(using: .png, properties: [:]) {
        try? png.write(to: URL(fileURLWithPath: out).appendingPathComponent(name))
    }
}
print("done")
