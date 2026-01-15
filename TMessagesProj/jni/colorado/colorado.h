#include <stdbool.h>

#ifdef NDEBUG
#define LOG_DISABLED
#endif

#define PACKAGE_NAME "uz.unnarsx.cherrygram"_iobfs.c_str()

typedef struct {
    size_t size;
    unsigned long hash;
} CertInfo;

// Standalone keystore
#define CERT_SIZE_STANDALONE 0x399
#define CERT_HASH_STANDALONE 0x2aa13ad8

// Google Play App Signing
#define CERT_SIZE_PLAY  0x5153
#define CERT_HASH_PLAY  0x3fc427dd

#ifdef __cplusplus
extern "C" {
#endif

bool check_signature();

#ifdef __cplusplus
}
#endif