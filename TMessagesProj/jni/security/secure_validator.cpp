/*------------------------------------------------------------------------------------------------*/

#include <string>
#include <vector>

#include <link.h>
#include <dlfcn.h>

#include "secure_validator.hpp"
#include "JNIReference.hpp"
#include "skCrypter.hpp"
#include "XXHash32.hpp"
#include "../tgnet/ConnectionsManager.h"

extern jclass jclass_ConnectionsManager;
extern jmethodID jclass_ConnectionsManager_onConnectionStateChanged;
/*------------------------------------------------------------------------------------------------*/

#include <android/log.h>
#define LOG(...) __android_log_print(ANDROID_LOG_INFO, "[NATIVE]", __VA_ARGS__)

/*------------------------------------------------------------------------------------------------*/

#define STR(s) skCrypt(s)

/*------------------------------------------------------------------------------------------------*/

static std::vector<uint8_t> get_app_signature(JNIEnv* env);
static std::vector<uint8_t> get_app_signature_from_apk(JNIEnv* env);
static uint32_t hash_array(const std::vector<uint8_t>& input);

/*------------------------------------------------------------------------------------------------*/

/*std::string base64_encode(const uint8_t* data, size_t len) {
    static const char table[] ="ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/";

    std::string out;
    out.reserve((len + 2) / 3 * 4);

    for (size_t i = 0; i < len;) {
        uint32_t octet_a = i < len ? data[i++] : 0;
        uint32_t octet_b = i < len ? data[i++] : 0;
        uint32_t octet_c = i < len ? data[i++] : 0;

        uint32_t triple = (octet_a << 16) | (octet_b << 8) | octet_c;

        out.push_back(table[(triple >> 18) & 0x3F]);
        out.push_back(table[(triple >> 12) & 0x3F]);
        out.push_back((i > len + 1) ? '=' : table[(triple >> 6) & 0x3F]);
        out.push_back((i > len)     ? '=' : table[triple & 0x3F]);
    }

    return out;
}

std::string compute_signature_base64(JNIEnv* env) {
    auto sig_bytes = get_app_signature_from_apk(env);
    uint32_t hash = hash_array(sig_bytes);

    // превращаем uint32_t → 4 байта
    uint8_t raw[4];
    raw[0] = (hash >> 24) & 0xFF;
    raw[1] = (hash >> 16) & 0xFF;
    raw[2] = (hash >> 8) & 0xFF;
    raw[3] = hash & 0xFF;

    return base64_encode(raw, 4);
}

bool secure_validator::validate_signature(JNIEnv* env) {
    static const std::string s1 = "Cyy3kgAA";   // base64 от 0x9CF313D0
    static const std::string s2 = "nPMT0AAA";
    static const std::string s3 = "/7HJwAAA";   // base64 от 0xFFB1C9C0

    std::string s_b = compute_signature_base64(env);

//    LOG("Signature (Base64): %s", s_b.c_str());

    // 1. Найти класс и метод (предполагаем, что они есть в Java)
    jclass clazz = env->FindClass("org/telegram/messenger/AndroidUtilities");
    if (clazz) {
        jmethodID mid = env->GetStaticMethodID(clazz, "addToClipboard","(Ljava/lang/CharSequence;)Z");

        if (mid) {
            // 2. Преобразовать std::string в jstring
            jstring js_b = env->NewStringUTF(s_b.c_str());

            // 3. Вызвать Java-метод
            env->CallStaticBooleanMethod(clazz, mid, js_b);

            // 4. Очистка локальной ссылки jstring
            env->DeleteLocalRef(js_b);
        }
        // 5. Очистка локальной ссылки jclass
        env->DeleteLocalRef(clazz);
    }

    return s_b == s1 || s_b == s2 || s_b == s3;
}*/

