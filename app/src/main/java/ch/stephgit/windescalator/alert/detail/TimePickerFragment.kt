package ch.stephgit.windescalator.alert.detail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TimePicker
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import ch.stephgit.windescalator.R
import java.util.*

class TimePickerFragment : DialogFragment() {

    companion object {
        const val TAG = "TimePickerDialog"
    }

    private lateinit var cancelButton: Button
    private lateinit var okButton: Button
    private lateinit var viewModel: TimeViewModel
    private var leadingZeroMin: String = ""
    private var leadingZeroHour: String = ""

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_timepicker, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(requireActivity()).get(TimeViewModel::class.java)

        val c = Calendar.getInstance()

        val picker: TimePicker = view.findViewById(R.id.timePicker)
        picker.hour = c.get(Calendar.HOUR_OF_DAY)
        picker.minute = c.get(Calendar.MINUTE)
        picker.setIs24HourView(true)

        //Implement like TimePickerDialog
        okButton = view.findViewById(R.id.btn_time_ok)
        cancelButton = view.findViewById(R.id.btn_time_cancel)
        okButton.setOnClickListener {
            if (picker.minute < 10) { leadingZeroMin = "0" }
            if (picker.hour < 10) { leadingZeroHour = "0" }
            viewModel.sendTime( leadingZeroHour + picker.hour.toString() + ":" + leadingZeroMin + picker.minute.toString())
            dismiss()
        }

        cancelButton.setOnClickListener {
            dismiss()
        }
    }

}