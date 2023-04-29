package com.example.test_keyboard

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.charleskorn.kaml.Yaml
import com.example.test_keyboard.databinding.FragmentFirstBinding
import kotlinx.serialization.Serializable

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class FirstFragment : Fragment() {
    var DEBUG = true;
    private lateinit var keyboardLayout: MyKBView.KeyboardLayout

    private var _binding: FragmentFirstBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentFirstBinding.inflate(inflater, container, false)
        keyboardLayout = Yaml.default.decodeFromStream(
            MyKBView.KeyboardLayout.serializer(),
            resources.openRawResource(R.raw.kb_default)
        )
        binding.myKBView.layout = keyboardLayout
        if (DEBUG) {
            Log.i(getString(R.string.my_ime), "onInitializeInterface: ${keyboardLayout.rows.size}")
            for (row in keyboardLayout.rows)
                Log.i(getString(R.string.my_ime), "onInitializeInterface: ${row.toPrintString()}")
        }
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonFirst.setOnClickListener {
            findNavController().navigate(R.id.action_FirstFragment_to_SecondFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}