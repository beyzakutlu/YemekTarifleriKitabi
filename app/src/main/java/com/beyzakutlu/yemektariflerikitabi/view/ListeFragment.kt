package com.beyzakutlu.yemektariflerikitabi.view

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.room.Room
import com.beyzakutlu.yemektariflerikitabi.adapter.TarifAdapter
import com.beyzakutlu.yemektariflerikitabi.databinding.FragmentListeBinding
import com.beyzakutlu.yemektariflerikitabi.model.Tarif
import com.beyzakutlu.yemektariflerikitabi.roomdb.TarifDao
import com.beyzakutlu.yemektariflerikitabi.roomdb.TarifDatabase
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers

class ListeFragment : Fragment() {
    private var _binding: FragmentListeBinding? = null

    private val binding get() = _binding!!
    private lateinit var db: TarifDatabase
    private lateinit var tarifDao: TarifDao
    private val mDisposable = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        db= Room.databaseBuilder(requireContext(), TarifDatabase::class.java,"Tarifler").build()
        tarifDao= db.tarifDao()
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentListeBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.floatingActionButton.setOnClickListener { yeniEkle(it) }
        binding.tarifRecyclerView.layoutManager= LinearLayoutManager(requireContext())
        verileriAl()


        // Arama çubuğu için fonksiyonumuz
        binding.searchView.setOnQueryTextListener(object : androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                (binding.tarifRecyclerView.adapter as? TarifAdapter)?.filter(newText ?: "")
                return true
            }
        })
    }

    private fun verileriAl (){
        mDisposable.add(
            tarifDao.getAll()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::handleResponse)
        )
    }
    //verileriAl fonskiyonundan sonra çalışıcak listeden bir tarif döndürecek
    //O yüzden adapter bunun içinde olucak
    private fun handleResponse(tarifler: List<Tarif>){
        val adapter= TarifAdapter(tarifler)
        binding.tarifRecyclerView.adapter=adapter
    }

    // FloatingActionButton a atadığımız OnClick metodu
    fun yeniEkle (view: View){
        val action= ListeFragmentDirections.actionListeFragmentToTarifFragment(bilgi = "yeni",id=0)
        Navigation.findNavController(view).navigate(action)
    }

    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}