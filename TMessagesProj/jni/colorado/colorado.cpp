#include <string_view>
#include <dirent.h>
#include <unistd.h>
#include <zlib.h>

#include "colorado.h"
#include "logging.h"
#include "obfs-string.h"
#include "utils.h"
#include <fstream>
#include <iomanip>

void kill_self() {
    kill(getpid(), SIGKILL);
}

bool check_signature() {
    static const CertInfo validCerts[] = {
            { 0, CERT_HASH_STANDALONE },
            { 0, CERT_HASH_PLAY }
    };

    DIR *dir = opendir("/proc/self/fd"_iobfs.c_str());

    int dir_fd = dirfd(dir);
    struct dirent *ent;
    char buf[PATH_MAX];
    bool checked = false;

    while ((ent = readdir(dir)) != nullptr) {
        if (ent->d_name[0] == '.') continue;

        ssize_t len = readlinkat(dir_fd, ent->d_name, buf, PATH_MAX);
        if (len <= 0) continue;
        buf[len] = '\0';

        std::string_view real_path(buf, len);

        if (!starts_with(real_path, "/data/app/"_iobfs.c_str()) ||
            !ends_with(real_path, ".apk"_iobfs.c_str()) ||
            !contains(real_path, PACKAGE_NAME)) {
            continue;
        }

        std::string cert = read_certificate(atoi(ent->d_name));
        size_t size = cert.size();
        uLong crc = crc32(0, reinterpret_cast<const unsigned char *>(cert.data()), cert.size());

        bool valid = false;
        for (const auto &c : validCerts) {
            if (c.size == 0) {
                if (crc == c.hash) {
                    valid = true;
                    break;
                }
            } else {
                if (size == c.size && crc == c.hash) {
                    valid = true;
                    break;
                }
            }
        }

        if (valid) {
            checked = true;
#if !defined(NDEBUG)
            LOGE("colorado: success for %.*s", (int)len, buf);
#endif
            break;
        } else {
#if !defined(LOG_DISABLED)
            LOGE("colorado: mismatch for %.*s", (int)len, buf);
            for (const auto &c : validCerts) {
                LOGE("colorado: expected %zx:%lx, got %zx:%lx",
                     c.size, c.hash,
                     size, crc);
            }
#endif
            checked = false;
            kill_self();
            break;
        }
    }

    closedir(dir);

    if (!checked) {
        kill_self();
    }

    return checked;
}