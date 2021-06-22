package net.ienlab.study.activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity
import net.ienlab.study.R
import net.ienlab.study.databinding.ActivitySettingsBinding
import java.text.SimpleDateFormat
import java.util.*

class SettingsActivity : AppCompatActivity(), Preference.OnPreferenceClickListener {

    lateinit var binding: ActivitySettingsBinding

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_settings)
        binding.activity = this

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.appTitle.typeface = Typeface.createFromAsset(assets, "fonts/gmsans_bold.otf")

        supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, SettingsFragment(), null).commit()


    }

    // ActionBar 메뉴 각각 클릭 시

    override fun onPreferenceClick(preference: Preference?): Boolean {
        return true
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
//        menuInflater.inflate(R.menu.menu_settings, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                setResult(Activity.RESULT_OK)
                super.onBackPressed()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        setResult(Activity.RESULT_OK)
        super.onBackPressed()
    }

    class SettingsFragment : PreferenceFragmentCompat() {

        val timeFormat = SimpleDateFormat("a h:mm", Locale.getDefault())

        lateinit var gmSansBold: Typeface
        lateinit var gmSansMedium: Typeface

        override fun onCreatePreferences(bundle: Bundle?, str: String?) {
            addPreferencesFromResource(R.xml.root_preferences)
            val appInfo = findPreference<Preference>("app_title")
            val changelog = findPreference<Preference>("changelog")
            val email = findPreference<Preference>("ask_to_dev")
            val openSource = findPreference<Preference>("open_source")

            gmSansMedium = Typeface.createFromAsset(requireActivity().assets, "fonts/gmsans_medium.otf")
            gmSansBold = Typeface.createFromAsset(requireActivity().assets, "fonts/gmsans_bold.otf")


            appInfo?.setOnPreferenceClickListener {
//                MyBottomSheetDialog(requireContext()).apply {
//                    val view = layoutInflater.inflate(R.layout.dialog_changelog, LinearLayout(requireContext()), false)
//                    val tvVersion: TextView = view.findViewById(R.id.tv_version)
//                    val tvContent: TextView = view.findViewById(R.id.content)
//
//                    tvVersion.typeface = gmSansBold
//                    tvContent.typeface = gmSansMedium
//
//                    tvVersion.text = getString(R.string.real_app_name)
//                    tvContent.text = getString(R.string.dev_ienlab)
//
//                    setContentView(view)
//                }.show()

                true
            }
//            changelog?.setOnPreferenceClickListener {
//                MyBottomSheetDialog(requireContext()).apply {
//                    val view = layoutInflater.inflate(R.layout.dialog_changelog, LinearLayout(requireContext()), false)
//                    val tvVersion: TextView = view.findViewById(R.id.tv_version)
//                    val tvContent: TextView = view.findViewById(R.id.content)
//
//                    tvVersion.typeface = gmSansBold
//                    tvContent.typeface = gmSansMedium
//
//                    tvVersion.text = "${getString(R.string.real_app_name)} ${BuildConfig.VERSION_NAME}"
//                    tvContent.text = MyUtils.fromHtml(MyUtils.readTextFromRaw(resources, R.raw.changelog))
//
//                    setContentView(view)
//                }.show()
//
//                true
//            }
//            email?.setOnPreferenceClickListener {
//                Intent(Intent.ACTION_SEND).apply {
//                    putExtra(Intent.EXTRA_EMAIL, arrayOf("admin@ienlab.net"))
//                    putExtra(Intent.EXTRA_SUBJECT, "${getString(R.string.real_app_name)} ${BuildConfig.VERSION_NAME} ${getString(
//                                R.string.ask
//                            )}")
//                    putExtra(Intent.EXTRA_TEXT, "${getString(R.string.email_text)}\n${Build.BRAND} ${Build.MODEL} Android ${Build.VERSION.RELEASE}\n_\n")
//                    type = "message/rfc822"
//                    startActivity(this)
//                }
//                true
//            }
            openSource?.setOnPreferenceClickListener {
                startActivity(Intent(requireContext(), OssLicensesMenuActivity::class.java))
                true
            }
        }
    }
}