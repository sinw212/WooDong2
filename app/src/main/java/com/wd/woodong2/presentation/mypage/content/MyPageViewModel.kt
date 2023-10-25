package com.wd.woodong2.presentation.mypage.content

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.wd.woodong2.data.repository.UserRepositoryImpl
import com.wd.woodong2.domain.usecase.UserGetItemsUseCase
import com.wd.woodong2.presentation.chat.content.UserItem
import com.wd.woodong2.presentation.home.content.HomeItem
import kotlinx.coroutines.launch

class MyPageViewModel(
    private val userItem: UserGetItemsUseCase,
    private val udbReference: DatabaseReference
) : ViewModel(

){
    // 누적 조회수, 작성글/댓글 수, 받은 공감 수 설정
    private val _list: MutableLiveData<List<HomeItem>> = MutableLiveData()
    val list: LiveData<List<HomeItem>> get() = _list

    val userId = "user1"
    var userInfo : MutableLiveData<UserItem> = MutableLiveData()   //UserItem
    init {
        getUserItem()
        loadDataFromFirebase()
    }

//    누적 조회수
//    조회수 변수 필요 - HOMELIST에 작성한 글에 대한 조회수의 모든 합
//    HOMELIST에 작성한 글에 대한 thumbCount의 모든 합
    private fun loadDataFromFirebase() {
        MyPageViewModelFactory().databaseReference.addValueEventListener(object :
            ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val dataList = ArrayList<HomeItem>()

                for (postSnapshot in dataSnapshot.children) {
                    val firebaseData = postSnapshot.getValue(HomeItem::class.java)
                    if (firebaseData != null) {
                        dataList.add(firebaseData)
                    }
                }
                _list.value = dataList
            }

            override fun onCancelled(databaseError: DatabaseError) {
            }
        })
    }
//    userInfo에
//    댓글 횟수를 나타내는 변수 필요
//    작성한 글의 횟수를 나타내는 변수 필요
    private fun getUserItem(

    ) = viewModelScope.launch {
        runCatching {
            userItem(userId) { items ->
                val userItem = items?.userItems?.map {
                    UserItem(
                        id = it.id,
                        name = it.name,
                        imgProfile = it.imgProfile,
                        email = it.email,
                        chatIds = it.chatIds,
                        firstLocation = it.firstLocation,
                        secondLocation = it.secondLocation
                    )
                }.orEmpty()
                userInfo.postValue(userItem.firstOrNull())
            }
        }.onFailure {
            Log.e("homeItem", it.message.toString())
        }

    }

    //비밀번호, 이름, 이메일 ->
    // 나중에 정보 입력 받을때 어떤 정보를 받을지 몰라서
    // 아직은 useCase에 안 옴기겠습니다.
    fun updateUserItem(
        name : String?,
        imgProfile : String?,
        email : String?,
    ) = viewModelScope.launch {
        runCatching {
            val updateUserData = mapOf(
                "name" to name,
                "imgProfile" to imgProfile,
                "email" to email,
            )
            udbReference.updateChildren(updateUserData)
        }
    }
}
class MyPageViewModelFactory() : ViewModelProvider.Factory {

    val databaseReference by lazy {
        FirebaseDatabase.getInstance().reference.child("home_list")
    }

    private val userDatabaseReference by lazy {
        FirebaseDatabase.getInstance().getReference("users")
    }

    private val udbReference = userDatabaseReference.child("user1")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MyPageViewModel::class.java)) {
            return MyPageViewModel(
                UserGetItemsUseCase(UserRepositoryImpl(userDatabaseReference)),
                udbReference
            ) as T
        } else {
            throw IllegalArgumentException("Not found ViewModel class.")
        }

    }
}