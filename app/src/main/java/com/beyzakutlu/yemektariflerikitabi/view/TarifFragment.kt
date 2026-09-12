package com.beyzakutlu.yemektariflerikitabi.view

import android.os.Bundle
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.Navigation
import androidx.room.Room
import com.beyzakutlu.yemektariflerikitabi.databinding.FragmentTarifBinding
import com.beyzakutlu.yemektariflerikitabi.model.Tarif
import com.beyzakutlu.yemektariflerikitabi.roomdb.TarifDao
import com.beyzakutlu.yemektariflerikitabi.roomdb.TarifDatabase
import com.google.android.material.snackbar.Snackbar
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import java.io.ByteArrayOutputStream
import java.io.IOException



class TarifFragment : Fragment() {
    private var _binding: FragmentTarifBinding? = null
    private val binding get() = _binding!!

    private lateinit var permissionLauncher : ActivityResultLauncher<String>        //İZİN İSTEMEK İÇİN
    private lateinit var activityResultLauncher: ActivityResultLauncher<Intent>     // GALERİYE GİTMEK İÇİN

    private var secilenGorsel : Uri?=null
    private var secilenBitmap: Bitmap?=null

    private lateinit var db: TarifDatabase
    private lateinit var tarifDao: TarifDao
    // Devamlı istek yapıldığında hafızada birikmemesi için kullan at işini yapar
     private val mDisposable = CompositeDisposable()
    private  var secilenTarif: Tarif?=null


    override fun onCreate(savedInstanceState: Bundle?) {    //OnCreate de DAO yu oluşturuyoruz
        super.onCreate(savedInstanceState)
        registerLauncher()


        db= Room.databaseBuilder(requireContext(), TarifDatabase::class.java,"Tarifler").build()
        tarifDao= db.tarifDao()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentTarifBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.imageView.setOnClickListener { gorselSec(it) }
        binding.kaydetbutton.setOnClickListener { kaydet(it) }
        binding.silbutton.setOnClickListener { sil(it) }
        binding.geritusu.setOnClickListener { gerigit(it) }

        arguments?.let {
            val bilgi = TarifFragmentArgs.fromBundle(it).bilgi
            if(bilgi=="yeni"){
                // Yeni tarif ekleneceği için sil butonu pasif, kaydet butonu aktif olmalı
                secilenTarif=null
                binding.silbutton.isEnabled=false
                binding.kaydetbutton.isEnabled=true
            }else{
                // Eski tarif görüntüleniyorsa silinebilir veya güncellenebilir
                binding.silbutton.isEnabled=true
                binding.kaydetbutton.isEnabled=false
                val id = TarifFragmentArgs.fromBundle(it).id

                mDisposable.add(
                    tarifDao.findById(id)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(this::handleResponse)

                    // Bu fonksiyonu farklı bir handle fonskiyonuna bağlıcaz
                )
            }

        }

    }
    private fun handleResponse(tarif: Tarif){
        val bitmap= BitmapFactory.decodeByteArray(tarif.gorsel,0,tarif.gorsel.size)
        binding.imageView.setImageBitmap(bitmap)
        binding.isimText.setText(tarif.isim)
        binding.malzemeText.setText(tarif.malzeme)
        //imageview kısmı biraz daha farklı, byte dizisini görsele çeviricez

        secilenTarif=tarif
    }
    fun gerigit (view: View){
        val action = TarifFragmentDirections.actionTarifFragmentToListeFragment()
        Navigation.findNavController(view).navigate(action)
    }
    
    fun kaydet (view: View){
        val isim = binding.isimText.text.toString()
        val malzeme = binding. malzemeText.text.toString()

        if(secilenBitmap !=null){
            val kucukBitmap = kucukBitmapOlustur(secilenBitmap!!,300)
            val outputStream= ByteArrayOutputStream()
            kucukBitmap.compress(Bitmap.CompressFormat.PNG,50,outputStream)
            val byteDizisi = outputStream.toByteArray()

            val tarif = Tarif(isim,malzeme,byteDizisi)

            // RXJAVA
            mDisposable.add(// hem internet hem veri tabanı işlemlerinde io kullanılır
                tarifDao.insert(tarif).subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(this::handleResponseForInsert))

            //Bunun sonucu this e aktarılacak
        }


    }
    private fun handleResponseForInsert (){
        //bir önceki fragmenta dön
        val action = TarifFragmentDirections.actionTarifFragmentToListeFragment()
        Navigation.findNavController(requireView()).navigate(action)
    }
    fun sil(view: View){
        if (secilenTarif != null){
            mDisposable.add(
                tarifDao.delete(tarif = secilenTarif!!).subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(this:: handleResponseForInsert))
        }




    }


