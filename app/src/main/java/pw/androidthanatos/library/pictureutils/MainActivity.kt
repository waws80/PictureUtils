package pw.androidthanatos.library.pictureutils

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import pw.androidthanatos.library.picutil.ObtainPicture
import java.io.File

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        ObtainPicture.init(ObtainPicture.ConfigureBean(true, application) {
            Toast.makeText(this,it, Toast.LENGTH_SHORT).show()
        })

    }



    fun click(v: View){
        val file = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "/test/picture.jpg")
        if (!file.parentFile.exists()) {
            file.parentFile.mkdirs()
        }

        ObtainPicture.cropPhotoByPicture(this, 100){
            (v as ImageView).setImageBitmap(it)
        }
    }
}
