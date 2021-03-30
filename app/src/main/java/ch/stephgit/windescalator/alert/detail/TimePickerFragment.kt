package ch.stephgit.windescalator.alert.detail

import android.app.TimePickerDialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TimePicker
import androidx.fragment.app.DialogFragment
import ch.stephgit.windescalator.R
import java.util.*

class TimePickerFragment : DialogFragment(), TimePickerDialog.OnTimeSetListener {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        var view = inflater.inflate(R.layout.fragment_timepicker, container, false)

        val c = Calendar.getInstance()

        var picker: TimePicker = view.findViewById(R.id.timePicker)
        picker.hour = c.get(Calendar.HOUR_OF_DAY)
        picker.minute = c.get(Calendar.MINUTE)
        picker.setIs24HourView(true)

        //Implement like TimePickerDialog

        return view
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }
//    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
//        super.onCreateDialog(savedInstanceState)
//        // Use the current time as the default values for the picker
//        val c = Calendar.getInstance()
//        val hour = c.get(Calendar.HOUR_OF_DAY)
//        val minute = c.get(Calendar.MINUTE)
//
//        // Create a new instance of TimePickerDialog and return it
//        return TimePickerDialog(activity, this, hour, minute, true)
//    }

    override fun onTimeSet(view: TimePicker, hourOfDay: Int, minute: Int) {
        // Do something with the time chosen by the user
    }
}