    fun gorselSec(view: View){

        //TEELFON TİREMUSUDAN KÜÇÜKSE ESKİ VERSİYON, BÜYÜKSE YENİ VERSİYON KOD KULLANILIR
        //AYNI KOD AMA SADECE READ EXTERNAL STORAGE KISMI READ MEDIA IMAGES olarak değiştirildi
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){

            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED){
                // izin verilmemiş, izin istememiz gerekiyor

                //kullanıcıdan izin alınmamış o yüzden izin veriyo mu diye sorduk
                if(ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), Manifest.permission.READ_MEDIA_IMAGES)){
                    //snackbar göstermemiz lazım, kullanıcıdan neden izin istediğimizi bir kez daha söyleyerek izin istememiz lazım
                    Snackbar.make(view,"Galeriye ulaşıp görsel seçmemiz lazım", Snackbar.LENGTH_INDEFINITE).setAction(
                        "İzin Ver", View.OnClickListener{
                            //izin isteyeceğiz
                            permissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)

                        }
                    ).show()

                }else{
                    //izin isteyeceğiz
                    permissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
                }

            }else{
                //izin verilmiş galeriye gidebilirim
                val intentToGallery= Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                activityResultLauncher.launch(intentToGallery)
            }
        }else{
            //"Kullanıcı bu izni uygulamaya verdi mi, yoksa henüz vermedi mi?"
            //Manifesti import ederken ANDROİD olanı seç
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                // izin verilmemiş, izin istememiz gerekiyor

                //kullanıcıdan izin alınmamış o yüzden izin veriyo mu diye sorduk
                if(ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), Manifest.permission.READ_EXTERNAL_STORAGE)){
                    //snackbar göstermemiz lazım, kullanıcıdan neden izin istediğimizi bir kez daha söyleyerek izin istememiz lazım
                    Snackbar.make(view,"Galeriye ulaşıp görsel seçmemiz lazım", Snackbar.LENGTH_INDEFINITE).setAction(
                        "İzin Ver", View.OnClickListener{
                            //izin isteyeceğiz
                            permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)

                        }
                    ).show()

                }else{
                    //izin isteyeceğiz
                    permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                }

            }else{
                //izin verilmiş galeriye gidebilirim
                val intentToGallery= Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                activityResultLauncher.launch(intentToGallery)
            }
        }



    }


    //GORSEL İSTEMEK İÇİN
    private fun registerLauncher (){ // ONCREATE içinde çağırmayı unutma
        activityResultLauncher=registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
            result ->
            if(result.resultCode== AppCompatActivity.RESULT_OK){
                val intentFromResult= result.data
                if(intentFromResult!= null){
                   secilenGorsel= intentFromResult.data

                    try {
                        if(Build.VERSION.SDK_INT>=28){
                            //YENİ YÖNTEM
                            val source = ImageDecoder.createSource (requireActivity().contentResolver,secilenGorsel!!)
                            secilenBitmap= ImageDecoder.decodeBitmap(source)
                            binding.imageView.setImageBitmap(secilenBitmap)
                        }else{
                            //ESKİ YÖNTEM
                            secilenBitmap= MediaStore.Images.Media.getBitmap(requireActivity().contentResolver,secilenGorsel)
                            binding.imageView.setImageBitmap(secilenBitmap)
                        }
                    }catch (e: IOException){
                        println(e.localizedMessage)
                    }


                }
            }
        }
        //İZİN İSTEMEK İÇİN
        permissionLauncher= registerForActivityResult(ActivityResultContracts.RequestPermission()){
            result ->
            if(result) {
                //izin verildi
                //galeriye gidebiliriz
                val intentToGallery= Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                activityResultLauncher.launch(intentToGallery)


            }else{
                //izin verilmedi
                Toast.makeText(requireContext(),"İzin verilmedi", Toast.LENGTH_LONG).show()

            }
        }

    }

    private fun kucukBitmapOlustur (kullanicininSectigiBitmap: Bitmap, maximumBoyut: Int): Bitmap{

        //kullanicinin seçtiği görselin wight ve height değerleri
        var width =kullanicininSectigiBitmap.width
        var height=kullanicininSectigiBitmap.height
        if (width <= maximumBoyut && height <= maximumBoyut) {
            return kullanicininSectigiBitmap
        }

        val bitmapOrani: Double = width.toDouble()/height.toDouble()

        if(bitmapOrani>1){ // GORSEL YATAY
            width= maximumBoyut
            val kisaltilmisYukseklik= width / bitmapOrani
            height=kisaltilmisYukseklik.toInt()

        }else{  // GÖRSEL DİKEY
            height=maximumBoyut
            val kisaltilmisGenislik= height*bitmapOrani
            width=kisaltilmisGenislik.toInt()
        }
        return Bitmap.createScaledBitmap(kullanicininSectigiBitmap,width,height,true)




    }




    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }




}