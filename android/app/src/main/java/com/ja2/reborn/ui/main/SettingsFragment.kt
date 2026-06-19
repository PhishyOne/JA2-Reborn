package com.ja2.reborn.ui.main

import android.app.AlertDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.ja2.reborn.*
import com.ja2.reborn.databinding.FragmentLauncherSettingsBinding


/**
 * A placeholder fragment containing a simple view.
 */
class SettingsFragment : Fragment() {
    private var _binding: FragmentLauncherSettingsBinding? = null
    private val binding get() = _binding!!

    private lateinit var configurationModel: ConfigurationModel
    private lateinit var resolutionModes: Array<ResolutionMode>
    private lateinit var scalingQualities: Array<ScalingQuality>
    private lateinit var mouseModes: Array<MouseMode>
    private var updatingResolutionFields = false

    override fun onCreate(savedInstanceState: Bundle?) {
        configurationModel = ViewModelProvider(requireActivity())[ConfigurationModel::class.java]
        resolutionModes = arrayOf(
            ResolutionMode.MODERN,
            ResolutionMode.HIGH_RES,
            ResolutionMode.RETRO
        )
        scalingQualities = arrayOf(
            ScalingQuality.NEAR_PERFECT,
            ScalingQuality.PERFECT,
            ScalingQuality.LINEAR
        )
        mouseModes = MouseMode.DISPLAY_ORDER

        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLauncherSettingsBinding.inflate(inflater, container, false)

        val resolutionModeLabels = resolutionModes.map { LocalizationHelper.getResolutionModeLabel(requireContext(), it) }
        val resolutionModeAdapter: ArrayAdapter<String> =
            ArrayAdapter(this.requireContext(), R.layout.launcher_spinner_item, resolutionModeLabels)
        resolutionModeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.resolutionModeSpinner.adapter = resolutionModeAdapter

        configurationModel.resolutionMode.observe(viewLifecycleOwner) { mode ->
            val index = resolutionModes.indexOf(mode)
            if (index >= 0) {
                binding.resolutionModeSpinner.setSelection(index)
            }
            if (configurationModel.expertSettings.value != true) {
                applyPresetResolution(mode)
            }
        }
        binding.resolutionModeSpinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    if (position >= 0 && position < resolutionModes.size) {
                        val mode = resolutionModes[position]
                        configurationModel.setResolutionMode(mode)
                        if (configurationModel.expertSettings.value != true) {
                            applyPresetResolution(mode)
                        }
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                }
            }

        val scalingLabels = scalingQualities.map { LocalizationHelper.getScalingQualityLabel(requireContext(), it) }
        val scalingAdapter: ArrayAdapter<String> =
            ArrayAdapter(this.requireContext(), R.layout.launcher_spinner_item, scalingLabels)
        scalingAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.scalingQualitySpinner.adapter = scalingAdapter

