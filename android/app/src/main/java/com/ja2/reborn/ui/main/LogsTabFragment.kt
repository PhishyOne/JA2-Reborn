package com.ja2.reborn.ui.main

import android.content.ClipData
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.ja2.reborn.R
import com.ja2.reborn.databinding.FragmentLauncherLogsTabBinding
import java.io.File


class LogsTabFragment : Fragment() {
    private var _binding: FragmentLauncherLogsTabBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLauncherLogsTabBinding.inflate(inflater, container, false)

        binding.logsCopyToClipboardButton.setOnClickListener {
            val text = binding.logsText.text
            val clipboard =
                requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
            val clip = ClipData.newPlainText(resources.getText(R.string.logs_copied_to_clipboard_name), text)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(requireContext(), resources.getText(R.string.logs_copied_to_clipboard_toast), Toast.LENGTH_SHORT).show()
        }

        return binding.root
    }

    override fun onResume() {
        super.onResume()

        val logFile = activity?.cacheDir?.let { File(it, "ja2.log") }
        if (logFile != null && logFile.exists()) {
            val ringBuffer = ArrayDeque<String>(2000)
            logFile.bufferedReader().use { reader ->
                reader.forEachLine { line ->
                    if (ringBuffer.size >= 2000) {
                        ringBuffer.removeFirst()
                    }
                    ringBuffer.addLast(line)
                }
            }
            binding.logsText.text = ringBuffer.joinToString("\n")
        }
    }
}
