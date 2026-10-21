package com.freesensi.ai

import android.app.Activity
import android.os.Bundle
import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.view.Gravity
import android.view.ViewGroup
import android.widget.*
import org.json.JSONObject
import kotlin.math.roundToInt

class MainActivity : Activity() {
    private val bg = Color.rgb(14, 14, 18)
    private val panel = Color.rgb(29, 29, 36)
    private val red = Color.rgb(239, 45, 62)
    private val white = Color.rgb(245, 245, 248)
    private val muted = Color.rgb(180, 180, 190)
    private val labels = listOf("عام", "النقطة الحمراء", "سكوب 2x", "سكوب 4x", "سكوب القناص", "النظر الحر")
    private val values = mutableListOf(185, 180, 175, 165, 90, 150)
    private lateinit var root: LinearLayout
    private lateinit var result: TextView
    private lateinit var profileName: EditText
    private lateinit var sliders: MutableList<SeekBar>
    private val prefs by lazy { getSharedPreferences("sensi_profiles", Context.MODE_PRIVATE) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = bg
        window.navigationBarColor = bg
        loadValues()
        buildUi()
    }

    private fun dp(v: Int): Int = (v * resources.displayMetrics.density).roundToInt()

    private fun buildUi() {
        root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(bg)
            setPadding(dp(16), dp(14), dp(16), dp(20))
        }
        val scroll = ScrollView(this)
        scroll.addView(root)
        setContentView(scroll)

        root.addView(text("FREE FIRE SENSI AI PRO", 23, red, true, Gravity.CENTER))
        root.addView(text("مساعد ذكي لضبط الحساسية حسب تجربتك", 15, muted, false, Gravity.CENTER).apply {
            setPadding(0, dp(8), 0, dp(16))
        })

        val info = "الأداة تقترح إعدادات فقط؛ لا تعدّل ملفات اللعبة ولا تضمن الهيدشوت."
        root.addView(card().apply { addView(text(info, 14, white, false, Gravity.CENTER)) })

