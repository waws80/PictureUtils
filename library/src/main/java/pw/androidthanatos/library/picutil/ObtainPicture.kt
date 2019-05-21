package pw.androidthanatos.library.picutil

import android.Manifest
import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.FragmentActivity
import pw.androidthanatos.library.picutil.delegate.DelegateFragment
import pw.androidthanatos.library.picutil.type.OpenType
import java.io.File
import java.lang.RuntimeException

/**
 * 功能描述: 获取图片工具类
 * @className: ObtainPicture.kt
 * @author: thanatos
 * @createTime: 2019/5/21
 * @updateTime: 2019/5/21 13:42
 */
object ObtainPicture {

    //配置类
    private var configureBean: ConfigureBean? = null

    //打开相机失败
    const val MSG_OPEN_CAMERA_FAILURE = "打开相机失败"

    //打开相册失败
    const val MSG_OPEN_PICTURE_FAILURE = "打开相册失败"

    //页面回调请求码
    const val CODE_OPEN_REQUEST_CODE = 254

    /**
     * 初始化
     */
    fun init(configureBean: ConfigureBean){
        this.configureBean = configureBean
    }


    /**
     * 打开相机
     */
    fun openCamera(activity: FragmentActivity, requestCode: Int, picUri: Uri, pictureCallback:(Uri)->Unit){
        if (checkPermission()){
            val delegateFragment = DelegateFragment.invoke(requestCode = requestCode, toastCall = configureBean?.toastCallback!!)
            delegateFragment.takePictureUri = picUri
            delegateFragment.takePictureCallBack = pictureCallback
            attacheFragment(activity, delegateFragment)
        }else{
            toastCall("没有打开相机的权限")
        }
    }

    /**
     * 打开相册
     */
    fun openPicture(activity: FragmentActivity, requestCode: Int, pictureCallback: (Uri) -> Unit){
        if (checkStoragePermission()){
            val delegateFragment = DelegateFragment.invoke(requestCode = requestCode, type = OpenType.Picture,
                toastCall = configureBean?.toastCallback!!)
            delegateFragment.takePictureCallBack = pictureCallback
            attacheFragment(activity, delegateFragment)
        }else{
            toastCall("没有打开相册的权限")
        }
    }

    /**
     * 裁剪相机返回的图片
     */
    fun cropPhotoByCamera(activity: FragmentActivity, requestCode: Int, picUri: Uri, cropCallback:(Bitmap)->Unit){
        openCamera(activity, requestCode, picUri){
           crop(activity, requestCode, it, cropCallback)
        }
    }


    /**
     * 裁剪相册图片
     */
    fun cropPhotoByPicture(activity: FragmentActivity, requestCode: Int, cropCallback:(Bitmap)->Unit){
        openPicture(activity, requestCode){
           crop(activity, requestCode, it, cropCallback)
        }
    }

    /**
     * 裁剪图片
     */
    private fun crop(activity: FragmentActivity, requestCode: Int, uri: Uri, cropCallback: (Bitmap) -> Unit){
        val delegateFragment = DelegateFragment.invoke(requestCode, OpenType.Crop,
            toastCall = configureBean?.toastCallback!!)
        delegateFragment.cropUri = uri
        delegateFragment.cropPictureCallback = cropCallback
        attacheFragment(activity, delegateFragment)
    }




    /**
     * 检测是否有内存卡读写权限和相机权限
     */
    fun checkPermission(): Boolean{
        return checkStoragePermission() && checkCameraPermission()
    }

    /**
     * 检测是否有内存卡读写权限
     */
    fun checkStoragePermission(): Boolean{
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.M){
            return true
        }
        val context = checkContext()
        return ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
    }


    /**
     * 检测是否有相机权限
     */
    fun checkCameraPermission(): Boolean{
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.M){
            return true
        }
        val context = checkContext()
        return ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
    }


    /**
     * 获取文件uri
     */
    fun getFileUri(file: File): Uri{
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
            FileProvider.getUriForFile(checkContext(),
                "pw.androidthanatos.library.picutil.FileProvider", file)
        }else{
            Uri.fromFile(file)
        }
    }


    /**
     * 检查上下文
     */
    private fun checkContext(): Context{
        return configureBean?.context?.baseContext ?: throw RuntimeException("context == null")
    }

    /**
     * 提示信息
     */
    private fun toastCall(msg: String){
        configureBean?.toastCallback?.invoke(msg)
    }


    /**
     * 绑定代理fragment
     */
    private fun attacheFragment(attachActivity: FragmentActivity, delegateFragment: DelegateFragment){
        val transaction = attachActivity.supportFragmentManager.beginTransaction()
        transaction.add(delegateFragment,"DelegateFragment" + System.currentTimeMillis())
            .commit()
    }







    /**
     * 功能描述: 获取图片配置类
     * @className: ObtainPicture.kt
     * @author: thanatos
     * @createTime: 2019/5/21
     * @updateTime: 2019/5/21 13:46
     */
    data class ConfigureBean(val isDebug: Boolean = false,
                             val context: Application,
                             val toastCallback: (String)->Unit = {})
}