# r/WearOS post

**Title:** I built an app that tells you which of your phone apps have Wear OS versions [Open Source]

**Body:**

Like a lot of you, when I got my Pixel Watch 3 I had no idea which of my existing apps had watch companions. The Play Store doesn't surface this anywhere — you just have to know or stumble across them.

So I spent the weekend building **WearSync**: an app that scans your phone, checks your watch, then queries the Play Store to find which missing apps have Wear OS versions. Everything shows up in a clean list with one-tap install links straight to the Play Store.

**How it works:**
- Phone side scans your installed apps
- Talks to the watch over the Wearable Data Layer (Bluetooth, no internet)
- Checks Play Store for Wear OS availability
- Caches results so it's instant after the first scan

**Privacy:** Zero data collection. No analytics, no accounts, no Firebase. The only network calls are Play Store lookups (just package names) and the install deep-links.

It's open source (Apache 2.0) and available on GitHub with a direct APK download. Play Store submission is next — once it's on the Play Store, the watch companion will install automatically alongside the phone app (no more sideloading).

GitHub + APK download: https://github.com/dinosoup1/wearsync

Would love to hear if it works well on different Wear OS versions / watch models. I've only tested on Pixel Watch 3 so far.
