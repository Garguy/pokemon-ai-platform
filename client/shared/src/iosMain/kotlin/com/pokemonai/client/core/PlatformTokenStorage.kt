package com.pokemonai.client.core

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.value
import platform.CoreFoundation.CFDictionaryCreateMutable
import platform.CoreFoundation.CFDictionaryAddValue
import platform.CoreFoundation.CFDictionaryRef
import platform.CoreFoundation.CFMutableDictionaryRef
import platform.CoreFoundation.CFStringRef
import platform.CoreFoundation.CFTypeRef
import platform.CoreFoundation.CFTypeRefVar
import platform.CoreFoundation.kCFAllocatorDefault
import platform.CoreFoundation.kCFBooleanTrue
import platform.Foundation.CFBridgingRelease
import platform.Foundation.CFBridgingRetain
import platform.Foundation.NSData
import platform.Foundation.NSString
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.create
import platform.Foundation.dataUsingEncoding
import platform.Security.SecItemAdd
import platform.Security.SecItemCopyMatching
import platform.Security.SecItemDelete
import platform.Security.kSecAttrAccount
import platform.Security.kSecAttrService
import platform.Security.kSecClass
import platform.Security.kSecClassGenericPassword
import platform.Security.kSecMatchLimit
import platform.Security.kSecMatchLimitOne
import platform.Security.kSecReturnData
import platform.Security.kSecValueData

@OptIn(ExperimentalForeignApi::class)
class PlatformTokenStorage : TokenStorage {
    private val service = "com.pokemonai.client"
    private val account = "auth_token"

    private fun buildQuery(extra: Map<Any?, Any?> = emptyMap()): CFMutableDictionaryRef? {
        val dict = CFDictionaryCreateMutable(kCFAllocatorDefault, 0, null, null) ?: return null
        CFDictionaryAddValue(dict, kSecClass, kSecClassGenericPassword)
        CFDictionaryAddValue(dict, kSecAttrService, CFBridgingRetain(service))
        CFDictionaryAddValue(dict, kSecAttrAccount, CFBridgingRetain(account))
        extra.forEach { (k, v) -> CFDictionaryAddValue(dict, k as CFTypeRef?, v as CFTypeRef?) }
        return dict
    }

    override fun save(token: String) {
        val data = NSString.create(string = token).dataUsingEncoding(NSUTF8StringEncoding) ?: return
        val deleteQuery = buildQuery() ?: return
        SecItemDelete(deleteQuery)
        val addQuery = buildQuery(mapOf(kSecValueData to CFBridgingRetain(data))) ?: return
        SecItemAdd(addQuery, null)
    }

    override fun load(): String? {
        val fetchQuery = buildQuery(mapOf(
            kSecReturnData to kCFBooleanTrue,
            kSecMatchLimit to kSecMatchLimitOne,
        )) ?: return null
        return memScoped {
            val result = alloc<CFTypeRefVar>()
            val status = SecItemCopyMatching(fetchQuery, result.ptr)
            if (status != 0) return@memScoped null
            val data = CFBridgingRelease(result.value) as? NSData ?: return@memScoped null
            NSString.create(data = data, encoding = NSUTF8StringEncoding)?.toString()
        }
    }

    override fun clear() {
        val query = buildQuery() ?: return
        SecItemDelete(query)
    }
}
