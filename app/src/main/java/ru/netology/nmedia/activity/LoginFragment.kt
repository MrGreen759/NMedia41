package ru.netology.nmedia.activity

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import ru.netology.nmedia.R
import ru.netology.nmedia.databinding.FragmentLoginBinding
import ru.netology.nmedia.viewmodel.LoginViewModel

class LoginFragment : Fragment() {

    companion object {
        fun newInstance() = LoginFragment()
    }

    private val viewModel: LoginViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentLoginBinding.inflate(inflater, container, false)
        var usernametext = ""
        var passwordtext = ""

        with (binding) {
            username.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) { }

                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                    usernametext = username.getText().toString()
                    if(passwordtext.isNotEmpty()) buttonLogin.isEnabled = true
                }

                override fun afterTextChanged(p0: Editable?) { }
            })

            password.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) { }

                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                    passwordtext = password.text.toString()
                    if(usernametext.isNotEmpty()) buttonLogin.isEnabled = true
                }

                override fun afterTextChanged(p0: Editable?) { }
            })

            buttonLogin.setOnClickListener {
                println("-----------Username: " + usernametext)
                println("-----------Password: " + passwordtext)
                if (usernametext.isNotEmpty() && passwordtext.isNotEmpty()) {
                    progressBar.isVisible = true
                    viewModel.login(usernametext, passwordtext)
                } else {
                    println("............ One or two fields are empty")
                }
            }
        }

        viewModel.authResult.observe(viewLifecycleOwner) {
            if(it.equals("OK")) {
                viewModel.authResult.postValue("nothing")
                findNavController().navigate(R.id.action_loginFragment_to_feedFragment)
            }
            if(!it.equals("nothing")) {
                binding.retryTitle.text = it
                binding.errorGroup.isVisible = true
                binding.progressBar.isVisible = false
            }
        }

        binding.retryButton.setOnClickListener {
            binding.errorGroup.isVisible = false
            viewModel.authResult.postValue("nothing")
        }

        binding.cancelButton.setOnClickListener {
            binding.errorGroup.isVisible = false
            viewModel.authResult.postValue("nothing")
            findNavController().navigate(R.id.action_loginFragment_to_feedFragment)
        }

        return binding.root
    }

}
