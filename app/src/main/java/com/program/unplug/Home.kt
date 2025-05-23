package com.program.unplug

import android.app.AppOpsManager
import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.program.unplug.databinding.FragmentHomeBinding
import java.util.*
import java.util.concurrent.TimeUnit

class Home : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    // chart and data variables
    private lateinit var barChart: BarChart
    private lateinit var barData: BarData
    private lateinit var barDataSet: BarDataSet
    private lateinit var barEntries: ArrayList<BarEntry>

    // labels for X-axis
    private val days = arrayOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // inflate the layout and bind to the _binding
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        // retrieve the entered data by the user
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Request Usages Access permission if needed
        requestUsageStatsPermission()

        // Setup chart
        barChart = binding.mpBarChart
        getBarChartData()

        // initializing bar data set
        barDataSet = BarDataSet(barEntries, "Weekly Usage (min)")
        // setting text size
        barDataSet.valueTextSize = 12f
        barDataSet.setDrawValues(true)
        barDataSet.highLightColor = Color.GRAY
        barDataSet.highLightAlpha = 100
        // setting colors for bar chart text
        barDataSet.valueTextColor = Color.BLACK
        // setting color for bar data set
        barDataSet.color = resources.getColor(R.color.Lavender_Indigo, null)

        // initializing bar data
        barData = BarData(barDataSet)
        barChart.data = barData

        barChart.apply {
            // animation
            animateY(1000)

            // hide grid background
            setDrawGridBackground(false)

            // disable zoom and scaling
            setScaleEnabled(false)
            setPinchZoom(false)

            // x-axis customization
            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                valueFormatter = IndexAxisValueFormatter(days)
                granularity = 1f
                setDrawGridLines(false)
                textColor = Color.BLACK
                textSize = 12f
            }

            // y-axis customization
            axisLeft.apply {
                axisMinimum = 0f
                textColor = Color.BLACK
                textSize = 12f
                setDrawGridLines(false)
            }

            axisRight.isEnabled = false

            legend.isEnabled = false
            description.isEnabled = false
        }

        // setup circular progress bar
        setupProgressBar()

        //show today's usage
        updateTodayUsage()
        // Update daily usage if permission is granted
        if (hasUsageStatsPermission()) {
            updateTodayUsage()
        }
    }

    private fun getBarChartData() {
        // initialize bar entries list
        barEntries = ArrayList()

        // adding data to bar entries list
        barEntries.add(BarEntry(0f, 50f))
        barEntries.add(BarEntry(1f, 40f))
        barEntries.add(BarEntry(2f, 30f))
        barEntries.add(BarEntry(3f, 60f))
        barEntries.add(BarEntry(4f, 70f))
        barEntries.add(BarEntry(5f, 65f))
        barEntries.add(BarEntry(6f, 60f))
    }

    //======================================================================================
    private fun setupProgressBar() {
        val total = 100
        val completed = 75

        binding.progressBar.max = total
        binding.progressBar.progress = completed
        binding.progressText.text = "$completed%"
    }

    //===========================================================================================
    // Uses Permission
    private fun requestUsageStatsPermission() {
        if (!hasUsageStatsPermission()) {
            startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
        }
    }

    private fun hasUsageStatsPermission(): Boolean {
        val context = requireContext()
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager

        return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            // For Android 10 (API 29) and above, use unsafeCheckOpNoThrow
            appOps.unsafeCheckOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                android.os.Process.myUid(),
                context.packageName
            ) == AppOpsManager.MODE_ALLOWED
        } else {
            // For older versions, fallback to deprecated checkOpNoThrow with suppression
            @Suppress("DEPRECATION")
            appOps.checkOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                android.os.Process.myUid(),
                context.packageName
            ) == AppOpsManager.MODE_ALLOWED
        }
    }

    //===================================================================================================================
    // Today Use Time Update
    private fun updateTodayUsage() {
        val usageStatsManager = requireContext().getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val packageManager = requireContext().packageManager

        // Start of today
        val calendar = Calendar.getInstance().apply {
            timeZone = TimeZone.getDefault()
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val startTime = calendar.timeInMillis
        val endTime = System.currentTimeMillis()

        val usageStatsList = usageStatsManager.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY,
            startTime,
            endTime
        )

        var totalTimeMillis: Long = 0

        for (usageStats in usageStatsList) {
            val packageName = usageStats.packageName

            if (usageStats.lastTimeUsed < startTime || usageStats.totalTimeInForeground <= 1000) continue

            try {
                val appInfo = packageManager.getApplicationInfo(packageName, 0)
                val isSystemApp = (appInfo.flags and android.content.pm.ApplicationInfo.FLAG_SYSTEM) != 0
                val launchIntent = packageManager.getLaunchIntentForPackage(packageName)

                // Skip system apps, non-launchable apps, and keyboards
                if (isSystemApp || launchIntent == null || isKeyboardApp(packageName)) continue

                totalTimeMillis += usageStats.totalTimeInForeground
            } catch (e: Exception) {
                // ignore errors
            }
        }

        val hours = TimeUnit.MILLISECONDS.toHours(totalTimeMillis)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(totalTimeMillis) % 60

        val usageString = when {
            hours > 0 -> "${hours}h ${minutes}m"
            minutes > 0 -> "${minutes}m"
            else -> "0m"
        }

        binding.dailyUseTime.text = usageString
    }

    private fun isKeyboardApp(packageName: String): Boolean {
        return packageName.contains("keyboard") || packageName in listOf(
            "com.google.android.inputmethod.latin",  // Gboard
            "com.microsoft.swiftkey",
            "com.baidu.input",
            "com.samsung.android.honeyboard"
        )
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
