package org.j_keepass.util;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;

import com.google.android.material.textfield.TextInputEditText;

import org.j_keepass.fields.dtos.FieldData;
import org.j_keepass.db.operation.Db;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.atomic.AtomicReference;

public class DateAndTimePickerUtil {
    public void showDateAndTimePicker(TextInputEditText editText, Date toBeDisplayedDateObj, FieldData fieldData) {
        Calendar toBeDisplayedDate = Calendar.getInstance();
        toBeDisplayedDate.setTime(toBeDisplayedDateObj);
        Calendar currentCalender = Calendar.getInstance(TimeZone.getDefault());
        DatePickerDialog datePickerDialog = new DatePickerDialog(editText.getContext(), (view, year, month, dayOfMonth) -> {
            currentCalender.set(year, month, dayOfMonth);
            AtomicReference<Date> selectedExpiryDate = new AtomicReference<>(currentCalender.getTime());
            editText.setText(Utils.convertDateToString(selectedExpiryDate.get()));
            TimePickerDialog timePickerDialog = new TimePickerDialog(editText.getContext(), (view1, hourOfDay, minute) -> {
                currentCalender.set(year, month, dayOfMonth, hourOfDay, minute);
                selectedExpiryDate.set(currentCalender.getTime());
                editText.setText(Utils.convertDateToString(selectedExpiryDate.get()));
                Utils.log("Calling update field Value");
                fieldData.value = editText.getText().toString();
                Db.getInstance().updateEntryField(Db.getInstance().getCurrentEntryId(), fieldData);
            }, toBeDisplayedDate.get(Calendar.HOUR_OF_DAY), toBeDisplayedDate.get(Calendar.MINUTE), true);
            timePickerDialog.show();
        }, toBeDisplayedDate.get(Calendar.YEAR), toBeDisplayedDate.get(Calendar.MONTH), toBeDisplayedDate.get(Calendar.DAY_OF_MONTH));

        datePickerDialog.show();
    }
}