        configurationModel.scalingQuality.observe(viewLifecycleOwner) { scalingQuality ->
            val index = scalingQualities.indexOf(scalingQuality)
            if (index >= 0) {
                binding.scalingQualitySpinner.setSelection(index)
            }
        }
        binding.scalingQualitySpinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    if (position >= 0 && position < scalingQualities.size) {
                        configurationModel.setScalingQuality(scalingQualities[position])
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                }
            }

        val mouseModeLabels = mouseModes.map { LocalizationHelper.getMouseModeLabel(requireContext(), it) }
        val mouseModeAdapter: ArrayAdapter<String> =
            ArrayAdapter(this.requireContext(), R.layout.launcher_spinner_item, mouseModeLabels)
        mouseModeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.mouseModeSpinner.adapter = mouseModeAdapter

        configurationModel.mouseMode.observe(viewLifecycleOwner) { mouseMode ->
            val index = mouseModes.indexOf(mouseMode)
            if (index >= 0) {
                binding.mouseModeSpinner.setSelection(index)
            }
        }
        binding.mouseModeSpinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    if (position >= 0 && position < mouseModes.size) {
                        configurationModel.setMouseMode(mouseModes[position])
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                }
            }

        configurationModel.resolution.observe(viewLifecycleOwner) { resolution ->
            updatingResolutionFields = true
            binding.resolutionWidthEditText.setText(resolution.width.toString())
            binding.resolutionHeightEditText.setText(resolution.height.toString())
            updatingResolutionFields = false
        }

        binding.resolutionWidthEditText.addTextChangedListener(resolutionTextWatcher)
        binding.resolutionHeightEditText.addTextChangedListener(resolutionTextWatcher)

        configurationModel.expertSettings.observe(viewLifecycleOwner) { enabled ->
            binding.expertSettingsCheckbox.isChecked = enabled
            setExpertControlsEnabled(enabled)
            if (!enabled) {
                applyStandardSettings()
            }
        }

        binding.expertSettingsCheckbox.setOnCheckedChangeListener { _, isChecked ->
            if (configurationModel.expertSettings.value == isChecked) {
                return@setOnCheckedChangeListener
            }

            configurationModel.setExpertSettings(isChecked)
            if (isChecked) {
                showExpertSettingsWarning()
            } else {
                applyStandardSettings()
            }
        }

        return binding.root
    }

    private val resolutionTextWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit

        override fun afterTextChanged(s: Editable?) {
            if (updatingResolutionFields || configurationModel.expertSettings.value != true) {
                return
            }

            val width = binding.resolutionWidthEditText.text.toString().toUIntOrNull()
            val height = binding.resolutionHeightEditText.text.toString().toUIntOrNull()
            if (width != null && height != null && width > 0u && height > 0u) {
                configurationModel.setResolution(Resolution(width, height))
            }
        }
    }

    private fun applyPresetResolution(mode: ResolutionMode) {
        val launcherActivity = requireActivity()
        if (launcherActivity is LauncherActivity) {
            configurationModel.setResolution(launcherActivity.calculateResolutionForMode(mode))
        }
    }

    private fun applyStandardSettings() {
        val mode = configurationModel.resolutionMode.value ?: ResolutionMode.DEFAULT
        applyPresetResolution(mode)
        configurationModel.setScalingQuality(ScalingQuality.DEFAULT)
        configurationModel.setMouseMode(MouseMode.DEFAULT)
    }

    private fun setExpertControlsEnabled(enabled: Boolean) {
        binding.manualResolutionLabel.isEnabled = enabled
        binding.resolutionWidthEditText.isEnabled = enabled
        binding.resolutionHeightEditText.isEnabled = enabled
        binding.scalingQualitySpinner.isEnabled = enabled
        binding.mouseModeSpinner.isEnabled = enabled

        val alpha = if (enabled) 1.0f else 0.42f
        binding.manualResolutionLabel.alpha = alpha
        binding.manualResolutionFields.alpha = alpha
        binding.scalingQualityInfoTitle.alpha = alpha
        binding.scalingQualityInfoText.alpha = alpha
        binding.scalingQualitySpinner.alpha = alpha
        binding.mouseModeInfoTitle.alpha = alpha
        binding.mouseModeInfoText.alpha = alpha
        binding.mouseModeSpinner.alpha = alpha
    }

    private fun showExpertSettingsWarning() {
        AlertDialog.Builder(requireContext())
            .setTitle("! ${getString(R.string.expert_settings_warning_title)}")
            .setMessage(getString(R.string.expert_settings_warning_message))
            .setPositiveButton(R.string.expert_settings_confirm, null)
            .show()
    }

    companion object {
        private const val TAG = "SettingsFragment"

        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private const val ARG_SECTION_NUMBER = "section_number"

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        @JvmStatic
        fun newInstance(sectionNumber: Int): SettingsFragment {
            return SettingsFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_SECTION_NUMBER, sectionNumber)
                }
            }
        }
    }
}
