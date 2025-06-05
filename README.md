# 🍒 Cherrygram

Cherrygram is a third-party Telegram client with not many but useful modifications.

This is an unofficial fork of the original [Telegram App for Android](https://github.com/DrKLO/Telegram).

This repo contains the official source code for [Telegram App for Android](https://play.google.com/store/apps/details?id=org.telegram.messenger).

## Current Maintainers

- [arsLan4k1390](https://github.com/arsLan4k1390)
- You? :)

## Contributors

- [arsLan4k1390](https://github.com/arsLan4k1390)


## Discussion

Join the [Cherrygram official channel](https://t.me/Cherry_gram)

Join the [Cherrygram official group](https://t.me/CherrygramSupport)

## Changes:

*Addons:*
- Gemini AI,
- Flashlight support for video messages (both cameras),
- OTA updates,
- Material You (Monet) themes and app icon,
- No content restrictions,
- Support of up to 10 accounts,
- Profile pictures (both normal and public) upload date,
- IOS/TDesktop style of latest activity status,
- Fetching emoji and stickers from profile pictures (both normal and public),
- Fetching emoji from profile background,
- Mutual contact icon in contacts list,
- Allow to set a proxy before login,
- Added native biometrics support,
- "tg://user?id=int" Links support (e.g. "tg://user?id=282287840"),
- Allow to set "Keep media" for one day,
- "Kaboom" (Erase all Cherrygram's data in 1 click),
- "Kaboom" home screen widget.

*Chats:*
- Scrollable and clickable chat preview,
- Blocking stickers (which cause Telegram apps crash),
- Open profile from chat preview,
- "Mark as read" support for folders,
- Chat Blur support for all devices and themes (Blur is enabled by default),
- Fast search (Open a search by holding dialog name),
- Jump to the beginning of any chat,
- Delete all OWN messages from groups,
- "Remove file from cache" feature for files in chat,
- Copying photos to clipboard,
- Stickers downloader,
- Messages history from any user and channel in any chat,
- Select messages in chat between message A and message B,
- View admins (Group info) for all members,
- Open avatar by swiping down in profile,
- Mention in chat by name (long press on username).

*Cherrygram General Preferences:*
- Enable/Disable members rounding,
- Enable/Disable system emoji,
- Enable/Disable system fonts,
- Default notification icon (Telegram),
- Tablet mode,
- Enable/Disable Telegram Stories in app header,
- Automatically archive stories from contacts and channels,
- Enable/Disable animated avatars,
- Enable/Disable reactions overlay or reactions animation,
- Enable/Disable taps on premium stickers,
- Enable/Disable premium stickers auto-play,
- Enable/Disable "Send as channel" button.

*Cherrygram Appearance Preferences:*
- VKUI/Solar icons in app,
- One UI (Samsung) Switchers style,
- Center title in action bar,
- Enable/Disable toolbar shadow or dividers,
- Overriding header color,
- Snow in App Header and Chats.

*Messages and profiles Preferences:*
- Show seconds in timestamps,
- Enable/Disable premium statuses,
- Customize reply and profile background,
- Show/Hide Personal channel in profile,
- Show/Hide DC/ID in profile,
- Show/Hide Birth Date in profile,
- Show/Hide Business hours and location in profile.

*Folders Preferences:*
- Show folder name instead of app name,
- Hide "All chats" tab,
- Hide messages counter in tabs.
- Tab style (Default, Rounded, Text, VKUI or Pills),
- Tab icon style (Only titles, Only icons and Icons with titles) with stroke.

*Drawer Preferences:*
- Snow in drawer,
- Profile photo as drawer background,
- Darken menu header background,
- Gradient menu header background,
- Blur menu header background and blur intensity,
- Enable/Disable drawer buttons,
- Drawer icons set.

*Cherrygram Chats Preferences:*
- Configure admin shortcuts in chats,
- Center chat title like on IOS,
- Showing unread chats counter on "Back" button like on IOS,
- Ask before a call,
- Hide keyboard while scrolling a chat,
- Enable/Disable transition to the next channel,
- Show/Hide bottom button in channels (Mute/Unmute),
- Direct Share button (Select where to show the button),
- Configure message menu,
- Configure messages bubble size (Photos/Videos, Stickers and GIFs),
- Hide time on stickers,
- Use "Delete for all" by default,
- Show forwarded message date,
- Pencil icon for edited messages instead of "edited",
- Left button action (Forward w/o authorship, Direct Share, Reply or Save),
- Double tap action (Disable, Reaction, Reply, Save/Edit/Translate a message),
- Message swipe action (Reply, Save, Translate or Direct Share),
- Large photos (2560px),
- Spoiler effect,
- Voice enhancements,
- Enable/Disable playing video on volume button click,
- Auto pause video while switching to the background,
- Disable in-app vibration,
- Double tap to seek videos duration,
- Enable/Disable incoming message sound or choose between IOS and Android,
- Enable/Disable vibration in chats and choose vibration intensity,
- Silence notifications from non-contacts.

*Cherrygram Camera Preferences:*
- Camera Type (Default, CameraX or System camera),
- Disable camera in attachments menu,
- Default camera for video messages,
- Custom camera aspect ratio for video-messages (1:1, 4:3 or 16:9),
- Camera stabilisation in videomessages,
- Exposure and zoom slider in videomessages.

*Cherrygram Experimental Preferences:*
- Enable/Disable spring animations for app navigation,
- Show a resident notification,
- Toast all RPC errors,
- Custom chat for Saved Messages,
- Download speed boost,
- Upload speed boost,
- Slow network mode.

*Cherrygram Security Preferences:*
- Enable/Disable Google Analytics,
- Delete old cache folder (/sdcard/Telegram),
- Delete your Telegram account,
- Hide archive from chats list,
- Require biometrics to open archive,
- Require biometrics to open specific chats,
- Require biometrics to delete chats.

## API, Protocol documentation

Telegram API manuals: https://core.telegram.org/api

MTproto protocol manuals: https://core.telegram.org/mtproto


## Compilation Guide

1. Download the Cherrygram source code ( `git clone https://github.com/arslan4k1390/Cherrygram.git` )
2. Fill out storeFile, storePassword, keyAlias, keyPassword in app's build.gradle to sign your app
3. Go to https://console.firebase.google.com/, create two android apps with application IDs uz.unnarsx.cherrygram and uz.unnarsx.cherrygram.beta, turn on firebase messaging and download `google-services.json`, which should be copied into `TMessagesProj` folder.
4. Open the project in the Studio (note that it should be opened, NOT imported).
5. Fill out values in `TMessagesProj/src/main/java/uz/unnarsx/cherrygram/Extra.kt` – there’s a link for each of the variables showing where and which data to obtain.
6. You are ready to compile and use Cherrygram.


## Thanks to:
- [Catogram](https://github.com/Catogram/Catogram) and [Nekogram](https://gitlab.com/Nekogram/Nekogram)
- [exteraGram](https://github.com/exteraSquad/exteraGram) and [OwlGram](https://github.com/OwlGramDev/OwlGram)
- [Telegraher](https://github.com/nikitasius/Telegraher) and [Telegram Monet](https://github.com/c3r5b8/Telegram-Monet)
