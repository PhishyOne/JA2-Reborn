package com.ja2.reborn.ui.main

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.DocumentsContract
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.ja2.reborn.*
import com.ja2.reborn.databinding.FragmentLauncherDataTabBinding
import java.io.File


class DataTabFragment : Fragment() {
    private var _binding: FragmentLauncherDataTabBinding? = null
    private val binding get() = _binding!!

    private lateinit var configurationModel: ConfigurationModel
    private lateinit var versions: Array<VanillaVersion>
    private lateinit var scalingQualities: Array<ScalingQuality>
    private lateinit var mouseModes: Array<MouseMode>
    private val gameDirectoryPicker =
        registerForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
            uri?.let { handleGameDirectoryPicked(it) }
        }
    private val saveGameDirectoryPicker =
        registerForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
            uri?.let { handleSaveGameDirectoryPicked(it) }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        configurationModel = ViewModelProvider(requireActivity())[ConfigurationModel::class.java]
        versions = (VanillaVersion::values)()
        scalingQualities = arrayOf(
            ScalingQuality.NEAR_PERFECT,
            ScalingQuality.PERFECT,
            ScalingQuality.LINEAR
        )
        mouseModes = (MouseMode::values)()

        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLauncherDataTabBinding.inflate(inflater, container, false)

        val spinnerLabels = versions.map { v: VanillaVersion -> LocalizationHelper.getVanillaVersionLabel(requireContext(), v) }
        val adapter: ArrayAdapter<String> =
            ArrayAdapter(this.requireContext(), R.layout.launcher_spinner_item, spinnerLabels)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.gameVersionSpinner.adapter = adapter

        configurationModel.vanillaGameDir.observe(
            viewLifecycleOwner
        ) { vanillaGameDir ->
            if (vanillaGameDir != null) {
                binding.gameDirValueText.text = vanillaGameDir
            }
        }
        configurationModel.vanillaGameVersion.observe(
            viewLifecycleOwner
        ) { vanillaGameVersion ->
            val index = versions.indexOf(vanillaGameVersion)
            binding.gameVersionSpinner.setSelection(index)
        }
        configurationModel.saveGameDir.observe(
            viewLifecycleOwner
        ) { saveGameDir ->
            if (saveGameDir != null) {
                binding.saveGameDirValueText.text = saveGameDir
            }
        }
        binding.gameDirChooseButton.setOnClickListener {
            gameDirectoryPicker.launch(null)
        }
        binding.gameVersionSpinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    if (position >= 0 && position < versions.size) {
                        configurationModel.setVanillaGameVersion(versions[position])
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                }
            }
        binding.saveGameDirChooseButton.setOnClickListener {
            saveGameDirectoryPicker.launch(null)
        }

        val scalingLabels = scalingQualities.map { LocalizationHelper.getScalingQualityLabel(requireContext(), it) }
        val scalingAdapter: ArrayAdapter<String> =
            ArrayAdapter(this.requireContext(), R.layout.launcher_spinner_item, scalingLabels)
        scalingAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.scalingQualitySpinner.adapter = scalingAdapter

        configurationModel.resolution.observe(viewLifecycleOwner) { resolution ->
            if (binding.resolutionWidthEdit.text.toString() != resolution.width.toString()) {
                binding.resolutionWidthEdit.setText(resolution.width.toString())
            }
            if (binding.resolutionHeightEdit.text.toString() != resolution.height.toString()) {
                binding.resolutionHeightEdit.setText(resolution.height.toString())
            }
        }
        binding.resolutionWidthEdit.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (s.isNotEmpty()) {
                    try {
                        val width = s.toString().toUInt()
                        val current = configurationModel.resolution.value ?: Resolution.DEFAULT
                        if (width != current.width) {
                            configurationModel.setResolution(Resolution(width, current.height))
                        }
                    } catch (e: NumberFormatException) {
                        Log.w(TAG, "Invalid resolution width: $s", e)
                    }
                }
            }
        })
        binding.resolutionHeightEdit.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (s.isNotEmpty()) {
                    try {
                        val height = s.toString().toUInt()
                        val current = configurationModel.resolution.value ?: Resolution.DEFAULT
                        if (height != current.height) {
                            configurationModel.setResolution(Resolution(current.width, height))
                        }
                    } catch (e: NumberFormatException) {
                        Log.w(TAG, "Invalid resolution height: $s", e)
                    }
                }
            }
        })
        binding.resolutionAutoButton.setOnClickListener {
            val launcherActivity = requireActivity()
            if (launcherActivity is LauncherActivity) {
                configurationModel.resolution.value = launcherActivity.getRecommendedResolution()
            }
        }

        configurationModel.scalingQuality.observe(viewLifecycleOwner) { scalingQuality ->
            val index = scalingQualities.indexOf(scalingQuality)
            binding.scalingQualitySpinner.setSelection(index)
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
            binding.mouseModeSpinner.setSelection(index)
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

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun handleGameDirectoryPicked(uri: Uri) {
        persistDirectoryPermission(uri)
        val path = uri.toExternalStoragePath()
        if (path == null || !File(path).exists()) {
            Toast.makeText(requireContext(), R.string.directory_picker_path_error, Toast.LENGTH_SHORT).show()
            return
        }

        GameDir.checkGameDirectoryForCommonMistakes(requireContext(), path) {
            configurationModel.setVanillaGameDir(path)
            (activity as? LauncherActivity)?.persistJA2Configuration()
        }
    }

    private fun handleSaveGameDirectoryPicked(uri: Uri) {
        persistDirectoryPermission(uri)
        val path = uri.toExternalStoragePath()
        if (path == null || !File(path).exists()) {
            Toast.makeText(requireContext(), R.string.directory_picker_path_error, Toast.LENGTH_SHORT).show()
            return
        }

        configurationModel.setSaveGameDir(path)
        (activity as? LauncherActivity)?.persistJA2Configuration()
    }

    private fun persistDirectoryPermission(uri: Uri) {
        val flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
        runCatching {
            requireContext().contentResolver.takePersistableUriPermission(uri, flags)
        }
    }

    private fun Uri.toExternalStoragePath(): String? {
        if (authority != "com.android.externalstorage.documents") {
            return null
        }

        val documentId = DocumentsContract.getTreeDocumentId(this)
        val separatorIndex = documentId.indexOf(':')
        if (separatorIndex < 0) {
            return null
        }

        val volume = documentId.substring(0, separatorIndex)
        val relativePath = documentId.substring(separatorIndex + 1)
        val rootPath = when (volume.lowercase()) {
            "primary" -> Environment.getExternalStorageDirectory().absolutePath
            "home" -> File(Environment.getExternalStorageDirectory(), "Documents").absolutePath
            else -> "/storage/$volume"
        }

        return if (relativePath.isEmpty()) rootPath else File(rootPath, relativePath).absolutePath
    }

    companion object {
        private const val TAG = "DataTabFragment"
        private const val ARG_SECTION_NUMBER = "section_number"

        @JvmStatic
        fun newInstance(sectionNumber: Int): DataTabFragment {
            return DataTabFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_SECTION_NUMBER, sectionNumber)
                }
            }
        }
    }
}
