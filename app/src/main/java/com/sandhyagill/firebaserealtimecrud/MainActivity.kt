package com.sandhyagill.firebaserealtimecrud


import android.app.Dialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.Firebase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import com.sandhyagill.firebaserealtimecrud.databinding.ActivityMainBinding
class MainActivity : AppCompatActivity() {
    var list = arrayListOf<Student>()
    lateinit var arrayAdapter : ArrayAdapter<Student>
    var firebaseDatabase = Firebase.database
    val binding : ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        arrayAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, list)
        binding.listView.adapter = arrayAdapter

        firebaseDatabase.reference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                list.clear()
                for(snapshots in dataSnapshot.children) {
                    Log.d("TAG", "Value is: ${dataSnapshot.value}  ")
                 //   var name = dataSnapshot.child("name").getValue(String::class.java)
                    var name = snapshots.getValue(Student::class.java)
                    name?.id = snapshots.key
                    Log.d("TAG", "Value is: ${snapshots.value}  value $name")
                    //list.add(value?: Student())
                    name?.let {
                        list.add(it)
                    }
                    arrayAdapter.notifyDataSetChanged()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Failed to read value
                Log.w("TAG", "Failed to read value.", error.toException())
            }
        })

        binding.fab.setOnClickListener {
            firebaseDatabase.reference.push().setValue(Student(name = "Dristi", rollno = 1))
//            firebaseDatabase.reference.push().setValue("testing updated")
//            firebaseDatabase.reference.child("testing1").setValue("updated value")
//            firebaseDatabase.reference.child(list[0].id?:"").removeValue()
//            firebaseDatabase.reference.child(list[0].id?:"").setValue("Sandhya")
        }
        
//        binding.btnUpdate.setOnClickListener {
//            firebaseDatabase.reference.child(list[0].id?:"" ).setValue(Student(name = "Sandhya", rollno = 2))
//        }
//        binding.btnDelete.setOnClickListener {
//            firebaseDatabase.reference.child(list[0].id?:"").removeValue()
//        }
        binding.listView.setOnItemLongClickListener { parent, view, position, id ->
            AlertDialog.Builder(this)
                .setTitle("Are you sure to delete")
                .setPositiveButton("yes"){_,_->
                    firebaseDatabase.reference.child(list[0].id?:"").removeValue()
                    Toast.makeText(this, "Deleted", Toast.LENGTH_SHORT).show()
                }
                .setNegativeButton("No"){_,_->
                    Toast.makeText(this, "Not Deleted", Toast.LENGTH_SHORT).show()
                }
                .show()
            return@setOnItemLongClickListener true
        }
        binding.listView.setOnItemClickListener { parent, view, position, id ->
            val selectedItem = list[position]
            var dialog = Dialog(this)
            dialog.setContentView(R.layout.custom_dialog_box)
            var name = dialog.findViewById<EditText>(R.id.etName)
            var rollno = dialog.findViewById<EditText>(R.id.etRollno)
            var btnUpdate = dialog.findViewById<Button>(R.id.btnUpdate)

            name.setText(selectedItem.name)
            rollno.setText(selectedItem.rollno.toString())

            btnUpdate.setOnClickListener {

                if (name.text.toString().trim().isNullOrEmpty()){
                    name.error = resources.getString(R.string.enter_name)
                }else if (rollno.text.toString().trim().isNullOrEmpty()){
                    rollno.error = resources.getString(R.string.enter_rollno)
                }else{
                    var updatedName = name.text.toString()
                    var updatedRollNo = rollno.text.toString().toInt()

                    var studentRef = firebaseDatabase.reference.child(selectedItem.id!!)
                    studentRef.child("name").setValue(updatedName)
                    studentRef.child("rollno").setValue(updatedRollNo)
                    arrayAdapter.notifyDataSetChanged()

                    var student = Student(selectedItem.id, name = updatedName, rollno = updatedRollNo)
                    list.set(position,student)
                    arrayAdapter.notifyDataSetChanged()
                    Toast.makeText(this, "Student updated successfully", Toast.LENGTH_SHORT).show()
                }
            }
            dialog.window?.setLayout(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            )
            dialog.show()
            return@setOnItemClickListener
        }
    }
}
