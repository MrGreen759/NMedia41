package ru.netology.nmedia.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import ru.netology.nmedia.BuildConfig
import ru.netology.nmedia.databinding.FragmentPictureViewBinding
import ru.netology.nmedia.util.StringArg
import ru.netology.nmedia.view.load

class PictureViewFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding: FragmentPictureViewBinding =
            FragmentPictureViewBinding.inflate(inflater, container, false)
        val pictureUrl = arguments?.urlArg

        binding.pictureView.load("${BuildConfig.BASE_URL}/media/${pictureUrl}")

        return binding.root
    }

    companion object {
        var Bundle.urlArg: String? by StringArg
    }
}
