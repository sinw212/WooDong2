package com.wd.woodong2.presentation.main

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowInsetsController
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.wd.woodong2.R
import com.wd.woodong2.databinding.MainActivityBinding
import com.wd.woodong2.presentation.chat.content.ChatFragment
import com.wd.woodong2.presentation.group.content.GroupFragment
import com.wd.woodong2.presentation.home.content.HomeFragment
import com.wd.woodong2.presentation.mypage.content.MyPageFragment

class MainActivity : AppCompatActivity() {

    companion object {
        private const val ID = "ID"
        fun newIntentForMain(context: Context): Intent =
            Intent(context, MainActivity::class.java)

        fun newIntentForAutoLogin(context: Context, id: String): Intent =
            Intent(context, MainActivity::class.java).apply {
                putExtra(ID, id)
            }
    }

    private lateinit var binding: MainActivityBinding
    /*

        private val homeFragment by lazy {
            HomeFragment.newInstance()
        }

        private val groupFragment by lazy {
            GroupFragment.newInstance()
        }

        private val chatFragment by lazy {
            ChatFragment.newInstance()
        }

        private val myPageFragment by lazy {
            MyPageFragment.newInstance()
        }

    */

    /**
     * 갤러리 접근 권한 설정
     * Target SDK 33 부터 READ_EXTERNAL_STORAGE 권한 세분화 (이미지/동영상/오디오)
     * Android 13(VERSION_CODES.TIRAMISU) 버전 체크하여 권한 요청 필요
     */
    private val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        arrayOf(
            android.Manifest.permission.READ_MEDIA_IMAGES,
            android.Manifest.permission.READ_MEDIA_VIDEO
        )
    } else {
        arrayOf(
            android.Manifest.permission.READ_EXTERNAL_STORAGE
        )
    }

    private val galleryPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            if (permissions.values.all { it }) {
                initView()
            } else {
                Toast.makeText(this, getString(R.string.main_toast_permission), Toast.LENGTH_SHORT)
                    .show()
                finish()
            }
        }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = MainActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //상태바 & 아이콘 색상 변경
        window.statusBarColor = ContextCompat.getColor(this@MainActivity, R.color.egg_yellow_toolbar)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) { // 안드로이드 11 이상에서만 동작
            window.insetsController?.setSystemBarsAppearance(
                WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS,
                WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
            )
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) { // 안드로이드 6.0 이상에서만 동작
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        } // 안드로이드 6.0 이하는 상태바 아이콘 색상 변경 지원 안함

        checkPermissions()

        // TODO 삭제
        val id = intent.getStringExtra(ID)
    }

    private fun checkPermissions() {
        if (permissions.all {
                ContextCompat.checkSelfPermission(
                    this,
                    it
                ) == PackageManager.PERMISSION_GRANTED
            }
        ) {
            initView()
        } else {
            galleryPermissionLauncher.launch(permissions)
        }
    }

    private fun initView() = with(binding) {
        //BottomNavigation 설정
        bottomNavigation.setOnItemSelectedListener { item ->
            val selectedFragment = when (item.itemId) {
                R.id.bottom_menu_home -> HomeFragment()
                R.id.bottom_menu_group -> GroupFragment()
                R.id.bottom_menu_chat -> ChatFragment()
                else -> MyPageFragment()
            }
            supportFragmentManager.beginTransaction()
                .replace(frameLayout.id, selectedFragment).commit()
            true
        }
        supportFragmentManager.beginTransaction()
            .replace(frameLayout.id, HomeFragment()).commit()
    }
}