# Points Tracker
This app keeps a running tally of accumulated rewards from supported credit card apps and sends a notification when a threshold is reached.
For example, it can notify the user every time a $100 in redeemable cashback threshold is reached.

## Supported cards
* Robinhood Gold Card (`com.robinhood.money` android package): tracks points parsed from notification text like `(+63 points)`.
* Chase (`com.chase.sig.android` android package): tracks dollar transaction amounts parsed from notification text like `$20.89`.

## Install APK
Download the latest published artifact directly:
[PointsTracker-v1.0-1-release-unsigned.apk](https://github.com/tsubery/PointsTracker/actions/runs/22049430585/artifacts/5520397101)

1. Download the APK from the link above.
2. Move the APK to your Android device and install it.
3. If prompted, allow installs from unknown sources for the app you used to open the APK.

Android requires giving the app access to notifications, but the app only parses notifications from supported card apps.
The app does not collect or track anything except accumulated points count. It stores everything locally and does not ask for permissions to access the internet.
Because the app has no internet permission, it cannot send your notification data or totals to external servers, which helps protect user privacy.
For the time being it is not published to the app store as I made it for personal use. I may publish it if anyone asks.

## Important disclaimers:
* This project is not affiliated in anyway with Robinhood Markets, Inc and/or JP Morgan Chase.
* This app was vibe coded with AI. It works fine for me but may contain bugs.
* See [License](LICENSE) for more details

## Screenshot for dark mode:

<img src="https://github.com/user-attachments/assets/e8ef53ca-5cd4-4c0b-8e99-8f1dcb82eee2" width="200">
