# 🍒 Cherrygram

Cherrygram is a third-party Telegram client with not many but useful modifications.

This is an unofficial fork of the original [Telegram App for Android](https://github.com/DrKLO/Telegram).

This repo contains the official source code for [Telegram App for Android](https://play.google.com/store/apps/details?id=org.telegram.messenger).

## Current Maintainers

- [arsLan4k1390](https://github.com/arsLan4k1390)
- you? :)

## Contributors

- [arsLan4k1390](https://github.com/arsLan4k1390)


## Discussion

Join the [Cherrygram official channel](https://t.me/cherry_gram)

Join the [Cherrygram official group](https://t.me/cherry_gram_support)

## Changes:

*General:*
- Material You (Monet) themes and app icon,
- HQ Voice Messages,
- No content restrictions,
- Support of up to 10 accounts,
- Added ability to see avatar uploading date,
- Increased avatars limit from 80 to 100,
- Allow to set a proxy before login,
- Added native biometrics support,
- "tg://user?id=int" Links support (e.g. "tg://user?id=282287840").

*Chats:*
- "Mark as read" support for folders,
- Bubble radius is set to 17,
- Chat Blur support for all devices and themes (Blur is enabled by default),
- Fast search (Open a search by holding dialog name),
- Jump to the beginning of any chat,
- Delete all OWN messages,
- Added "Remove file from cache",
- Messages history from any user and channel in any chat,
- Select messages in chat between message A and message B,
- Recent stickers value is raised from 20 to max value coming from backend,
- View admins (Group info) for all members,
- Open the linked channel from a group,
- Open avatar by swiping down in profile,
- Mention in chat by name (long press on username),
- Added "View Stats" button to the context menu.

*Telegram Folders Settings (Tab icon style):*
- Only titles,
- Only icons,
- Icons with titles.

*Cherrygram Appearance Preferences:*
- App Icon: Default, White and Material You (Monet),
- VKUI icons in app,
- Back button from IOS (Beta),
- Hiding your phone number from settings and menu,
- Flat statusbar,
- Mutual contact icon in contacts list,
- Enable/Disable System fonts,
- Hide "All chats" tab,
- Hide messages counter in tabs,
- Show tabs on forward screen,
- Showing ID in profile,
- Showing DC in profile,
- Avatar as menu header background,
- Blur menu header background,
- Darken menu header background,
- Enable/Disable "Saved messages" button in menu,
- Enable/Disable "Archived chats" button in menu,
- Enable/Disable "People Nearby" button in menu.

*Cherrygram Chats Preferences:*
- Stickers size amplifier (changer),
- Hide time on stickers,
- Scrollable chat preview,
- Showing unread chats counter on "Back" button like on IOS,
- Enable/Disable members rounding,
- Ask before a call,
- Show forwarded message date,
- Show seconds in timestamps,
- Enable/Disable quick reactions,
- Enable/Disable quick reactions animation,
- Enable/Disable transition to the next channel,
- Hide keyboard while scrolling a chat,
- Enable/Disable "Send as channel" button,
- Recent emojis counter (amplifier),
- Recent stickers counter (amplifier),
- Play GIFs as videos,
- Enable/Disable playing video on volume button click,
- Auto pause video while switching to the background
- Pause music while playing voice and video messages,
- Disable in-app vibration,
- Disable "Flip" photos,
- Disable camera in attachments menu,
- Rear camera by default for video messages,
- Enable/Disable proxymity sensor actions,
- Enable/Disable incoming message sound from Telegram IOS app,
- Silence notifications from non-contacts.

*Cherrygram Security Preferences:*
- "Kaboom" (Erase all Cherrygram data in 1 click),
- Delete your Telegram account.


## API, Protocol documentation

Telegram API manuals: https://core.telegram.org/api

MTproto protocol manuals: https://core.telegram.org/mtproto


## Compilation Guide

1. Download the Cherrygram source code ( `git clone https://github.com/arslan4k1390/Cherrygram.git` )
1. Fill out storeFile, storePassword, keyAlias, keyPassword in app's build.gradle to sign your app
1. Go to https://console.firebase.google.com/, create two android apps with application IDs uz.unnarsx.cherrygram and uz.unnarsx.cherrygram.beta, turn on firebase messaging and download `google-services.json`, which should be copied into `TMessagesProj` folder.
1. Open the project in the Studio (note that it should be opened, NOT imported).
1. Fill out values in `TMessagesProj/src/main/java/uz/unnarsx/cherrygram/Extra.java` – there’s a link for each of the variables showing where and which data to obtain.
1. You are ready to compile and use Cherrygram.


## Thanks to:
- [Catogram](https://github.com/Catogram/Catogram)
- [Nekogram](https://gitlab.com/Nekogram/Nekogram)
- [Nekogram X](https://github.com/NekoX-Dev/NekoX)
- [OwlGram](https://github.com/OwlGramDev/OwlGram)
- [Telegram-FOSS](https://github.com/Telegram-FOSS-Team/Telegram-FOSS)
- [Telegram Monet](https://github.com/c3r5b8/Telegram-Monet)
