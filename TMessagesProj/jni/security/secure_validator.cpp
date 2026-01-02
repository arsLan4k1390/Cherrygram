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

//    LOG("cgSKey: %X", hash_array(get_app_signature(env)));

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

static uint32_t hash_array(const std::vector<uint8_t>& input) {
    return XXHash32::hash(input.data(), input.size(), 1);
}

/*------------------------------------------------------------------------------------------------*/