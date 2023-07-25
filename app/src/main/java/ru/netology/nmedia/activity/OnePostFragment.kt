package ru.netology.nmedia.activity

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.nmedia.*
import ru.netology.nmedia.activity.PictureViewFragment.Companion.urlArg
import ru.netology.nmedia.databinding.FragmentOnePostBinding
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.util.IdArg
import ru.netology.nmedia.util.Utils
import ru.netology.nmedia.view.*
import ru.netology.nmedia.viewmodel.PostViewModel

// Фрагмент просмотра карточки одного поста во весь экран

@AndroidEntryPoint
class OnePostFragment: Fragment() {

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding: FragmentOnePostBinding = FragmentOnePostBinding.inflate(inflater, container, false)
//        val viewModel: PostViewModel by viewModels(ownerProducer = ::requireParentFragment)
        val viewModel: PostViewModel by activityViewModels()
        val postId = arguments?.idArg


        viewModel.data.observe(viewLifecycleOwner) { state ->
            val post = state.posts.find { it.id == postId } ?: return@observe

            with(binding) {
                author.text = post.author
                published.text = post.published.toString()
                content.text = post.content
                tvPostId.setText("ID: " + post.id.toString())
                ibLikes.text = Utils.convert(post.likes)
                ibLikes.isChecked = post.likedByMe

                ivPhoto.load("${BuildConfig.BASE_URL}/media/${post.attachment?.url}")

                ivPhoto.setOnClickListener{
                    findNavController().navigate(R.id.action_onePostFragment_to_pictureViewFragment,
                    Bundle().apply { urlArg = post.attachment?.url })
                }

                ibLikes.setOnClickListener {
                    viewModel.likeById(post.id)
                }

                // слушатель на кнопку "три точки"
                buttonMenu.setOnClickListener {
                    PopupMenu(it.context, it).apply {
                        inflate(R.menu.options_post)
                        setOnMenuItemClickListener { item ->
                            when (item.itemId) {
                                R.id.remove -> {
                                    viewModel.removeById(post.id)
                                    findNavController().navigateUp()
                                    true
                                }
                                R.id.add -> {
                                    val epost = Post (
                                        id = -1L,
                                        authorId = -1L,
                                        author = it.context.getString(R.string.title),
                                        authorAvatar = "",
                                        content = "",
                                        published = 0L,
                                        likedByMe = false,
                                        likes = 0,
                                        hidden = false,
                                        attachment = null
                                    )
                                    findNavController().navigate(R.id.action_onePostFragment_to_newPostFragment)
                                    true
                                }
                                R.id.edit -> {
                                    viewModel.edit(post)
                                    findNavController().navigateUp()
                                    true
                                }
                                else -> false
                            }
                        }
                    }.show()
                }
            }
        }
        return binding.root
    }

    companion object {
        var Bundle.idArg: Long? by IdArg
    }

}
