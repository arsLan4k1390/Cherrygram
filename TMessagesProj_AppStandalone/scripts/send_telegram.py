import json
import os
import sys
from pyrogram import Client
from pyrogram.enums import ParseMode
from pyrogram.types import InputMediaDocument

def main():
    api_id = int(os.environ["TG_APP_ID"])
    api_hash = os.environ["TG_APP_HASH"]
    bot_token = os.environ["TELEGRAM_BOT_TOKEN"]
    chat_id_raw = os.environ["TELEGRAM_CHAT_ID"]
    chat_id = int(chat_id_raw) if chat_id_raw.lstrip("-").isdigit() else chat_id_raw

    manifest_path = sys.argv[1]  # путь к JSON-манифесту: [{"path": ..., "caption": ...}, ...]

    with open(manifest_path, "r", encoding="utf-8") as f:
        items = json.load(f)

    if not items:
        print("Nothing to send: manifest is empty")
        return

    # in_memory=True — не создаёт .session файл на диске,
    # для бот-токена это самый простой вариант для CI
    with Client(
        "sender",
        api_id=api_id,
        api_hash=api_hash,
        bot_token=bot_token,
        in_memory=True,
    ) as app:
        if len(items) == 1:
            # Telegram не позволяет альбом из одного элемента —
            # в этом случае просто шлём обычным документом
            item = items[0]
            app.send_document(
                chat_id,
                item["path"],
                caption=item["caption"],
                parse_mode=ParseMode.HTML,
            )
        else:
            media = [
                InputMediaDocument(
                    media=item["path"],
                    caption=item["caption"],
                    parse_mode=ParseMode.HTML,
                )
                for item in items
            ]
            app.send_media_group(chat_id, media)

    print(f"{len(items)} file(s) successfully sent via kurigram!")

if __name__ == "__main__":
    main()