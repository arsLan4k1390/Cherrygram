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
- Monet (Material You)
- HQ Voice Messages,
- No content restrictions,
- Support of up to 10 accounts,
- Keep-alive service is enabled by default,
- Added ability to see avatar uploading date,
- Increased avatars limit from 80 to 100,
- Allow to set a proxy before login,
- Added native biometrics support.

*Chats:*
- Bubble radius is set to 17,
- Chat Blur support for all themes (by default),
- Fast search (Open a search by holding dialog name),
- Jump to the beginning of any chat,
- Added "Remove file from cache",
- Added IOS's incoming message sound,
- Recent stickers value is raised from 20 to max value coming from backend,
- Viewing admins (Group info) for all members,
- Opening linked channel from a group,
- Open avatar by swiping down in profile,
- Mention in chat by name (long press on username).

*Cherrygram Appearance Preferences:*
- VKUI icons in app,
- Back button from IOS (Beta),
- Hiding your phone number from settings and menu,
- Flat statusbar is enabled by default,
- Mutual contact icon in contacts list,
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
- Hide "All chats" tab,
- Hide messages counter in tabs,
- Show tabs on forwarding messages screen,
- Tab icon style (emojis),
- Play GIFs as videos,
- Enable/Disable playing video on volume button click,
- Auto pause video while switching to the background
- Pause music while playing voice and video messages,
- Disable in-app vibration,
- Disable camera in attachments menu,
- Using rear camera by default for video messages,
- Enable/Disable proxymity sensor actions,
- Enable/Disable incoming message sound from Telegram IOS app,
- Silence notifications from non-contacts.


## API, Protocol documentation

Telegram API manuals: https://core.telegram.org/api

MTproto protocol manuals: https://core.telegram.org/mtproto


## Compilation Guide

1. Download the Nekogram source code ( `git clone https://github.com/arslan4k1390/Cherrygram.git` )
1. Fill out storeFile, storePassword, keyAlias, keyPassword in app's build.gradle to sign your app
1. Go to https://console.firebase.google.com/, create two android apps with application IDs uz.unnarsx.cherrygram and uz.unnarsx.cherrygram.beta, turn on firebase messaging and download `google-services.json`, which should be copied into `TMessagesProj` folder.
1. Open the project in the Studio (note that it should be opened, NOT imported).
1. Fill out values in `TMessagesProj/src/main/java/uz/unnarsx/cherrygram/Extra.java` – there’s a link for each of the variables showing where and which data to obtain.
1. You are ready to compile and use Cherrygram.


## Thanks to:
- [Catogram](https://github.com/Catogram/Catogram)
- [Nekogram](https://gitlab.com/Nekogram/Nekogram)
- [Nekogram X](https://github.com/NekoX-Dev/NekoX)
- [Telegram-FOSS](https://github.com/Telegram-FOSS-Team/Telegram-FOSS)
- [Telegram Monet](https://github.com/c3r5b8/Telegram-Monet)
