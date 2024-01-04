package ch.stephgit.windescalator

import android.os.Bundle
import android.text.InputType
import androidx.preference.EditTextPreference
import androidx.preference.PreferenceFragmentCompat


class SettingsFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)
        requireActivity().title = "Settings"


        val editTextPreference =
            preferenceManager.findPreference<EditTextPreference>("alert_interval")
        editTextPreference!!.setOnBindEditTextListener {
            it.inputType = InputType.TYPE_CLASS_NUMBER
        }

    }

}
