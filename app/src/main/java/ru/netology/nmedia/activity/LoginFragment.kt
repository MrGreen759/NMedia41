package ru.netology.nmedia.activity

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import ru.netology.nmedia.R
import ru.netology.nmedia.activity.PictureViewFragment.Companion.urlArg
import ru.netology.nmedia.databinding.FragmentFeedBinding
import ru.netology.nmedia.databinding.FragmentLoginBinding
import ru.netology.nmedia.viewmodel.LoginViewModel
import ru.netology.nmedia.viewmodel.PostViewModel

class LoginFragment : Fragment() {

    companion object {
        fun newInstance() = LoginFragment()
    }

//    private lateinit var viewModel: LoginViewModel
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
                    viewModel.login(usernametext, passwordtext)
                    findNavController().navigate(R.id.action_loginFragment_to_feedFragment)
                } else {
                    println("............ One or two fields are empty")
                    findNavController().navigate(R.id.action_loginFragment_to_feedFragment)
                }
            }
        }
        return binding.root
    }

//    override fun onActivityCreated(savedInstanceState: Bundle?) {
//        super.onActivityCreated(savedInstanceState)
//        viewModel = ViewModelProvider(this).get(LoginViewModel::class.java)
//        // TODO: Use the ViewModel
//    }

}
