
package com.example.crownlogic

import android.content.Context
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.Json

private val Context.dataStore by preferencesDataStore("crownlogic")

class StateRepo(private val ctx: Context) {
  private val KEY = stringPreferencesKey("puzzle_json")

  suspend fun save(p: PuzzleState) {
    ctx.dataStore.edit { it[KEY] = Json.encodeToString(PuzzleState.serializer(), p) }
  }
  suspend fun load(): PuzzleState? {
    val prefs = ctx.dataStore.data.first()
    val txt = prefs[KEY] ?: return null
    return runCatching { Json.decodeFromString(PuzzleState.serializer(), txt) }.getOrNull()
  }
}