        root.addView(text("إعدادات الحساسية", 19, white, true, Gravity.RIGHT).apply {
            setPadding(0, dp(18), 0, dp(8))
        })
        sliders = mutableListOf()
        labels.forEachIndexed { i, label ->
            val row = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(dp(12), dp(8), dp(12), dp(8))
                setBackgroundColor(panel)
            }
            val header = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
            }
            val title = text(label, 15, white, true, Gravity.RIGHT)
            header.addView(title, LinearLayout.LayoutParams(0, dp(36), 1f))
            val number = text(values[i].toString(), 15, red, true, Gravity.CENTER)
            header.addView(number, LinearLayout.LayoutParams(dp(48), dp(36)))
            row.addView(header)
            val seek = SeekBar(this).apply {
                max = 200
                progress = values[i].coerceIn(0, 200)
                progressTintList = android.content.res.ColorStateList.valueOf(red)
                thumbTintList = android.content.res.ColorStateList.valueOf(red)
            }
            seek.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(s: SeekBar?, p: Int, fromUser: Boolean) {
                    values[i] = p
                    number.text = p.toString()
                    updateRecommendation()
                }
                override fun onStartTrackingTouch(s: SeekBar?) {}
                override fun onStopTrackingTouch(s: SeekBar?) {}
            })
            row.addView(seek)
            sliders.add(seek)
            val lp = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            lp.bottomMargin = dp(8)
            root.addView(row, lp)
        }

        root.addView(text("تحليل وتوصية", 19, white, true, Gravity.RIGHT).apply {
            setPadding(0, dp(12), 0, dp(8))
        })
        result = text("", 14, white, false, Gravity.RIGHT)
        root.addView(card().apply { addView(result) })
        updateRecommendation()

        root.addView(text("حفظ ملف إعدادات", 18, white, true, Gravity.RIGHT).apply {
            setPadding(0, dp(16), 0, dp(6))
        })
        profileName = EditText(this).apply {
            hint = "اسم الملف مثلاً: سحب سريع"
            setHintTextColor(muted)
            setTextColor(white)
            textSize = 15f
            setSingleLine(true)
            setPadding(dp(12), 0, dp(12), 0)
            setBackgroundColor(panel)
        }
        root.addView(profileName, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(52)))
        root.addView(button("حفظ الإعدادات") { saveProfile() })
        root.addView(button("عرض الملفات المحفوظة") { showProfiles() })
        root.addView(button("استعادة الإعدادات الافتراضية") {
            val defaults = listOf(185, 180, 175, 165, 90, 150)
            defaults.forEachIndexed { i, v -> sliders[i].progress = v }
            Toast.makeText(this, "تمت استعادة القيم الافتراضية", Toast.LENGTH_SHORT).show()
        })
        root.addView(text("ملاحظة: ابدأ بالتجربة والتعديل تدريجياً، واستخدم إعدادات اللعبة الرسمية فقط.", 12, muted, false, Gravity.CENTER).apply {
            setPadding(0, dp(16), 0, dp(8))
        })
    }

    private fun text(s: String, size: Int, color: Int, bold: Boolean, gravity: Int): TextView =
        TextView(this).apply {
            text = s
            textSize = size.toFloat()
            setTextColor(color)
            this.gravity = gravity or Gravity.CENTER_VERTICAL
            if (bold) typeface = Typeface.DEFAULT_BOLD
            textDirection = TextView.TEXT_DIRECTION_LOCALE
        }

    private fun card(): LinearLayout = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
        setPadding(dp(14), dp(12), dp(14), dp(12))
        setBackgroundColor(panel)
        val lp = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        lp.bottomMargin = dp(8)
        layoutParams = lp
    }

    private fun button(label: String, action: () -> Unit): Button = Button(this).apply {
        text = label
        setTextColor(white)
        backgroundTintList = android.content.res.ColorStateList.valueOf(red)
        textSize = 14f
        setOnClickListener { action() }
        val lp = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(52))
        lp.topMargin = dp(8)
        layoutParams = lp
    }

    private fun updateRecommendation() {
        if (!::result.isInitialized) return
        val avg = values.average().roundToInt()
        val speed = when {
            values[0] >= 180 -> "عالية"
            values[0] >= 130 -> "متوسطة"
            else -> "منخفضة"
        }
        result.text = "متوسط قيمك الحالي: $avg/200\nحساسية العام: $speed\n\nاقتراح: جرّب القيم في ساحة التدريب، وعدّل كل مرة بمقدار 5 إلى 10 نقاط حسب حركة التصويب على جهازك. لا توجد قيمة واحدة تناسب كل اللاعبين."
    }

    private fun saveProfile() {
        val name = profileName.text.toString().trim()
        if (name.isEmpty()) {
            Toast.makeText(this, "اكتب اسماً للملف أولاً", Toast.LENGTH_SHORT).show()
            return
        }
        val obj = JSONObject()
        labels.forEachIndexed { i, label -> obj.put(label, values[i]) }
        val all = try { JSONObject(prefs.getString("profiles", "{}") ?: "{}") } catch (_: Exception) { JSONObject() }
        all.put(name, obj)
        prefs.edit().putString("profiles", all.toString()).apply()
        Toast.makeText(this, "تم حفظ الملف: $name", Toast.LENGTH_SHORT).show()
    }

    private fun showProfiles() {
        val all = try { JSONObject(prefs.getString("profiles", "{}") ?: "{}") } catch (_: Exception) { JSONObject() }
        val names = mutableListOf<String>()
        val iterator = all.keys()
        while (iterator.hasNext()) names.add(iterator.next())
        if (names.isEmpty()) {
            Toast.makeText(this, "ما في ملفات محفوظة لسه", Toast.LENGTH_SHORT).show()
            return
        }
        AlertDialog.Builder(this)
            .setTitle("اختر ملفاً لاستعادته")
            .setItems(names.toTypedArray()) { _, which ->
                val obj = all.optJSONObject(names[which]) ?: return@setItems
                labels.forEachIndexed { i, label ->
                    sliders[i].progress = obj.optInt(label, values[i]).coerceIn(0, 200)
                }
                Toast.makeText(this, "تم تحميل ${names[which]}", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("إغلاق", null)
            .show()
    }

    private fun loadValues() {
        // Defaults are intentionally local and safe; saved profiles are loaded through the profile picker.
    }
}
