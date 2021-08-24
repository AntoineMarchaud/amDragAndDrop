package com.amarchaud.amdraganddrop.screen

import android.content.ClipData
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Point
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.*
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.setFragmentResultListener
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.amarchaud.amdraganddrop.R
import com.amarchaud.amdraganddrop.databinding.ActivityMainBinding
import com.amarchaud.amdraganddrop.databinding.ItemOnePersonBinding
import com.amarchaud.amdraganddrop.domain.entity.EntityOnePerson
import com.amarchaud.amdraganddrop.screen.new_user.NewUserDialog
import com.bumptech.glide.Glide
import dagger.hilt.android.AndroidEntryPoint

data class ItemWithPosition(
    val position: Int,
    val v: View
)

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainActivityViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        binding.run {

            newUser.setOnClickListener {
                val customPopup = NewUserDialog.newInstance()
                customPopup.show(supportFragmentManager, "NewUserDialog")
                customPopup.setFragmentResultListener(NewUserDialog.OUTPUT) { _, bundle ->
                    val newUser = bundle.getParcelable<EntityOnePerson>(NewUserDialog.PERSON_ADDED)
                    newUser?.let { addOnePerson(newUser) }
                }
            }
        }

        viewModel.loadingStatus.observe(this, ::handleLoading)
        viewModel.People.observe(this, ::handleState)
        viewModel.fetchPeople()
    }

    private fun handleLoading(loadingStatus: LoadingStatus?) {
        loadingStatus?.let {
            when (loadingStatus) {
                is LoadingStatus.StateLoadingStart -> {
                    binding.progressBar.visibility = View.VISIBLE
                }
                is LoadingStatus.StateLoadingEnd -> {
                    binding.progressBar.visibility = View.GONE
                }
            }
        }
    }

    private fun handleState(status: Status?) {
        status?.let {
            when (status) {
                is Status.StatusOk -> {
                    // like SubmitList, check existing items rather than deleteAll/addAll
                    removeAllView()
                    addAllViews(status.profiles)
                }
                is Status.StatusError -> {
                    AlertDialog.Builder(this)
                        .setTitle("Error !")
                        .setMessage(status.message)
                        .setPositiveButton(
                            android.R.string.ok
                        ) { _, _ ->
                            viewModel.fetchPeople() // retry
                        }
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show()
                }
            }
        }
    }

    private fun removeAllView() {
        binding.run {
            peopleList.removeAllViews()
        }
    }

    private fun addAllViews(l: List<EntityOnePerson>?) {
        binding.run {
            l?.forEach {
                addOnePerson(it)
            }
        }
    }

    private fun addOnePerson(onePerson: EntityOnePerson) {
        binding.run {


            val itemBinding =
                ItemOnePersonBinding.inflate(LayoutInflater.from(this@MainActivity)).apply {

                    when (onePerson.platform) {
                        "iOS" -> imagePlatform.setImageResource(R.drawable.ios)
                        "Android" -> imagePlatform.setImageResource(R.drawable.android)
                        "Fullstack" -> imagePlatform.setImageResource(R.drawable.fullstack)
                    }

                    onePersonName.text = onePerson.name
                    onePersonPosition.text = onePerson.position

                    onePerson.pic?.let {

                        val circularProgressDrawable =
                            CircularProgressDrawable(this@MainActivity.baseContext)
                        circularProgressDrawable.strokeWidth = 5f
                        circularProgressDrawable.centerRadius = 30f
                        circularProgressDrawable.start()

                        Glide.with(this@MainActivity)
                            .load(onePerson.pic)
                            .placeholder(circularProgressDrawable)
                            .error(R.drawable.placeholder)
                            .into(onePersonImage)

                    } ?: onePersonImage.setImageResource(R.drawable.placeholder)

                    root.setOnLongClickListener { v ->
                        val data =
                            ClipData.newPlainText(onePerson.id.toString(), onePerson.name)
                        val myShadow = MyDragShadowBuilder(v)
                        v.startDragAndDrop(
                            data,
                            myShadow,
                            ItemWithPosition(position = peopleList.indexOfChild(v), v = v),
                            0
                        )

                        true
                    }

                    deleteButton.setOnClickListener {
                        //Remove currentLine
                        peopleList.removeView(root)
                        viewModel.deletePerson(onePerson) // remove db
                    }
                }


            itemBinding.root.setOnDragListener(dragListen)
            peopleList.addView(itemBinding.root)
        }
    }

    private class MyDragShadowBuilder(v: View) : View.DragShadowBuilder(v) {

        private val shadow = ColorDrawable(Color.LTGRAY)

        // Defines a callback that sends the drag shadow dimensions and touch point back to the
        // system.
        override fun onProvideShadowMetrics(size: Point, touch: Point) {
            // Sets the width of the shadow to half the width of the original View
            val width: Int = view.width / 2

            // Sets the height of the shadow to half the height of the original View
            val height: Int = view.height / 2

            // The drag shadow is a ColorDrawable. This sets its dimensions to be the same as the
            // Canvas that the system will provide. As a result, the drag shadow will fill the
            // Canvas.
            shadow.setBounds(0, 0, width, height)

            // Sets the size parameter's width and height values. These get back to the system
            // through the size parameter.
            size.set(width, height)

            // Sets the touch point's position to be in the middle of the drag shadow
            touch.set(width / 2, height / 2)
        }

        // Defines a callback that draws the drag shadow in a Canvas that the system constructs
        // from the dimensions passed in onProvideShadowMetrics().
        override fun onDrawShadow(canvas: Canvas) {
            // Draws the ColorDrawable in the Canvas passed in from the system.
            shadow.draw(canvas)
        }
    }

    private val dragListen = View.OnDragListener { v, event ->
        when (event.action) {
            DragEvent.ACTION_DRAG_STARTED -> {
                true
            }
            DragEvent.ACTION_DRAG_ENTERED -> {
                true
            }

            DragEvent.ACTION_DRAG_LOCATION -> {
                true
            }
            DragEvent.ACTION_DRAG_EXITED -> {
                true
            }
            DragEvent.ACTION_DROP -> {

                val idStr = event.clipData.description.label
                val itemWithPosition = event.localState as ItemWithPosition
                val posRemoved = itemWithPosition.position
                val view = itemWithPosition.v

                val positionInserted = binding.peopleList.indexOfChild(v)

                binding.peopleList.removeViewAt(posRemoved)
                binding.peopleList.addView(view, positionInserted)

                viewModel.order(idStr.toString().toInt(), positionInserted)
                true
            }

            DragEvent.ACTION_DRAG_ENDED -> {
                true
            }
            else -> {
                // An unknown action type was received.

                false
            }
        }
    }
}