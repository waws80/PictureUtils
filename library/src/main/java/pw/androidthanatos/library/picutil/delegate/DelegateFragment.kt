package pw.androidthanatos.library.picutil.delegate

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import androidx.core.os.BuildCompat
import androidx.fragment.app.Fragment
import pw.androidthanatos.library.picutil.ObtainPicture
import pw.androidthanatos.library.picutil.type.OpenType
import java.io.File
import java.lang.Exception

/**
 * 功能描述: 请求打开相机和相册代理类
 * @className: DelegateFragment.kt
 * @author: thanatos
 * @createTime: 2019/5/21
 * @updateTime: 2019/5/21 14:15
 */
class DelegateFragment : Fragment(){

    companion object{

        /**
         * 创建代理fragment
         */
        @JvmName("create")
        @JvmStatic
        fun invoke(requestCode: Int = ObtainPicture.CODE_OPEN_REQUEST_CODE,
                   type: OpenType = OpenType.Camera,
                   toastCall: (String)->Unit): DelegateFragment {

            val delegateFragment = DelegateFragment()
            delegateFragment.mOpenType = type
            delegateFragment.mRequestCode = requestCode
            delegateFragment.mToastCall = toastCall

            return delegateFragment
        }
    }

    private var mOpenType: OpenType = OpenType.Camera
    private var mToastCall: (String)->Unit = {}
    private var mRequestCode: Int = ObtainPicture.CODE_OPEN_REQUEST_CODE

    var takePictureUri: Uri? = null
    var cropUri: Uri? = null
    var cropOutUri: Uri? = null
    var takePictureCallBack:(Uri)->Unit = {}
    var cropPictureCallback:(Bitmap)->Unit = {}


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        when(mOpenType){
            OpenType.Camera -> openCamera()
            OpenType.Picture -> openPicture()
            OpenType.Crop -> crop()
        }
    }

    /**
     * 打开相机
     */
    private fun openCamera() {
        try {
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            intent.putExtra(MediaStore.EXTRA_OUTPUT, takePictureUri)
            startActivityForResult(intent,mRequestCode)
        }catch (e: Exception){
            mToastCall.invoke(ObtainPicture.MSG_OPEN_CAMERA_FAILURE)
        }
    }

    /**
     * 打开相册
     */
    private fun openPicture(){
        try {
            val intent = Intent()
            intent.action = Intent.ACTION_PICK
            intent.type = "image/**"
            startActivityForResult(intent, mRequestCode)
        }catch (e: Exception){
            mToastCall.invoke(ObtainPicture.MSG_OPEN_PICTURE_FAILURE)
        }
    }


    private fun crop(){
        val crop = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val cropPhoto = File(crop.path, "Crop.jpg")
        try {
            if (cropPhoto.exists()) {
                cropPhoto.delete()
            }
            cropPhoto.createNewFile()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        cropOutUri = Uri.fromFile(cropPhoto)
        // 调用系统中自带的图片剪裁
        val intent = Intent("com.android.camera.action.CROP")
        intent.setDataAndType(cropUri, "image/*")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            //添加这一句表示对目标应用临时授权该Uri所代表的文件
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        intent.putExtra("crop", "true")
        intent.putExtra("scale", true)

        intent.putExtra("aspectX", 1)
        intent.putExtra("aspectY", 1)

        //输出的宽高
        intent.putExtra("outputX", 100)
        intent.putExtra("outputY", 100)
        if (BuildCompat.isAtLeastQ()) {
            intent.putExtra("return-data", true)
        } else {
            intent.putExtra(MediaStore.EXTRA_OUTPUT, cropOutUri)
        }
        intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString())
        intent.putExtra("noFaceDetection", true)
        startActivityForResult(intent, mRequestCode)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == mRequestCode && mOpenType == OpenType.Crop){
            val bitmap = data?.extras?.get("data") as Bitmap?
            if (bitmap != null){
                cropPictureCallback.invoke(bitmap)
                deleteSelf()
                return
            }
        }
        if (requestCode == mRequestCode && resultCode == Activity.RESULT_OK){
            when(mOpenType){
                OpenType.Camera -> {
                    if (this.takePictureUri != null){
                        takePictureCallBack.invoke(this.takePictureUri!!)
                    }
                }
                OpenType.Picture -> {
                    if (data != null){
                        this.takePictureUri = data.data
                        takePictureCallBack.invoke(this.takePictureUri!!)
                    }
                }
                OpenType.Crop -> {
                    val b = BitmapFactory.decodeStream(requireContext().contentResolver.openInputStream(cropOutUri))
                    cropPictureCallback.invoke(b)
                }
            }
        }
        deleteSelf()
    }


    /**
     * 自杀
     */
    private fun deleteSelf(){
        requireActivity().supportFragmentManager.beginTransaction()
            .remove(this).commitAllowingStateLoss()
    }


}