package ru.netology.nmedia.activity

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.nmedia.*
import ru.netology.nmedia.activity.PictureViewFragment.Companion.urlArg
import ru.netology.nmedia.databinding.FragmentOnePostBinding
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.util.Utils
import ru.netology.nmedia.view.*
import ru.netology.nmedia.viewmodel.PostViewModel

// Фрагмент просмотра карточки одного поста во весь экран

@AndroidEntryPoint
class OnePostFragment: Fragment() {

//    companion object {
//        var Bundle.idArg: Long? by IdArg
//    }


    @SuppressLint("SuspiciousIndentation")
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding: FragmentOnePostBinding = FragmentOnePostBinding.inflate(inflater, container, false)
//        val viewModel: PostViewModel by viewModels(ownerProducer = ::requireParentFragment)
        val viewModel: PostViewModel by activityViewModels()
        val onePost = arguments?.getSerializable("post") as Post

//       viewModel.data.observe(viewLifecycleOwner) { state ->
//            val post = state.posts.find { it.id == postId } ?: return@observe
//            val post = pl.find{post:Post -> post.id == postId}

            with(binding) {
                author.text = onePost.author
                published.text = Utils.covertUT(onePost.published)
                content.text = onePost.content
                icon.loadCircleCrop("${BuildConfig.BASE_URL}/avatars/${onePost.authorAvatar}")
                tvPostId.setText("ID: " + onePost.id.toString())
                ibLikes.text = Utils.convert(onePost.likes)
                ibLikes.isChecked = onePost.likedByMe

                ivPhoto.load("${BuildConfig.BASE_URL}/media/${onePost.attachment?.url}")

                ivPhoto.setOnClickListener{
                    findNavController().navigate(R.id.action_onePostFragment_to_pictureViewFragment,
                    Bundle().apply { urlArg = onePost.attachment?.url })
                }

                ibLikes.setOnClickListener {
                    viewModel.likeById(onePost)
                }

                // слушатель на кнопку "три точки"
                buttonMenu.setOnClickListener {
                    PopupMenu(it.context, it).apply {
                        inflate(R.menu.options_post)
                        setOnMenuItemClickListener { item ->
                            when (item.itemId) {
                                R.id.remove -> {
                                    viewModel.removeById(onePost.id)
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
                                    viewModel.edit(onePost)
                                    findNavController().navigateUp()
                                    true
                                }
                                else -> false
                            }
                        }
                    }.show()
                }
            }
        return binding.root
        }
    }