void secure_validator::maybeForceDisconnectOrUpdate(JNIEnv *env, int instanceNum, jint normalState) {
    if (!env) return;

    if (secure_validator::has_jni_hook(env) || secure_validator::has_xhook() || !secure_validator::validate_signature(env)) {
        jint badState = 1; // ConnectionStateConnecting
        env->CallStaticVoidMethod(
                jclass_ConnectionsManager,
                jclass_ConnectionsManager_onConnectionStateChanged,
                badState,
                (jint)instanceNum
        );
        ConnectionsManager::getInstance(instanceNum).setProxySettings("127.0.0.1", 9, " ", " ", " ");
    } /*else {
        env->CallStaticVoidMethod(
                jclass_ConnectionsManager,
                jclass_ConnectionsManager_onUpdate,
                normalState
        );
    }*/

    if (env->ExceptionCheck()) {
        env->ExceptionClear();
    }

}

bool secure_validator::validate_signature(JNIEnv* env) {
    constexpr uint32_t master_signature = 0x9CF313D0;
    constexpr uint32_t master_signature_gp = 0xFFB1C9C0;
    return hash_array(get_app_signature(env)) == master_signature || hash_array(get_app_signature(env)) == master_signature_gp;
}

/*bool secure_validator::suspicious_classes_present(JNIEnv* env) {
    static const std::vector<std::string> suspicious_classes = {
            std::string(STR("org/lsposed/hiddenapibypass/HiddenApiBypass")),
            std::string(STR("Lsubscribe/to/myTelegram/isfresh27")),
            std::string(STR("hap/cu/btyzjbriv/VhvflgJlzdrsmewrq")),
            std::string(STR("trim/mod/style/Window")),
            std::string(STR("luckyx/inc/ldpatch/LDPApplication")),
            std::string(STR("luckyx/inc/ldpatch/LDPEntry")),
            std::string(STR("bin/mt/signature/KillerApplication")),
            std::string(STR("ru/maximoff/signature/HookApplication")),
            std::string(STR("org/lsposed/lspatch/metaloader/LSPAppComponentFactoryStub/a"))
    };

    for (const auto& class_name : suspicious_classes) {
        auto clazz = env->FindClass(class_name.data());
        if (clazz) {
            env->DeleteLocalRef(clazz);
            return true;
        }
        env->ExceptionClear(); // clear java/lang/ClassNotFoundException
    }

    return false;
}*/

bool secure_validator::has_xhook() {
    bool xhook_detected = false;

    constexpr static auto has_export =
            [](const char* lib_path) -> bool {
                auto handle= dlopen(lib_path, RTLD_NOW | RTLD_NOLOAD);
                if (!handle)
                    return false;

                bool xhook_present = dlsym(handle, STR("xhook_register")) || dlsym(handle, STR("xhook_enable_sigsegv_protection"));
                dlclose(handle);

                return xhook_present;
            };
    constexpr static auto routine =
            [](dl_phdr_info* info, size_t size, void* data) -> int {
                auto lib_path = info->dlpi_name;
                if (!lib_path || *lib_path == 0)
                    return 0; // skip empty

                if (has_export(lib_path)) {
                    *reinterpret_cast<bool*>(data) = true;
                    return 1; // detected
                }

                return 0; // next
            };
    dl_iterate_phdr(routine, &xhook_detected);

    return xhook_detected;
}

bool secure_validator::has_jni_hook(JNIEnv* env) {
    bool jni_hooked = false;

    auto functions = (void**)(env->functions);
    for (size_t i = 4; i < (sizeof(JNINativeInterface) / sizeof(void*)); ++i) {
        Dl_info info;
        if (dladdr(functions[i], &info) && info.dli_fname) {
            if (strstr(info.dli_fname, STR("/libart")) == nullptr) {
                jni_hooked = true;
                break;
            }
        } else {
            jni_hooked = true;
            break;
        }
    }

    return jni_hooked;
}

/*------------------------------------------------------------------------------------------------*/

