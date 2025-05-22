package com.program.unplug

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.program.unplug.databinding.FragmentProfileBinding

class Profile : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupClickable()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null // avoid memory leaks
    }

    private fun setupClickable() {
        val layouts = listOf(
            binding.settingLayout,
            binding.privacyPolicyLayout,
            binding.helpSprtLayout,
            binding.signOutLayout
        )

        for (layout in layouts) {
            layout.setOnClickListener {
                layouts.forEach {
                    it.setBackgroundResource(R.drawable.bg_icon_unselected)
                }
                layout.setBackgroundResource(R.drawable.bg_icon_selected)
            }
        }
    }
}
