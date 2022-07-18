package jp.deadend.noname.skk

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import java.io.*
import jp.deadend.noname.dialog.SimpleMessageDialogFragment

class SKKSettingsActivity : AppCompatActivity(),
    PreferenceFragmentCompat.OnPreferenceStartFragmentCallback, Preference.OnPreferenceClickListener {

    private val loadFileActivity =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK) {
                result.data?.getStringExtra(FileChooser.KEY_FILEPATH)?.let { filepath ->
                    try {
                        // 一度読んでparseしてみて問題がないことを確認する
                        File(filepath).useLines { lines ->
                            if (lines.any { it.split("\t").size < 2 }) {
                                SimpleMessageDialogFragment.newInstance(
                                    getString(R.string.error_romajimap)
                                ).show(supportFragmentManager, "dialog")
                                return@let
                            }
                        }
                        File(filepath).copyTo(
                            File(filesDir, SKKService.FILENAME_ROMAJIMAP),
                            overwrite = true
                        )
                        val intent = Intent(SKKService.ACTION_COMMAND)
                        intent.putExtra(SKKService.KEY_COMMAND, SKKService.COMMAND_RELOAD_ROMAJIMAP)
                        sendBroadcast(intent)
                    } catch (e: IOException) {
                        SimpleMessageDialogFragment.newInstance(
                            getString(R.string.error_file_load, filepath)
                        ).show(supportFragmentManager, "dialog")
                    }
                }
            }
        }

    class SettingsMainFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.prefs_main, rootKey)

            val importRomajiMapPr = findPreference<Preference>(getString(R.string.prefkey_import_romajimap))
            importRomajiMapPr?.setOnPreferenceClickListener(activity as SKKSettingsActivity)
        }
    }
    class SettingsSoftKeyFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.prefs_softkey, null)
        }
    }
    class SettingsHardKeyFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.prefs_hardkey, null)
        }

        override fun onDisplayPreferenceDialog(preference: Preference) {
            if (preference is SetKeyPreference) {
                val dialogFragment = SetKeyDialogFragment.newInstance(preference.key)
                dialogFragment.setTargetFragment(this, 0)
                dialogFragment.show(parentFragmentManager, "dialog")
            } else {
                super.onDisplayPreferenceDialog(preference)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
//        title = getString(R.string.title_activity_settings)

        supportFragmentManager.beginTransaction()
                .replace(android.R.id.content, SettingsMainFragment())
                .commit()
    }

    override fun onPreferenceStartFragment(caller: PreferenceFragmentCompat, pref: Preference): Boolean {
        val args = pref.extras
        val className = pref.fragment ?: return false
        val fragment = supportFragmentManager.fragmentFactory.instantiate(classLoader, className)
        fragment.arguments = args
        fragment.setTargetFragment(caller, 0)
        supportFragmentManager.beginTransaction()
            .replace(android.R.id.content, fragment)
            .addToBackStack(null)
            .commit()
        title = pref.title

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                if (supportFragmentManager.backStackEntryCount == 0) {
                    setTitle(R.string.label_pref_activity)
                }
            }
            else -> return super.onOptionsItemSelected(item)
        }

        return true
    }

    override fun onPause() {
        super.onPause()

        val intent = Intent(SKKService.ACTION_COMMAND)
        intent.putExtra(SKKService.KEY_COMMAND, SKKService.COMMAND_READ_PREFS)
        sendBroadcast(intent)
    }

    override fun onPreferenceClick(preference: Preference): Boolean {
        val intent = Intent(this@SKKSettingsActivity, FileChooser::class.java)
        intent.putExtra(FileChooser.KEY_MODE, FileChooser.MODE_OPEN)
        loadFileActivity.launch(intent)
        return true
    }
}
