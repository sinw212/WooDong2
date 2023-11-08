package com.wd.woodong2.presentation.home.content

import android.util.Log
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.FirebaseDatabase
import com.wd.woodong2.presentation.chat.content.UserItem
import kotlinx.coroutines.launch
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import com.wd.woodong2.data.repository.MapSearchRepositoryImpl
import com.google.firebase.messaging.FirebaseMessaging
import com.wd.woodong2.data.repository.UserRepositoryImpl
import com.wd.woodong2.domain.model.MapSearchEntity
import com.wd.woodong2.domain.repository.MapSearchRepository
import com.wd.woodong2.domain.usecase.MapSearchCircumLocationGetItemsUseCase
import com.wd.woodong2.domain.usecase.MapSearchGetItemsUseCase
import com.wd.woodong2.domain.provider.FirebaseTokenProvider
import com.wd.woodong2.domain.usecase.UserGetItemsUseCase
import com.wd.woodong2.presentation.home.map.HomeMapActivity
import com.wd.woodong2.presentation.home.map.HomeMapSearchItem
import com.wd.woodong2.retrofit.KAKAORetrofitClient

class HomeViewModel(
    private val userItem: UserGetItemsUseCase,
    private val circumLocationItem: MapSearchCircumLocationGetItemsUseCase,
    private val MapSearch : MapSearchGetItemsUseCase
) : ViewModel(
) {

    private val _list: MutableLiveData<List<HomeItem>> = MutableLiveData()
    val list: LiveData<List<HomeItem>> get() = _list


    private val _circumLocationList: MutableLiveData<List<HomeMapSearchItem>> = MutableLiveData()
    val circumLocationList: LiveData<List<HomeMapSearchItem>> get() = _circumLocationList
    //주변 위치 값 받아오기
    private var circumLocation = mutableSetOf<String>()

    private var countOne : Int = 0
    private var countTwo : Int = 0
    private var countThree : Int = 0

    //Home에 출력할 list 설정하기
    val _printList: MutableLiveData<List<HomeItem>> = MutableLiveData()
    val printList: LiveData<List<HomeItem>> get() = _printList

    private val _originalList: MutableLiveData<List<HomeItem>> = MutableLiveData()  // 원본 리스트 저장
    val originalList: LiveData<List<HomeItem>> get() = _originalList

    //userItem
    val userId = "user1"
    val userInfo: MutableLiveData<UserItem> = MutableLiveData()

    init {
        loadDataFromFirebase()
        getUserItem()
        loadDataFromFirebase()
    }

    fun circumLocationItemSearch(
        y: Double,
        x: Double,
        radius: Int,
        query: String,
        //locationType: Int?,
        userLocation: String,
    ) = viewModelScope.launch {
        runCatching {

            //Log.d("locationCheckci", locationType.toString())
            Log.d("locationCheckcq", query)
            val set = "주민센터"

                countOne = 0
                countTwo = 0
                countThree = 0

                if(circumLocation.isNotEmpty()){
                    circumLocation.clear()
                }

                var circumLocationItems = createCircumLocationItems(
                    Map = circumLocationItem(
                        y,
                        x,
                        radius,
                        "$query $set")
                )

                _circumLocationList.postValue(circumLocationItems)
                circumLocationItems = createCircumLocationItems(
                    Map = MapSearch(query)
                )
                _circumLocationList.postValue(circumLocationItems)

                Log.d("locationCheckci1", circumLocation.toString())

                //주변 위치 설정하기
                for(item in circumLocationItems){
                    if(item is HomeMapSearchItem.MapSearchItem){
                        val address = item.address
                        if (address != null) {
                            HomeMapActivity.fullNameLocationInfo(address)
                            if(HomeMapActivity.extractLocationInfo(userLocation.toString()) != HomeMapActivity.extractLocationInfo(address))  //사용자 현재위치는 설정 x
                            {circumLocation.add(HomeMapActivity.fullLocationName.toString())}
                        }
                    }
                }
                Log.d("locationCheckci1", circumLocation.toString())

            if(circumLocation.isNotEmpty() && circumLocation.size >= countOne ){
                    for(loc in circumLocation)
                    {
                        Log.d("locationCheckl1",loc)
                        val existingList = _printList.value.orEmpty()
                        //지역의 값이 일치할때
                        val filteredList = list.value?.filter { it.location == loc }
                        val combinedList = existingList.toMutableList().apply { addAll(filteredList!!) }
                        _printList.value = combinedList
                        countOne++
                    }
                }
                Log.d("locationCheckci1", countOne.toString())


            if(printList.value?.size!! < 10){
                val circumLocationItems = createCircumLocationItems(
                    Map = circumLocationItem(
                        y,
                        x,
                        radius,
                        "${HomeMapActivity.extractDistrictInfo(query)} $set")
                )

                _circumLocationList.postValue(circumLocationItems)
                //주변 위치 설정하기
                for(item in circumLocationItems){
                    if(item is HomeMapSearchItem.MapSearchItem){
                        val address = item.address
                        if (address != null) {
                            HomeMapActivity.fullNameLocationInfo(address)
                            if(HomeMapActivity.extractLocationInfo(userLocation.toString()) != HomeMapActivity.extractLocationInfo(address))  //사용자 현재위치는 설정 x
                            {circumLocation.add(HomeMapActivity.fullLocationName.toString())}
                        }
                    }
                }
                Log.d("locationCheckci2", circumLocation.toString())

                if(circumLocation.isNotEmpty()&& circumLocation.size >= countTwo ){
                    for(loc in circumLocation) {
                        if(countOne <= countTwo){
                            Log.d("locationCheckl2",loc)
                            val existingList = _printList.value.orEmpty()
                            //지역의 값이 일치할때
                            val filteredList = list.value?.filter { it.location == loc }
                            val combinedList = existingList.toMutableList().apply { addAll(filteredList!!) }
                            _printList.value = combinedList

                        }
                        countTwo++
                    }
                }
                Log.d("locationCheckci2", countTwo.toString())
                if(printList.value?.size!! < 10){

                    val circumLocationItems = createCircumLocationItems(
                        Map = circumLocationItem(
                            y,
                            x,
                            radius,
                            "${HomeMapActivity.extractCityInfo(query)} $set")
                    )

                    _circumLocationList.postValue(circumLocationItems)
                    //주변 위치 설정하기
                    for(item in circumLocationItems){
                        if(item is HomeMapSearchItem.MapSearchItem){
                            val address = item.address
                            if (address != null) {
                                HomeMapActivity.fullNameLocationInfo(address)
                                if(HomeMapActivity.extractLocationInfo(userLocation.toString()) != HomeMapActivity.extractLocationInfo(address))  //사용자 현재위치는 설정 x
                                {circumLocation.add(HomeMapActivity.fullLocationName.toString())}
                            }
                        }
                    }
                    Log.d("locationCheckci3", circumLocation.toString())

                    if(circumLocation.isNotEmpty() && circumLocation.size >= countThree){
                        for(loc in circumLocation) {
                            if(countTwo <= countThree){
                                Log.d("locationCheckl3",loc)
                                val existingList = _printList.value.orEmpty()
                                //지역의 값이 일치할때
                                val filteredList = list.value?.filter { it.location == loc }
                                val combinedList = existingList.toMutableList().apply { addAll(filteredList!!) }
                                _printList.value = combinedList

                            }
                            countThree++

                            if(countThree == circumLocation.size)
                            {
                                countOne = 0
                                countTwo = 0
                                countThree = 0
                                break
                            }

                        }

                        if(printList.value?.size!! < 10){
                            var checkCount = 0
                            for(loc in circumLocation) {
                                val existingList = _printList.value.orEmpty()
                                val filteredList = list.value?.filter { it.location != loc }
                                checkCount++
                                if(checkCount == circumLocation.size){
                                    val combinedList = existingList.toMutableList().apply { addAll(filteredList!!) }
                                    _printList.value = combinedList
                                }
                            }

                        }
                    }

                }
            }
        }.onFailure { e->
            Log.e("Retrofit Error", "Request failed: ${e.message}")
        }
    }

    private fun createCircumLocationItems(
        Map: MapSearchEntity
    ): List<HomeMapSearchItem> {
        fun createMapSearchItems(
            Map: MapSearchEntity
        ): List<HomeMapSearchItem.MapSearchItem> = Map.documents.map { document ->
            HomeMapSearchItem.MapSearchItem(
                address = document.addressName,
                x = document.x,
                y = document.y
            )
        }

        return createMapSearchItems(Map)
    }

    private fun loadDataFromFirebase() {
        val databaseReference = FirebaseDatabase.getInstance().reference.child("home_list")
        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val dataList = ArrayList<HomeItem>()

                for (postSnapshot in dataSnapshot.children) {
                    val firebaseData = postSnapshot.getValue(HomeItem::class.java)
                    if (firebaseData != null) {
                        dataList.add(firebaseData)
                    }
                }
                _originalList.value = dataList
                _list.value = dataList

            }

            override fun onCancelled(databaseError: DatabaseError) {
            }
        })
    }

    fun filterList(tag: String) {
        _list.value = if (tag == "All") {
            _originalList.value  // 전체 리스트
        } else {
            _originalList.value?.filter { it.tag == tag }  // 태그가 일치하는 리스트
        }
    }

    private fun getUserItem() = viewModelScope.launch {
        runCatching {
            userItem(userId).collect { user ->
                val userItem =
                    UserItem(
                        id = user?.id ?: "",
                        name = user?.name ?: "",
                        imgProfile = user?.imgProfile ?: "",
                        email = user?.email ?: "",
                        chatIds = user?.chatIds.orEmpty(),
                        groupIds = user?.groupIds.orEmpty(),        //모임
                        likedIds = user?.likedIds.orEmpty(),        //좋아요 게시물
                        writtenIds = user?.writtenIds.orEmpty(),        //작성한 게시물
                        firstLocation = user?.firstLocation ?: "",
                        secondLocation = user?.secondLocation ?: ""
                    )

                userInfo.postValue(userItem)
            }
        }.onFailure {
            Log.e("homeItem", it.message.toString())
        }
    }

    fun updateUserLocation(
        firstLocation: String,
        secondLocation: String,
    ) = viewModelScope.launch {
        runCatching {
            userItem(userId, firstLocation, secondLocation)
        }
    }
    fun deleteItem(item: HomeItem) {
        // Firebase에서 항목 삭제
        val itemId = item.id // 항목의 고유 ID 또는 키
        deleteItemFromFirebase(itemId)

        // 원본 목록에서 항목을 제거하고 LiveData를 업데이트합니다.
        _originalList.value = _originalList.value?.filter { it != item }
    }

    private fun deleteItemFromFirebase(itemId: String) {
        val databaseReference = FirebaseDatabase.getInstance().getReference("home_list")
        val itemReference = databaseReference.child(itemId)

        itemReference.removeValue()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {

                } else {
                    val exception = task.exception
                    if (exception != null) {
                        // 오류 처리 코드
                    }
                }
            }
    }
}

class HomeViewModelFactory : ViewModelProvider.Factory {

    private val userRepositoryImpl by lazy {
        UserRepositoryImpl(
            FirebaseDatabase.getInstance().getReference("users"),
            Firebase.auth,
            FirebaseTokenProvider(FirebaseMessaging.getInstance())
        )
    }
    private val circumLocationrepository: MapSearchRepository = MapSearchRepositoryImpl(
        KAKAORetrofitClient.search
    )
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            return HomeViewModel(
                UserGetItemsUseCase(userRepositoryImpl),
                MapSearchCircumLocationGetItemsUseCase(circumLocationrepository),
                MapSearchGetItemsUseCase(circumLocationrepository)
            ) as T
        } else {
            throw IllegalArgumentException("Not found ViewModel class.")
        }

    }
}