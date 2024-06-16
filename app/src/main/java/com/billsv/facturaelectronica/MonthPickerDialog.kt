package com.billsv.facturaelectronica
import android.app.Dialog
import android.os.Bundle
import android.widget.NumberPicker
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class MonthPickerDialog: DialogFragment() {

    interface OnMonthSelectedListener {
        fun onMonthSelected(month: Int)
    }

    private var listener: OnMonthSelectedListener? = null

    fun setOnMonthSelectedListener(listener: OnMonthSelectedListener) {
        this.listener = listener
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val monthPicker = NumberPicker(requireContext()).apply {
            minValue = 0
            maxValue = 11
            displayedValues = arrayOf(
                "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
                "Julio", "Augosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"
            )
        }

        return MaterialAlertDialogBuilder(requireContext())
            .setTitle("Seleccione el Mes")
            .setView(monthPicker)
            .setPositiveButton("Seleccionar") { _, _ ->
                listener?.onMonthSelected(monthPicker.value)
            }
            .setNegativeButton("Cancelar", null)
            .create()
    }
}
