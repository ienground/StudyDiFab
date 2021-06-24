package net.ienlab.study.activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Typeface
import android.os.*
import android.text.Editable
import android.text.TextWatcher
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity
import com.google.android.material.textfield.TextInputLayout
import net.ienlab.study.R
import net.ienlab.study.constant.SharedKey
import net.ienlab.study.databinding.ActivitySettingsBinding
import net.ienlab.study.utils.DeviceBTService
import net.ienlab.study.utils.MyBottomSheetDialog
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
        lateinit var sharedPreferences: SharedPreferences

        lateinit var gmSansBold: Typeface
        lateinit var gmSansMedium: Typeface

        override fun onCreatePreferences(bundle: Bundle?, str: String?) {
            addPreferencesFromResource(R.xml.root_preferences)
            val appInfo = findPreference<Preference>("app_title")
            val changelog = findPreference<Preference>("changelog")
            val email = findPreference<Preference>("ask_to_dev")
            val openSource = findPreference<Preference>("open_source")
            val connectDevice = findPreference<ListPreference>(SharedKey.CONNECT_DEVICE)
            val angle = findPreference<Preference>(SharedKey.NECK_ANGLE)

            gmSansMedium = Typeface.createFromAsset(requireActivity().assets, "fonts/gmsans_medium.otf")
            gmSansBold = Typeface.createFromAsset(requireActivity().assets, "fonts/gmsans_bold.otf")
            sharedPreferences = requireContext().getSharedPreferences("${requireActivity().packageName}_preferences", Context.MODE_PRIVATE)


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

            val btService = DeviceBTService(activity as Activity)
            if (btService.deviceState) { // 블루투스가 지원 가능한 기기일 때
                btService.enableBluetooth() // 블루투스 켜기
                val devices: ArrayList<String> = btService.BluetoothDevice()
                connectDevice?.entries = devices.toTypedArray<CharSequence>()
                connectDevice?.entryValues = devices.toTypedArray<CharSequence>()
                connectDevice?.summary = connectDevice?.value
            }

            connectDevice?.setOnPreferenceChangeListener { preference, newValue ->
                preference.summary = newValue as String
                true
            }
            connectDevice?.summary = connectDevice?.value

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

            angle?.summary = "${sharedPreferences.getInt(SharedKey.NECK_ANGLE, 15)}°"
            angle?.setOnPreferenceClickListener { preference ->
                MyBottomSheetDialog(requireContext()).apply {
                    val view = layoutInflater.inflate(R.layout.dialog_counter, LinearLayout(requireContext()), false)
                    val imgLogo: ImageView = view.findViewById(R.id.imgLogo)
                    val tvTitle: TextView = view.findViewById(R.id.tv_title)
                    val tvContent: TextView = view.findViewById(R.id.tv_content)
                    val btnLower: ImageButton = view.findViewById(R.id.btn_lower)
                    val btnHigher: ImageButton = view.findViewById(R.id.btn_higher)
                    val btnPositive: LinearLayout = view.findViewById(R.id.btn_positive)
                    val btnNegative: LinearLayout = view.findViewById(R.id.btn_negative)
                    val tvPositive: TextView = view.findViewById(R.id.btn_positive_text)
                    val tvNegative: TextView = view.findViewById(R.id.btn_negative_text)
                    val etValue: TextInputLayout = view.findViewById(R.id.et_value)

                    tvTitle.typeface = gmSansBold
                    tvContent.typeface = gmSansMedium
                    tvPositive.typeface = gmSansMedium
                    tvNegative.typeface = gmSansMedium
                    etValue.editText?.typeface = gmSansMedium

//                    imgLogo.setImageResource(R.drawable.ic_today)
//                    tvTitle.text = getString(R.string.intro_page2_title)
                    tvContent.visibility = View.GONE
                    etValue.editText?.setText(sharedPreferences.getInt(SharedKey.NECK_ANGLE, 15).toString())

                    sharedPreferences.getInt(SharedKey.NECK_ANGLE, 15).let {
                        if (it <= 1) {
                            btnLower.isEnabled = false
                            btnLower.alpha = 0.3f
                        }

                        if (it >= 85) {
                            btnHigher.isEnabled = false
                            btnHigher.alpha = 0.3f
                        }
                    }

                    class LowerLongClick: Handler(Looper.getMainLooper()) {
                        override fun handleMessage(msg: Message) {
                            if (etValue.editText?.text?.isNotEmpty() == true) {
                                val value = (etValue.editText?.text?.toString() ?: "1").toInt()
                                if (value > 1) {
                                    etValue.editText?.setText("${value - 1}")
                                    if (value - 1 <= 1) {
                                        btnLower.isEnabled = false
                                        btnLower.alpha = 0.3f
                                    }
                                    if (value - 1 < 85) {
                                        btnHigher.isEnabled = true
                                        btnHigher.alpha = 1f
                                    }
                                }
                            } else {
                                etValue.editText?.setText("1")
                            }
                            sendEmptyMessageDelayed(0, 100)
                        }
                    }
                    class HigherLongClick: Handler(Looper.getMainLooper()) {
                        override fun handleMessage(msg: Message) {
                            if (etValue.editText?.text?.isNotEmpty() == true) {
                                val value = (etValue.editText?.text?.toString() ?: "1").toInt()
                                if (value < 85) {
                                    etValue.editText?.setText("${value + 1}")
                                    if (value + 1 > 1) {
                                        btnLower.isEnabled = true
                                        btnLower.alpha = 1f
                                    }
                                    if (value + 1 >= 1000000) {
                                        btnHigher.isEnabled = false
                                        btnHigher.alpha = 0.3f
                                    }
                                }
                            } else {
                                etValue.editText?.setText("1")
                            }
                            sendEmptyMessageDelayed(0, 100)
                        }
                    }

                    val lowerHandler = LowerLongClick()
                    val higherHandler = HigherLongClick()

                    etValue.editText?.addTextChangedListener(object: TextWatcher {
                        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                        override fun afterTextChanged(s: Editable) {
                            if (s.isNotEmpty()) {
                                etValue.editText?.error = null
                                if (s.length >= 7 && s.toString() != "85") {
                                    etValue.editText?.setText("85")
                                    return
                                }
                                val value = s.toString().toInt()
                                if (value > 1) {
                                    btnLower.isEnabled = true
                                    btnLower.alpha = 1f
                                } else {
                                    btnLower.isEnabled = false
                                    btnLower.alpha = 0.3f
                                }

                                if (value < 85) {
                                    btnHigher.isEnabled = true
                                    btnHigher.alpha = 1f
                                } else {
                                    btnHigher.isEnabled = false
                                    btnHigher.alpha = 0.3f
                                }
                            }
                        }
                    })
                    btnLower.setOnClickListener {
                        if (etValue.editText?.text?.isNotEmpty() == true) {
                            val value = (etValue.editText?.text?.toString() ?: "1").toInt()
                            if (value > 1) {
                                etValue.editText?.setText("${value - 1}")
                                if (value - 1 <= 1) {
                                    btnLower.isEnabled = false
                                    btnLower.alpha = 0.3f
                                }
                                if (value - 1 < 85) {
                                    btnHigher.isEnabled = true
                                    btnHigher.alpha = 1f
                                }
                            }
                        } else {
                            etValue.editText?.setText("1")
                        }
                        lowerHandler.removeMessages(0)
                    }
                    btnHigher.setOnClickListener {
                        if (etValue.editText?.text?.isNotEmpty() == true) {
                            val value = (etValue.editText?.text?.toString() ?: "1").toInt()
                            if (value < 85) {
                                etValue.editText?.setText("${value + 1}")
                                if (value + 1 > 1) {
                                    btnLower.isEnabled = true
                                    btnLower.alpha = 1f
                                }
                                if (value + 1 >= 85) {
                                    btnHigher.isEnabled = false
                                    btnHigher.alpha = 0.3f
                                }
                            }
                        } else {
                            etValue.editText?.setText("1")
                        }
                        higherHandler.removeMessages(0)
                    }
                    btnLower.setOnLongClickListener {
                        lowerHandler.sendEmptyMessageDelayed(0, 100)
                        false
                    }
                    btnHigher.setOnLongClickListener {
                        higherHandler.sendEmptyMessageDelayed(0, 100)
                        false
                    }
                    btnNegative.setOnClickListener {
                        dismiss()
                    }
                    btnPositive.setOnClickListener {
                        if (etValue.editText?.text?.isNotEmpty() == true && etValue.editText?.text?.toString()?.toInt()!! >= 1) {
                            val value = etValue.editText?.text.toString().toInt()
                            sharedPreferences.edit().putInt(SharedKey.NECK_ANGLE, value).apply()
                            preference.summary = "$value°"
//                            preference.summary = if (value != 1) getString(R.string.days, value) else getString(R.string.a_day)
                            dismiss()
                        } else {
                            etValue.error = getString(R.string.please_input)
                        }
                    }

                    setContentView(view)
                }.show()
                true
            }
        }
    }
}