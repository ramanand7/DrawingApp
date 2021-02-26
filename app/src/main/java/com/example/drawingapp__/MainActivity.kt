package com.example.drawingapp__

import android.Manifest
import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Path
import android.media.Image
import android.media.MediaScannerConnection
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.get
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.lang.Exception

class MainActivity : AppCompatActivity() {
    lateinit var drawing__view : DrawinView
    private var mImageButtonCurrentPaint: ImageButton? =
            null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        setContentView(R.layout.activity_main)

         drawing__view  = findViewById(R.id.drawing_view)
        drawing__view.setSizeForBrush(5.toFloat())

        mImageButtonCurrentPaint = findViewById<LinearLayout>(R.id.ll_paint_colors)[1] as ImageButton
        mImageButtonCurrentPaint!!.setImageDrawable(
                ContextCompat.getDrawable(
                        this,
                        R.drawable.pallet_pressed
                )
        )

        findViewById<ImageButton>(R.id.ib_brush).setOnClickListener({
            showBrushSizeChooserDialoge()
        })

        findViewById<ImageButton>(R.id.ib_gallery).setOnClickListener({
            if (isReadStoage()){
               val pickphotointent = Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI)

                startActivityForResult(pickphotointent,GALLERY)

            }else
            {
                requestStoragePermission()
            }
        })

        findViewById<ImageButton>(R.id.ib_undo).setOnClickListener({
            drawing__view.onClickUndo()
        })

        findViewById<ImageButton>(R.id.ib_save).setOnClickListener({
            if (isReadStoage()){
                BitmapAsyncTask(getBitmapRomView(findViewById(R.id.fl_drawingviewConatainer))).execute()

            }else{
                requestStoragePermission()
            }
        })

    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK){

            if (requestCode == GALLERY){
                try {
                    if (data!!.data != null){
                       findViewById<ImageView>(R.id.iv_background).visibility = View.VISIBLE
                        findViewById<ImageView>(R.id.iv_background).setImageURI(data.data)
                    }else{
                        Toast.makeText(this,"error in parsing image or corrupted ",Toast.LENGTH_SHORT).show()
                    }

                }catch (e : Exception){
                    e.printStackTrace()
                }

            }

        }
    }


    private fun showBrushSizeChooserDialoge(){
        val brushDialoge= Dialog(this)
        brushDialoge.setContentView(R.layout.diloagebrushsize)
        brushDialoge.setTitle("brushSize: ")
        val smallBtn = brushDialoge.findViewById<ImageButton>(R.id.ib_small_brush)
        smallBtn.setOnClickListener({
            drawing__view.setSizeForBrush(5.toFloat())
            brushDialoge.dismiss()
        })

        val mediumButton = brushDialoge.findViewById<ImageButton>(R.id.ib_medium_brush)
        mediumButton.setOnClickListener({
            drawing__view.setSizeForBrush(20.toFloat())
            brushDialoge.dismiss()
        })

        val largebutn = brushDialoge.findViewById<ImageButton>(R.id.ib_large_brush)
        largebutn.setOnClickListener({
            drawing__view.setSizeForBrush(30.toFloat())
            brushDialoge.dismiss()
        })

        brushDialoge.show()
    }

    fun paintClicked(view: View) {
        if (view !== mImageButtonCurrentPaint) {
            // Update the color
            val imageButton = view as ImageButton
            // Here the tag is used for swaping the current color with previous color.
            // The tag stores the selected view
            val colorTag = imageButton.tag.toString()
            // The color is set as per the selected tag here.
            drawing__view.setColor(colorTag)
            // Swap the backgrounds for last active and currently active image button.
            imageButton.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.pallet_pressed))
            mImageButtonCurrentPaint!!.setImageDrawable(
                    ContextCompat.getDrawable(
                            this,
                            R.drawable.pallet_normal
                    )
            )

            //Current view is updated with selected view in the form of ImageButton.
            mImageButtonCurrentPaint = view
        }

    }

    private fun requestStoragePermission(){
        if(ActivityCompat.shouldShowRequestPermissionRationale(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE).toString())){
               Toast.makeText(this,"need permission for load image",Toast.LENGTH_SHORT).show()
                //if the user denay
        }
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), STORAGE_PERMISSION_CODE)

    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode== STORAGE_PERMISSION_CODE){
            if (grantResults.isNotEmpty() && grantResults[0]==PackageManager.PERMISSION_GRANTED)
               Toast.makeText(this,"now you can read files ",Toast.LENGTH_SHORT).show()
        }else{
            Toast.makeText(this,"oops you denied the permission  ",Toast.LENGTH_SHORT).show()
        }
    }

    private fun isReadStoage():Boolean{
        val result = ContextCompat.checkSelfPermission(this,Manifest.permission.READ_EXTERNAL_STORAGE)
        return result == PackageManager.PERMISSION_GRANTED
    }

    private fun getBitmapRomView(view : View) : Bitmap{
        val returnBitmap=  Bitmap.createBitmap(view.width , view.height,Bitmap.Config.ARGB_8888)

        val canvas = Canvas(returnBitmap)
        val bgDrabwable = view.background
        if (bgDrabwable != null){
            bgDrabwable.draw(canvas)
        }
        else
        {
            canvas.drawColor(Color.WHITE)
        }
        view.draw(canvas)
        return returnBitmap
    }

    private  inner class BitmapAsyncTask(val mBitmap : Bitmap) : AsyncTask<Any, Void, String>() {
        override fun doInBackground(vararg params: Any?): String {
            var result = " "
            if (mBitmap != null)
            {
                try {
                    val bytes = ByteArrayOutputStream()
                    mBitmap.compress(Bitmap.CompressFormat.PNG,90,bytes)
                    val f = File(externalCacheDir!!.absoluteFile.toString()+ File.separator + "kidsDrawApp "+ System.currentTimeMillis()/1000 + ".png")
                    val fos = FileOutputStream(f)
                    fos.write(bytes.toByteArray())
                    fos.close()
                    result = f.absolutePath
                }catch (e : Exception){
                    result = " "
                    e.printStackTrace()
                }

            }
            return result
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            if (!result!!.isEmpty()){

                Toast.makeText(this@MainActivity,"File saved Suscessfullly : $result",Toast.LENGTH_SHORT).show()
            }
            else
            { Toast.makeText(this@MainActivity,"something went wrong while saving the file ",Toast.LENGTH_SHORT).show()}

            outerpostexecutework(result)
        }


    }

 fun outerpostexecutework(result: String?){
     MediaScannerConnection.scanFile(this, arrayOf(result),null){
         Path,uri->val shareIntent = Intent()
         shareIntent.action = Intent.ACTION_SEND
         shareIntent.putExtra(Intent.EXTRA_STREAM,uri)
         shareIntent.type = "image/png"
         startActivity(Intent.createChooser(
                 shareIntent ,"Share"
         ))
     }

 }
    companion object {
        private const val STORAGE_PERMISSION_CODE = 1
        private const val GALLERY = 2
    }

}