package com.splitemapp.android.screen.expense;

import android.widget.DatePicker;

import com.splitemapp.android.screen.DatePickerFragment;
import com.splitemapp.commons.domain.UserExpense;

import java.util.Calendar;

/**
 * Created by nicolas on 1/31/17.
 */

public class ExpenseDatePickerFragment extends DatePickerFragment {

    private UserExpense mUserExpense;
    private ExpenseFragment mExpenseFragment;

    @Override
    public void onDateSet(DatePicker view, int year, int month, int day) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.MONTH, month);
        cal.set(Calendar.DAY_OF_MONTH, day);
        mUserExpense.setExpenseDate(cal.getTime());
        mExpenseFragment.updateExpenseDateDisplay(mUserExpense);
    }

    public UserExpense getmUserExpense() {
        return mUserExpense;
    }

    public void setmUserExpense(UserExpense mUserExpense) {
        this.mUserExpense = mUserExpense;
    }

    public ExpenseFragment getmExpenseFragment() {
        return mExpenseFragment;
    }

    public void setmExpenseFragment(ExpenseFragment mExpenseFragment) {
        this.mExpenseFragment = mExpenseFragment;
    }
}