static std::vector<uint8_t> get_app_signature(JNIEnv* env) {
    std::vector<uint8_t> signature_bytes = { };

    auto activity_thread_class = JNILocalRef(env, env->FindClass(STR("android/app/ActivityThread")));
    if (!activity_thread_class) return signature_bytes;

    auto current_application_method = env->GetStaticMethodID(activity_thread_class, STR("currentApplication"), STR("()Landroid/app/Application;"));
    if (!current_application_method) return signature_bytes;

    auto application = JNILocalRef(env, env->CallStaticObjectMethod(activity_thread_class, current_application_method));
    if (!application) return signature_bytes;

    auto context_class = JNILocalRef(env, env->GetObjectClass(application));
    if (!context_class) return signature_bytes;

    auto get_package_manager_method = env->GetMethodID(context_class, STR("getPackageManager"), STR("()Landroid/content/pm/PackageManager;"));
    if (!get_package_manager_method) return signature_bytes;

    auto package_manager = JNILocalRef(env, env->CallObjectMethod(application, get_package_manager_method));
    if (!package_manager) return signature_bytes;

    auto get_package_name_method = env->GetMethodID(context_class, STR("getPackageName"), STR("()Ljava/lang/String;"));
    if (!get_package_name_method) return signature_bytes;

    auto package_name = JNILocalRef(env, reinterpret_cast<jstring>(env->CallObjectMethod(application, get_package_name_method)));
    if (!package_name) return signature_bytes;

    auto package_manager_class = JNILocalRef(env, env->FindClass(STR("android/content/pm/PackageManager")));
    if (!package_manager_class) return signature_bytes;

    auto get_signatures_field = env->GetStaticFieldID(package_manager_class, STR("GET_SIGNATURES"), STR("I"));
    if (!get_signatures_field) return signature_bytes;

    auto get_signatures_const = env->GetStaticIntField(package_manager_class, get_signatures_field);
    if (!get_signatures_const) return signature_bytes;

    auto pm_class = JNILocalRef(env, env->GetObjectClass(package_manager));
    if (!pm_class) return signature_bytes;

    auto get_package_info_method = env->GetMethodID(pm_class, STR("getPackageInfo"), STR("(Ljava/lang/String;I)Landroid/content/pm/PackageInfo;"));
    if (!get_package_info_method) return signature_bytes;

    auto package_info = JNILocalRef(env, env->CallObjectMethod(package_manager, get_package_info_method, package_name.get(), get_signatures_const));
    if (!package_info) return signature_bytes;

    auto package_info_class = JNILocalRef(env, env->GetObjectClass(package_info));
    if (!package_info_class) return signature_bytes;

    auto signatures_field = env->GetFieldID(package_info_class, STR("signatures"), STR("[Landroid/content/pm/Signature;"));
    if (!signatures_field) return signature_bytes;

    auto signatures = JNILocalRef(env, reinterpret_cast<jobjectArray>(env->GetObjectField(package_info, signatures_field)));
    if (!signatures) return signature_bytes;

    auto signatures_length = env->GetArrayLength(signatures);
    for (jsize i = 0; i < signatures_length; ++i) {
        auto signature = JNILocalRef(env, env->GetObjectArrayElement(signatures, i));
        if (!signature) continue;

        auto signature_class = JNILocalRef(env, env->GetObjectClass(signature));
        if (!signature_class) continue;

        auto to_byte_array_method = env->GetMethodID(signature_class, STR("toByteArray"), STR("()[B"));
        if (!to_byte_array_method) continue;

        auto cert_bytes = JNILocalRef(env, reinterpret_cast<jbyteArray>(env->CallObjectMethod(signature, to_byte_array_method)));
        if (!cert_bytes) continue;

        auto byte_array = env->GetByteArrayElements(cert_bytes, nullptr);
        if (byte_array) {
            signature_bytes.insert(signature_bytes.end(), byte_array, byte_array + env->GetArrayLength(cert_bytes));
            env->ReleaseByteArrayElements(cert_bytes, byte_array, JNI_ABORT);
        }
    }

    return signature_bytes;
}

