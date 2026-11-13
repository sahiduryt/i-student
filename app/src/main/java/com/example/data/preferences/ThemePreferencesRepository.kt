package com.example.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.data.model.AccentPalette
import com.example.data.model.InterfaceDensity
import com.example.data.model.ThemeModePreference
import com.example.data.model.UserAppPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "istudent_prefs")

class ThemePreferencesRepository(private val context: Context) {

  val preferencesFlow: Flow<UserAppPreferences> = context.dataStore.data.map { prefs ->
    val mode = runCatching {
      ThemeModePreference.valueOf(prefs[KEY_THEME_MODE] ?: ThemeModePreference.SYSTEM.name)
    }.getOrDefault(ThemeModePreference.SYSTEM)
    val accent = runCatching {
      AccentPalette.valueOf(prefs[KEY_ACCENT] ?: AccentPalette.ROYAL_PURPLE.name)
    }.getOrDefault(AccentPalette.ROYAL_PURPLE)
    val density = runCatching {
      InterfaceDensity.valueOf(prefs[KEY_DENSITY] ?: InterfaceDensity.COMFORTABLE.name)
    }.getOrDefault(InterfaceDensity.COMFORTABLE)

    UserAppPreferences(
      themeMode = mode,
      accentPalette = accent,
      fontScale = prefs[KEY_FONT_SCALE] ?: 1.0f,
      density = density,
      highContrast = prefs[KEY_HIGH_CONTRAST] ?: false,
    )
  }

  suspend fun setThemeMode(mode: ThemeModePreference) {
    context.dataStore.edit { it[KEY_THEME_MODE] = mode.name }
  }

  suspend fun setAccentPalette(accent: AccentPalette) {
    context.dataStore.edit { it[KEY_ACCENT] = accent.name }
  }

  suspend fun setFontScale(scale: Float) {
    context.dataStore.edit { it[KEY_FONT_SCALE] = scale.coerceIn(0.85f, 1.3f) }
  }

  suspend fun setDensity(density: InterfaceDensity) {
    context.dataStore.edit { it[KEY_DENSITY] = density.name }
  }

  suspend fun setHighContrast(enabled: Boolean) {
    context.dataStore.edit { it[KEY_HIGH_CONTRAST] = enabled }
  }

  private companion object {
    val KEY_THEME_MODE = stringPreferencesKey("theme_mode")
    val KEY_ACCENT = stringPreferencesKey("accent_palette")
    val KEY_FONT_SCALE = floatPreferencesKey("font_scale")
    val KEY_DENSITY = stringPreferencesKey("interface_density")
    val KEY_HIGH_CONTRAST = booleanPreferencesKey("high_contrast")
  }
}
