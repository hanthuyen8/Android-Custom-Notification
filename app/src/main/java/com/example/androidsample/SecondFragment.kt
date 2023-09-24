package com.example.androidsample

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.example.androidsample.databinding.FragmentSecondBinding

/**
 * A simple [Fragment] subclass as the second destination in the navigation.
 */
class SecondFragment : Fragment() {

    private var _binding: FragmentSecondBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentSecondBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonSecond.setOnClickListener {
            findNavController().navigate(R.id.action_SecondFragment_to_FirstFragment)
        }

        val notificationHelper =
            com.senspark.custom_notification.NotificationHelper(requireContext())
        notificationHelper.init(true, requireActivity())
//        notificationHelper.createCocosNotification(requireActivity(), 0, "Damn it")
        notificationHelper.unitySchedule(0, "Hello world 1a", "This is the short message \uD83D\uDE0B", "#FF0000","#00FF00",0, "", 0, 0)
//        notificationHelper.unitySchedule(1, "Hello world 2a", "This is the long message \uD83D\uDE0B. This will also make a single line with ellipsis. This is the message \uD83D\uDE0B. This will also make a single line with ellipsis. This is the message \uD83D\uDE0B. This will also make a single line with ellipsis",0, "", 0, 0)
//        notificationHelper.unitySchedule(2, "Hello world 1b", "This is the short message \uD83D\uDE0B",0, "", 0, 0)
//        notificationHelper.unitySchedule(3, "Hello world 2b", "This is the long message \uD83D\uDE0B. This will also make a single line with ellipsis. This is the message \uD83D\uDE0B. This will also make a single line with ellipsis. This is the message \uD83D\uDE0B. This will also make a single line with ellipsis",0, "", 0, 0)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}