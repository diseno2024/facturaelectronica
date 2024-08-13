package com.billsv.facturaelectronica
import android.app.Dialog
import android.os.Bundle
import android.widget.NumberPicker
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.util.Calendar

class YearPickerDialog : DialogFragment() {

    interface OnYearSelectedListener {
        fun onYearSelected(year: Int)
    }

    private var listener: OnYearSelectedListener? = null

    fun setOnYearSelectedListener(listener: OnYearSelectedListener) {
        this.listener = listener
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        // Ensure the current year is an integer
        val currentYear: Int = Calendar.getInstance().get(Calendar.YEAR)

        val yearPicker = NumberPicker(requireContext()).apply {
            minValue = 2000
            maxValue = currentYear
            value = currentYear
        }

        return MaterialAlertDialogBuilder(requireContext())
            .setTitle("Seleccione el Año")
            .setView(yearPicker)
            .setPositiveButton("Seleccionar") { _, _ ->
                listener?.onYearSelected(yearPicker.value)
            }
            .setNegativeButton("Cancelar", null)
            .create()
    }
}
