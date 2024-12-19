package org.j_keepass.util;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;

import com.google.android.material.textfield.TextInputEditText;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.atomic.AtomicReference;

public class DateAndTimePickerUtil {
    public void showDateAndTimePicker(TextInputEditText editText, Date toBeDisplayedDateObj) {
        Calendar toBeDisplayedDate = Calendar.getInstance();
        toBeDisplayedDate.setTime(toBeDisplayedDateObj);
        Calendar currentCalender = Calendar.getInstance(TimeZone.getDefault());
        DatePickerDialog datePickerDialog = new DatePickerDialog(editText.getContext(), (view, year, month, dayOfMonth) -> {
            currentCalender.set(year, month, dayOfMonth);
            AtomicReference<Date> selectedExpiryDate = new AtomicReference<>(currentCalender.getTime());
            editText.setText(Util.convertDateToString(selectedExpiryDate.get()));
            TimePickerDialog timePickerDialog = new TimePickerDialog(editText.getContext(), (view1, hourOfDay, minute) -> {
                currentCalender.set(year, month, dayOfMonth, hourOfDay, minute);
                selectedExpiryDate.set(currentCalender.getTime());
                editText.setText(Util.convertDateToString(selectedExpiryDate.get()));
            }, toBeDisplayedDate.get(Calendar.HOUR_OF_DAY), toBeDisplayedDate.get(Calendar.MINUTE), true);
            timePickerDialog.show();
        }, toBeDisplayedDate.get(Calendar.YEAR), toBeDisplayedDate.get(Calendar.MONTH), toBeDisplayedDate.get(Calendar.DAY_OF_MONTH));

        datePickerDialog.show();
    }
}
