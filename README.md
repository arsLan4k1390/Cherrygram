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

Join the [Cherrygram official channel](https://t.me/cherrygram)

Join the [Cherrygram official group](https://t.me/CherrygramSupport)


## API, Protocol documentation

Telegram API manuals: https://core.telegram.org/api

MTproto protocol manuals: https://core.telegram.org/mtproto


## Compilation Guide

1. Download the Cherrygram source code (`git clone https://github.com/arslan4k1390/Cherrygram.git`)
2. Fill out storeFile, storePassword, keyAlias, keyPassword in app's build.gradle to sign your app
3. Go to https://console.firebase.google.com/, create two android apps with application IDs uz.unnarsx.cherrygram and uz.unnarsx.cherrygram.beta, turn on firebase messaging and download `google-services.json`, which should be copied into `TMessagesProj` folder.
4. Open the project in the Studio (note that it should be opened, NOT imported).
5. Locate the files `TMessagesProj/src/main/java/uz/unnarsx/cherrygram/Extra.kt.example`, `TMessagesProj/src/main/java/uz/unnarsx/cherrygram/chats/ui/MessageMenuHelper.java.example` and `TMessagesProj/src/main/java/uz/unnarsx/cherrygram/chats/helpers/MessagesFilterHelper.kt.example`, then remove the .example extension from their filenames.
6. Fill out values in `TMessagesProj/src/main/java/uz/unnarsx/cherrygram/Extra.kt` – there’s a link for each of the variables showing where and which data to obtain.
7. In `TMessagesProj/jni/security/secure_validator.cpp`, update the `validate_signature` function with the values obtained from logs for you signing key (`cgSKey`). And not forget to disable other signature verification checks in `Extra.kt`.
8. You are ready to compile and use Cherrygram.


## Thanks to:
- [Catogram](https://github.com/Catogram/Catogram) and [Nekogram](https://gitlab.com/Nekogram/Nekogram)
- [exteraGram](https://github.com/exteraSquad/exteraGram) and [OwlGram](https://github.com/OwlGramDev/OwlGram)
- [Telegraher](https://github.com/nikitasius/Telegraher) and [Telegram Monet](https://github.com/c3r5b8/Telegram-Monet)
