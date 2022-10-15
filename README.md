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

Join the [Cherrygram official channel](https://t.me/Cherry_gram)

Join the [Cherrygram official group](https://t.me/CherrygramSupport)

## Changes:

*General:*
- Material You (Monet) themes and app icon,
- No content restrictions,
- Support of up to 10 accounts,
- Added ability to see avatar uploading date,
- Increased avatars limit from 80 to 100,
- Mutual contact icon in contacts list,
- Allow to set a proxy before login,
- Added native biometrics support,
- "tg://user?id=int" Links support (e.g. "tg://user?id=282287840"),
- Allow to set "Keep media" for one day,
- "Kaboom" (Erase all Cherrygram data in 1 click) (Thanks to Telegraher),
- Force clear Telegram cache.

*Chats:*
- Scrollable chat preview,
- "Mark as read" support for folders,
- Chat Blur support for all devices and themes (Blur is enabled by default),
- Fast search (Open a search by holding dialog name),
- Jump to the beginning of any chat,
- Delete all OWN messages from groups,
- "Remove file from cache" feature for files in chat,
- Messages history from any user and channel in any chat,
- Select messages in chat between message A and message B,
- View admins (Group info) for all members,
- Open avatar by swiping down in profile,
- Mention in chat by name (long press on username).

*Telegram Folders Settings (Tab icon style):*
- Only titles,
- Only icons,
- Icons with titles,
- Hide messages counter in tabs.

*Cherrygram Appearance Preferences:*
- VKUI icons in app,
- Enable/Disable toolbar shadow,
- Flat statusbar,
- Transparent navigation bar,
- Enable/Disable System fonts,
- Show folder name instead of app name,
- Hide "All chats" tab,
- Show tabs on forward screen,
- Hiding your phone number from settings and menu,
- Showing ID in profile,
- Showing DC in profile,
*Drawer Preferences:*
- Profile photo as drawer background,
- Darken menu header background,
- Gradient menu header background,
- Blur menu header background and blur itensity,
- Drawer icons set,
- Enable/Disable drawer buttons.

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
- Enable/Disable premium stickers animation,
- Enable/Disable transition to the next channel,
- Hide keyboard while scrolling a chat,
- Enable/Disable "Send as channel" button,
- Recent emojis counter (amplifier),
- Recent stickers counter (amplifier),
- Enable/Disable playing video on volume button click,
- Auto pause video while switching to the background
- Pause music while playing voice and video messages,
- Disable in-app vibration,
- Disable "Flip" photos,
- Enable/Disable proxymity sensor actions,
- Enable/Disable incoming message sound from Telegram IOS app,
- Silence notifications from non-contacts.

*Cherrygram Camera Preferences:*
- Camera Type (Default, CameraX or System camera),
- Motion smoothness,
- Enable/Disable 16:9 resolution for videomessages,
- Rear camera by default for video messages,
- Disable camera in attachments menu.

*Cherrygram Experimental Preferences:*
- Download speed boost - Thanks to Nekogram for the idea :),
- Upload speed boost - Thanks to Nekogram for the idea :),
- Slow network mode - Thanks to Telegraher for the idea :),

*Cherrygram Security Preferences:*
- "Kaboom" (Erase all Cherrygram data in 1 click) - Thanks to Telegraher :),
- Delete old cache folder (/sdcard/Telegram),
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
- [exteraGram](https://github.com/exteraSquad/exteraGram)
- [Nekogram](https://gitlab.com/Nekogram/Nekogram)
- [Nekogram X](https://github.com/NekoX-Dev/NekoX)
- [OwlGram](https://github.com/OwlGramDev/OwlGram)
- [Telegram-FOSS](https://github.com/Telegram-FOSS-Team/Telegram-FOSS)
- [Telegram Monet](https://github.com/c3r5b8/Telegram-Monet)
