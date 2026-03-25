# Show HN: WearSync – Find which of your phone apps have Wear OS versions

**Title:** Show HN: WearSync – Discover which of your phone apps have Wear OS versions

**URL:** https://github.com/dinosoup1/wearsync

---

**Body:**

When I got my Pixel Watch 3, I had no idea which of my phone apps had watch companions. The Play Store doesn't surface this anywhere. You either stumble across them or miss them forever.

So I built WearSync. It:
1. Scans your installed phone apps
2. Queries your watch for what's already there
3. Checks the Play Store to find which missing apps have Wear OS versions
4. Shows you a clean list with one-tap install links

The watch communication is done via the Wearable Data Layer API (Bluetooth/WiFi Direct) — no internet required for that part. The Play Store check is the only network call, and results are cached so subsequent launches are instant.

Privacy-first by design: no accounts, no analytics, no Firebase, no data collection of any kind. The only things that leave your device are Play Store package name lookups and the install deep-links.

APK available on GitHub releases if you want to try it today. Play Store submission coming soon (when both modules are published together, the watch companion installs automatically — no sideloading needed).

Repo: https://github.com/dinosoup1/wearsync

Would love feedback on the Wear OS detection accuracy — the Play Store HTML parsing is the shakiest part right now.
