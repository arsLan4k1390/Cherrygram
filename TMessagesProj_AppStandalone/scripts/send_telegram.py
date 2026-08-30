import os
import sys
from pyrogram import Client
from pyrogram.enums import ParseMode

def main():
    api_id = int(os.environ["TG_APP_ID"])
    api_hash = os.environ["TG_APP_HASH"]
    bot_token = os.environ["TELEGRAM_BOT_TOKEN"]
    chat_id_raw = os.environ["TELEGRAM_CHAT_ID"]
    chat_id = int(chat_id_raw) if chat_id_raw.lstrip("-").isdigit() else chat_id_raw

    file_path = sys.argv[1]
    caption_path = sys.argv[2]  # путь к файлу с текстом подписи

    with open(caption_path, "r", encoding="utf-8") as f:
        caption = f.read()

    # in_memory=True — не создаёт .session файл на диске,
    # для бот-токена это самый простой вариант для CI
    with Client(
        "sender",
        api_id=api_id,
        api_hash=api_hash,
        bot_token=bot_token,
        in_memory=True,
    ) as app:
        app.send_document(
            chat_id,
            file_path,
            caption=caption,
            parse_mode=ParseMode.HTML,
        )

    print("APK successfully sent via kurigram!")

if __name__ == "__main__":
    main()