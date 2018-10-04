package com.github.jaccek.roseplayer

import android.databinding.DataBindingUtil
import android.os.Build
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.KeyEvent
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import com.github.jaccek.roseplayer.databinding.ActivityMainBinding
import kotlinx.android.synthetic.main.activity_main.view.*

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        val host: NavHostFragment = supportFragmentManager
                .findFragmentById(R.id.navigationHostFragment) as NavHostFragment? ?: return
        val navController = host.navController

        navController.navigate(R.id.playingQueueFragment)

        binding.root.bottomNavigationView?.let {
            NavigationUI.setupWithNavController(it, navController)
        }
    }

//    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            return super.onKeyDown(keyCode, event)
//        }
//        when (keyCode) {
//            KeyEvent.KEYCODE_MEDIA_PLAY -> {
//                yourMediaController.dispatchMediaButtonEvent(event)
//                return true
//            }
//        }
//        return super.onKeyDown(keyCode, event)
//    }
}
