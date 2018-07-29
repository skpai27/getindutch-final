package com.weikang.getindutch;

import java.util.HashMap;

public class MPFSummaryPageExpensesClass {

    private String payerUid;
    private String groupNameOrPayeeName;
    private String description;
    private float amountPaid;
    private float amountOwedPerPax;
    private String expenseIconUrl;
    private HashMap<String, String> peopleSharingExpense;

    public MPFSummaryPageExpensesClass(String payerUid, String groupNameOrPayeeName, String description,
                                       float amountPaid, float amountOwedPerPax, HashMap<String, String> peopleSharingExpense,
                                       String expenseIconUrl){
        this.payerUid = payerUid;
        this.groupNameOrPayeeName = groupNameOrPayeeName;
        this.description = description;
        this.amountPaid = amountPaid;
        this.amountOwedPerPax = amountOwedPerPax;
        this.peopleSharingExpense = peopleSharingExpense;
        this.expenseIconUrl = expenseIconUrl;
    }

    public MPFSummaryPageExpensesClass(){}

    //public boolean getIsGroup(){ return isGroup; }
    public String getPayerUid(){return payerUid;}
    public String getGroupNameOrPayeeName(){return groupNameOrPayeeName;}
    public String getDescription(){return description;}
    public float getAmountPaid(){return amountPaid;}
    public float getAmountOwedPerPax() { return amountOwedPerPax; }
    public HashMap<String, String> getPeopleSharingExpense() { return peopleSharingExpense; }
    public String getExpenseIconUrl(){return expenseIconUrl;}

    //public void setIsGroup(boolean isGroup){this.isGroup = isGroup;}
    public void setPayerUid(String payerUid){this.payerUid = payerUid;}
    public void setGroupNameOrPayeeName(String groupNameOrPayeeName){this.groupNameOrPayeeName = groupNameOrPayeeName;}
    public void setDescription(String description){this.description = description;}
    public void setAmountPaid(float amountPaid){this.amountPaid = amountPaid;}
    public void setAmountOwedPerPax(float amountOwedPerPax){this.amountOwedPerPax = amountOwedPerPax;}
    public void setExpenseIconUrl(String url){this.expenseIconUrl = url;}
}