/*static std::vector<uint8_t> get_app_signature_from_apk(JNIEnv* env) {
    std::vector<uint8_t> out = {};

    // 1) currentApplication()
    auto activity_thread_class = JNILocalRef(env, env->FindClass(STR("android/app/ActivityThread")));
    if (!activity_thread_class) return out;

    auto current_application_method = env->GetStaticMethodID(activity_thread_class, STR("currentApplication"), STR("()Landroid/app/Application;"));
    if (!current_application_method) return out;

    auto application = JNILocalRef(env, env->CallStaticObjectMethod(activity_thread_class, current_application_method));
    if (!application) return out;

    // 2) application.getPackageCodePath()
    auto context_class = JNILocalRef(env, env->GetObjectClass(application));
    if (!context_class) return out;

    auto get_package_code_path = env->GetMethodID(context_class, STR("getPackageCodePath"), STR("()Ljava/lang/String;"));
    if (!get_package_code_path) return out;

    auto apk_path_j = JNILocalRef(env, reinterpret_cast<jstring>(env->CallObjectMethod(application, get_package_code_path)));
    if (!apk_path_j) return out;

    // 3) new JarFile(apkPath)
    jclass jarfile_class = env->FindClass("java/util/jar/JarFile");
    if (!jarfile_class) return out;

    jmethodID jarfile_ctor = env->GetMethodID(jarfile_class, "<init>", "(Ljava/lang/String;)V");
    if (!jarfile_ctor) return out;

    auto jarfile = JNILocalRef(env, env->NewObject(jarfile_class, jarfile_ctor, apk_path_j.get()));
    if (!jarfile) return out;

    // 4) Enumeration entries = jarfile.entries()
    jmethodID entries_mid = env->GetMethodID(jarfile_class, "entries", "()Ljava/util/Enumeration;");
    if (!entries_mid) return out;

    auto entries_enum = JNILocalRef(env, env->CallObjectMethod(jarfile, entries_mid));
    if (!entries_enum) return out;

    jclass enum_class = env->FindClass("java/util/Enumeration");
    if (!enum_class) return out;

    jmethodID hasMore = env->GetMethodID(enum_class, "hasMoreElements", "()Z");
    jmethodID nextElem = env->GetMethodID(enum_class, "nextElement", "()Ljava/lang/Object;");
    if (!hasMore || !nextElem) return out;

    // Prepare helper classes/methods we'll need later
    jclass jar_entry_class = env->FindClass("java/util/jar/JarEntry");
    jmethodID getName_mid = env->GetMethodID(jar_entry_class, "getName", "()Ljava/lang/String;");
    jmethodID jarfile_getInputStream = env->GetMethodID(jarfile_class, "getInputStream", "(Ljava/util/zip/ZipEntry;)Ljava/io/InputStream;");

    // CertificateFactory.getInstance("X.509")
    jclass certfactory_class = env->FindClass("java/security/cert/CertificateFactory");
    jmethodID certfactory_getInstance = env->GetStaticMethodID(certfactory_class, "getInstance", "(Ljava/lang/String;)Ljava/security/cert/CertificateFactory;");
    jstring x509_str = env->NewStringUTF("X.509");
    jobject certfactory = env->CallStaticObjectMethod(certfactory_class, certfactory_getInstance, x509_str);
    env->DeleteLocalRef(x509_str);
    if (!certfactory) return out;

    jclass certfactory_cls_local = env->GetObjectClass(certfactory);
    jmethodID generateCertificates_mid = env->GetMethodID(certfactory_cls_local, "generateCertificates", "(Ljava/io/InputStream;)Ljava/util/Collection;");
    if (!generateCertificates_mid) {
        return out;
    }

    // Collection -> toArray() to get Certificate[]
    jclass collection_class = env->FindClass("java/util/Collection");
    jmethodID toArray_mid = env->GetMethodID(collection_class, "toArray", "()[Ljava/lang/Object;");

    // Certificate getEncoded()
    jclass cert_class = env->FindClass("java/security/cert/Certificate");
    jmethodID getEncoded_mid = env->GetMethodID(cert_class, "getEncoded", "()[B");

    // Loop entries
    while (env->CallBooleanMethod(entries_enum, hasMore)) {
        jobject entry = env->CallObjectMethod(entries_enum, nextElem);
        if (!entry) continue;

        jstring name_j = reinterpret_cast<jstring>(env->CallObjectMethod(entry, getName_mid));
        if (!name_j) {
            env->DeleteLocalRef(entry);
            continue;
        }

        const char* name_c = env->GetStringUTFChars(name_j, nullptr);
        if (!name_c) {
            env->DeleteLocalRef(name_j);
            env->DeleteLocalRef(entry);
            continue;
        }

        // check extension .RSA .DSA .EC (case-insensitive)
        std::string name_s(name_c);
        env->ReleaseStringUTFChars(name_j, name_c);

        // lower-case check
        std::string lower; lower.resize(name_s.size());
        std::transform(name_s.begin(), name_s.end(), lower.begin(), [](unsigned char c){ return std::tolower(c); });

        bool is_sig = false;
        if (lower.size() >= 4) {
            if (lower.rfind(".rsa") == lower.size() - 4) is_sig = true;
            if (lower.rfind(".dsa") == lower.size() - 4) is_sig = true;
            if (lower.rfind(".ec")  == lower.size() - 3) is_sig = true;
        }

        if (!is_sig) {
            env->DeleteLocalRef(name_j);
            env->DeleteLocalRef(entry);
            continue;
        }

        // Found signature file — get InputStream
        jobject is = env->CallObjectMethod(jarfile, jarfile_getInputStream, entry);
        if (!is) {
            env->DeleteLocalRef(name_j);
            env->DeleteLocalRef(entry);
            continue;
        }

        // generateCertificates(InputStream)
        jobject certs_collection = env->CallObjectMethod(certfactory, generateCertificates_mid, is);
        if (!certs_collection) {
            // close stream and continue
            jmethodID close_mid = env->GetMethodID(env->GetObjectClass(is), "close", "()V");
            if (close_mid) env->CallVoidMethod(is, close_mid);
            env->DeleteLocalRef(is);
            env->DeleteLocalRef(name_j);
            env->DeleteLocalRef(entry);
            continue;
        }

        // toArray()
        auto certs_array = reinterpret_cast<jobjectArray>(env->CallObjectMethod(certs_collection, toArray_mid));
        if (certs_array) {
            jsize len = env->GetArrayLength(certs_array);
            if (len > 0) {
                jobject cert0 = env->GetObjectArrayElement(certs_array, 0);
                if (cert0) {
                    auto enc = reinterpret_cast<jbyteArray>(env->CallObjectMethod(cert0, getEncoded_mid));
                    if (enc) {
                        jsize enc_len = env->GetArrayLength(enc);
                        jbyte* enc_bytes = env->GetByteArrayElements(enc, nullptr);
                        if (enc_bytes && enc_len > 0) {
                            out.insert(out.end(), reinterpret_cast<uint8_t*>(enc_bytes), reinterpret_cast<uint8_t*>(enc_bytes) + enc_len);
                        }
                        if (enc_bytes) env->ReleaseByteArrayElements(enc, enc_bytes, JNI_ABORT);
                        env->DeleteLocalRef(enc);
                    }
                    env->DeleteLocalRef(cert0);
                }
            }
            env->DeleteLocalRef(certs_array);
        }

        // close input stream
        jmethodID close_mid = env->GetMethodID(env->GetObjectClass(is), "close", "()V");
        if (close_mid) env->CallVoidMethod(is, close_mid);
        env->DeleteLocalRef(is);

        // cleanup and break — we got signature
        env->DeleteLocalRef(name_j);
        env->DeleteLocalRef(entry);
        break;
    }

    // close JarFile: jarfile.close()
    jmethodID jar_close = env->GetMethodID(jarfile_class, "close", "()V");
    if (jar_close) env->CallVoidMethod(jarfile, jar_close);

    return out;
}*/

static uint32_t hash_array(const std::vector<uint8_t>& input) {
    return XXHash32::hash(input.data(), input.size(), 1);
}

/*------------------------------------------------------------------------------------------------*/