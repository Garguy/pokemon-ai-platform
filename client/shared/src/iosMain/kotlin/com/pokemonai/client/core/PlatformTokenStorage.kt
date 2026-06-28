package com.pokemonai.client.core

import platform.Foundation.NSData
import platform.Foundation.NSString
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.create
import platform.Foundation.dataUsingEncoding
import platform.Security.SecItemAdd
import platform.Security.SecItemCopyMatching
import platform.Security.SecItemDelete
import platform.Security.SecItemUpdate
import platform.Security.kSecAttrAccount
import platform.Security.kSecAttrService
import platform.Security.kSecClass
import platform.Security.kSecClassGenericPassword
import platform.Security.kSecMatchLimit
import platform.Security.kSecMatchLimitOne
import platform.Security.kSecReturnData
import platform.Security.kSecValueData
import kotlinx.cinterop.ExperimentalForeignApi

@OptIn(ExperimentalForeignApi::class)
class PlatformTokenStorage : TokenStorage {
    private val service = "com.pokemonai.client"
    private val account = "auth_token"

    private fun query(): Map<Any?, Any?> = mapOf(
        kSecClass to kSecClassGenericPassword,
        kSecAttrService to service,
        kSecAttrAccount to account,
    )

    override fun save(token: String) {
        val data = NSString.create(string = token).dataUsingEncoding(NSUTF8StringEncoding) ?: return
        SecItemDelete(query())
        val addQuery = query() + (kSecValueData to data)
        SecItemAdd(addQuery, null)
    }

    override fun load(): String? {
        val fetchQuery = query() + mapOf(
            kSecReturnData to true,
            kSecMatchLimit to kSecMatchLimitOne,
        )
        val result = kotlinx.cinterop.memScoped {
            val ref = kotlinx.cinterop.alloc<kotlinx.cinterop.ObjCObjectVar<Any?>>()
            SecItemCopyMatching(fetchQuery, ref.ptr)
            ref.value
        }
        val data = result as? NSData ?: return null
        return NSString.create(data = data, encoding = NSUTF8StringEncoding)?.toString()
    }

    override fun clear() {
        SecItemDelete(query())
    }
}
