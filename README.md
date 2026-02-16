# Points Tracker
This app keeps a running tally of accumulated rewards from supported credit card apps and sends a notification when a threshold is reached.
For example, it can notify the user every time a $100 threshold is reached.

## Supported cards
* Robinhood Gold Card (`com.robinhood.money`): tracks points parsed from notification text like `(+63 points)`.
* Chase (`com.chase.sig.android`): tracks dollar transaction amounts parsed from notification text like `$20.89` (stored as cents internally).

Android requires giving the app access to notifications, but the app only parses notifications from supported card apps.
The app does not collect or track anything except accumulated points count. It stores everything locally and does not ask for permissions to access the internet.
Because the app has no internet permission, it cannot send your notification data or totals to external servers, which helps protect user privacy.
For the time being it is not published to the app store as I made it for personal use. I may publish it if anyone asks.

## Important disclaimers:
* This app and code is not affiliated in anyway with Robinhood Markets, Inc.
* This app was vibe coded with AI and likely contains bugs.
* See [License](LICENSE) for more details

## Screenshot for dark mode:

<img src="https://github.com/user-attachments/assets/e8ef53ca-5cd4-4c0b-8e99-8f1dcb82eee2" width="200">

