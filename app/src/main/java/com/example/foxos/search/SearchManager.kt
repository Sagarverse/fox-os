package com.example.foxos.search

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.provider.ContactsContract
import android.provider.Settings
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.foxos.model.AppInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import kotlinx.coroutines.coroutineScope

enum class SearchResultType {
    APP, SETTING, CONTACT, WHATSAPP, INSTAGRAM, WEB
}

data class SearchResult(
    val title: String,
    val subtitle: String,
    val type: SearchResultType,
    val iconVector: ImageVector? = null,
    val iconBitmap: Bitmap? = null,
    val packageName: String? = null,
    val intent: Intent? = null,
    val action: () -> Unit = {},
    val settingKey: String? = null
)

class SearchManager(private val context: Context) {

    suspend fun search(query: String, installedApps: List<AppInfo>): List<SearchResult> = coroutineScope {
        if (query.isBlank()) return@coroutineScope emptyList()

        val queryLower = query.lowercase()

        val appsDeferred = async(Dispatchers.IO) {
            installedApps.filter { 
                it.label.lowercase().contains(queryLower) || it.packageName.lowercase().contains(queryLower) 
            }.map { app ->
                SearchResult(
                    title = app.label,
                    subtitle = "App",
                    type = SearchResultType.APP,
                    packageName = app.packageName
                )
            }
        }

        val settingsDeferred = async(Dispatchers.IO) {
            val settingsShortcuts = listOf(
                Triple("Wifi", Settings.ACTION_WIFI_SETTINGS, "WIFI"),
                Triple("Bluetooth", Settings.ACTION_BLUETOOTH_SETTINGS, "BLUETOOTH"),
                Triple("Location", Settings.ACTION_LOCATION_SOURCE_SETTINGS, "LOCATION"),
                Triple("Display", Settings.ACTION_DISPLAY_SETTINGS, null),
                Triple("Battery", Settings.ACTION_BATTERY_SAVER_SETTINGS, null),
                Triple("Storage", Settings.ACTION_INTERNAL_STORAGE_SETTINGS, null),
                Triple("Sound", Settings.ACTION_SOUND_SETTINGS, null),
                Triple("Apps", Settings.ACTION_APPLICATION_SETTINGS, null),
                Triple("Security", Settings.ACTION_SECURITY_SETTINGS, null),
                Triple("About", Settings.ACTION_DEVICE_INFO_SETTINGS, null),
                Triple("Language", Settings.ACTION_LOCALE_SETTINGS, null),
                Triple("Date", Settings.ACTION_DATE_SETTINGS, null),
                Triple("Network", Settings.ACTION_WIRELESS_SETTINGS, null),
                Triple("Privacy", Settings.ACTION_PRIVACY_SETTINGS, null),
                Triple("Accessibility", Settings.ACTION_ACCESSIBILITY_SETTINGS, null),
                Triple("Developers", Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS, null)
            )
            settingsShortcuts.filter { it.first.lowercase().contains(queryLower) }.map { (name, action, key) ->
                SearchResult(
                    title = name,
                    subtitle = "System Setting",
                    type = SearchResultType.SETTING,
                    intent = Intent(action).apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK },
                    settingKey = key
                )
            }
        }

        val contactsDeferred = async(Dispatchers.IO) {
            val contactResults = mutableListOf<SearchResult>()
            try {
                val cursor = context.contentResolver.query(
                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                    arrayOf(
                        ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                        ContactsContract.CommonDataKinds.Phone.NUMBER
                    ),
                    "${ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME} LIKE ?",
                    arrayOf("%$query%"),
                    null
                )

                cursor?.use {
                    val nameIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                    val numberIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                    
                    var count = 0
                    while (it.moveToNext() && count < 5) {
                        val name = it.getString(nameIndex)
                        val number = it.getString(numberIndex)
                        contactResults.add(
                            SearchResult(
                                title = name,
                                subtitle = "Contact • $number",
                                type = SearchResultType.CONTACT,
                                intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$number")).apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK }
                            )
                        )
                        
                        contactResults.add(
                            SearchResult(
                                title = "Message $name",
                                subtitle = "WhatsApp",
                                type = SearchResultType.WHATSAPP,
                                intent = Intent(Intent.ACTION_VIEW).apply {
                                    data = Uri.parse("https://api.whatsapp.com/send?phone=$number")
                                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                }
                            )
                        )
                        count++
                    }
                }
            } catch (e: Exception) {
                // Ignore errors
            }
            contactResults
        }

        val socialDeferred = async(Dispatchers.IO) {
            if (query.startsWith("@")) {
                val handle = query.substring(1)
                listOf(
                    SearchResult(
                        title = "Instagram: @$handle",
                        subtitle = "Open Profile",
                        type = SearchResultType.INSTAGRAM,
                        intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://instagram.com/_u/$handle")).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            setPackage("com.instagram.android")
                        }
                    )
                )
            } else emptyList()
        }

        val allResults = awaitAll(appsDeferred, settingsDeferred, contactsDeferred, socialDeferred).flatten().toMutableList()
        
        // 5. Web Search Fallback
        if (query.isNotBlank()) {
            allResults.add(
                SearchResult(
                    title = "Search Web: $query",
                    subtitle = "Open in Browser",
                    type = SearchResultType.WEB,
                    intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/search?q=$query")).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                )
            )
        }

        // Simple ranking: exact app matches first, then app partials, then others
        allResults.sortedByDescending { result ->
            when {
                result.type == SearchResultType.APP && result.title.equals(query, ignoreCase = true) -> 100
                result.type == SearchResultType.APP -> 80
                result.type == SearchResultType.SETTING && result.title.equals(query, ignoreCase = true) -> 70
                result.type == SearchResultType.WEB -> -10 // Always at bottom
                else -> 0
            }
        }
    }
}
