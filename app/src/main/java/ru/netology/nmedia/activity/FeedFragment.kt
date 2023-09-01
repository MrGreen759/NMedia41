package ru.netology.nmedia.activity

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.asLiveData
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import ru.netology.nmedia.R
import ru.netology.nmedia.activity.NewPostFragment.Companion.textArg
import ru.netology.nmedia.activity.PictureViewFragment.Companion.urlArg
import ru.netology.nmedia.adapter.OnInteractionListener
import ru.netology.nmedia.adapter.PostsAdapter
import ru.netology.nmedia.databinding.FragmentFeedBinding
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.viewmodel.PostViewModel

@AndroidEntryPoint
class FeedFragment : Fragment() {

    private val viewModel: PostViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentFeedBinding.inflate(inflater, container, false)
        var errOp = 0
        var errId = 0L
        var newPostsCount = 0

        val adapter = PostsAdapter(object : OnInteractionListener {
            override fun onEdit(post: Post) {
                viewModel.edit(post)
            }

            override fun onLike(post: Post) {
                viewModel.likeById(post)
            }

            override fun onRemove(post: Post) {
                viewModel.removeById(post.id)
            }

            override fun onShare(post: Post) {
                val intent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, post.content)
                    type = "text/plain"
                }

                val shareIntent =
                    Intent.createChooser(intent, getString(R.string.chooser_share_post))
                startActivity(shareIntent)
            }

//            override fun onPost(id: Long) {
//                findNavController().navigate(R.id.action_feedFragment_to_onePostFragment,
//                    Bundle().apply { idArg = id })
//            }

            override fun onPost(post: Post) {
                var bundle = Bundle()
                bundle.putSerializable("post", post)
                findNavController().navigate(R.id.action_feedFragment_to_onePostFragment,
                    bundle)
            }

            override fun onPicture(url: String) {
                findNavController().navigate(R.id.action_feedFragment_to_pictureViewFragment,
                Bundle().apply { urlArg = url })
            }

        })

        binding.list.adapter = adapter

        viewModel.dataState.observe(viewLifecycleOwner) { state ->
            binding.progress.isVisible = state.loading
            binding.swiperefresh.isRefreshing = state.refreshing
            if (state.error) {
                Snackbar.make(binding.root, R.string.error_loading, Snackbar.LENGTH_LONG)
//                    .setAction(R.string.retry_loading) { viewModel.loadPosts() }
                    .setAction(R.string.retry_loading) { adapter.refresh() }
                    .show()
            }
        }

        lifecycleScope.launchWhenCreated {
            viewModel.data.collectLatest {
                adapter.submitData(it)
            }
        }

        lifecycleScope.launchWhenCreated {
            adapter.loadStateFlow.collectLatest { state ->
                binding.swiperefresh.isRefreshing =
                    state.refresh is LoadState.Loading ||
                            state.prepend is LoadState.Loading ||
                            state.append is LoadState.Loading
            }
        }

//        viewModel.data.observe(viewLifecycleOwner) { state ->
//            adapter.submitList(state.posts)
//            binding.emptyText.isVisible = state.empty
//        }

        viewModel.newerCount.asLiveData().observe(viewLifecycleOwner) { state ->
            if (state != 0) {
                newPostsCount += 1
                binding.buttonNew.isVisible = true
                binding.buttonNew.text = getString(R.string.new_posts_appear) + " ($newPostsCount)"
            }
        }

        binding.buttonNew.setOnClickListener {
            viewModel.showAll()
            with (binding) {
                list.smoothScrollToPosition(0)
                buttonNew.visibility = View.GONE
            }
            newPostsCount = 0
        }

        binding.swiperefresh.setOnRefreshListener {
            viewModel.refreshFromId()
        }

        binding.fab.setOnClickListener {
            findNavController().navigate(R.id.action_feedFragment_to_newPostFragment)
        }

        viewModel.edited.observe(viewLifecycleOwner) { post ->
            if (post.id == 0L) {
                return@observe
            }
            println("--------------------- ID: " + post.id)
            // переход во фрагмент редактирования текущего поста / создания нового поста
            findNavController().navigate(
                R.id.action_feedFragment_to_newPostFragment,
                Bundle().apply { textArg = post.content })
        }

        viewModel.errorPostId.observe(viewLifecycleOwner){ errId = it}
        viewModel.errorOperation.observe(viewLifecycleOwner) {
            if((it==1) || (it==2)) {
                binding.errorGroup.isVisible = true
                errOp = it
            }
        }

        binding.retryButton.setOnClickListener {
            binding.errorGroup.isVisible = false
            when (errOp) {
//                1 -> viewModel.likeById(errId)
                2 -> viewModel.removeById(errId)
            }
        }

        binding.cancelButton.setOnClickListener {
            binding.errorGroup.isVisible = false
            viewModel.errorOperation.value = 0
            errOp = 0
            errId = 0
        }

        return binding.root
    }
}
