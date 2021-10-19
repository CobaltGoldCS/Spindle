package com.cobaltware.webscraper.screens.settingsScreen

import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.preference.EditTextPreference
import androidx.preference.PreferenceFragmentCompat
import com.cobaltware.webscraper.R

class FragmentSettings : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val textSize = preferenceManager.findPreference<EditTextPreference>("textsize")!!
        setInputType(textSize, InputType.TYPE_CLASS_NUMBER)
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    @Suppress("SameParameterValue")
    private fun setInputType(preference: EditTextPreference, type: Int) {
        preference.setOnBindEditTextListener {
            it.inputType = type
        }
    }
}