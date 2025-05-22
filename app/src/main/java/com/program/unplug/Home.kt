package com.program.unplug

import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.program.unplug.databinding.FragmentHomeBinding

class Home : Fragment() {
    private var _binding:  FragmentHomeBinding? = null
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

        // variables with their ids
        barChart = binding.mpBarChart

        // calling get bar, chart data to add data to our array list
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

    private fun setupProgressBar() {
        val total = 100
        val completed = 75

        binding.progressBar.max = total
        binding.progressBar.progress = completed
        binding.progressText.text = getString(R.string.completed)
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}