package com.example.modabba;

public class transactionhistory {
    String transactionid;
    String AddorMinus;
    String name;
    String time;
    String paymentmethod;
    String amount;
    Boolean deductedBool;

    public transactionhistory(){}

    public transactionhistory(String transactionid, String addorMinus, String name,
                              String time, String paymentmethod, String amount, Boolean deductedBool) {
        this.transactionid = transactionid;
        AddorMinus = addorMinus;
        this.name = name;
        this.time = time;
        this.paymentmethod = paymentmethod;
        this.amount = amount;
        this.deductedBool = deductedBool;
    }

    public Boolean getDeductedBool() {
        return deductedBool;
    }

    public void setDeductedBool(Boolean deductedBool) {
        this.deductedBool = deductedBool;
    }

    public String getTransactionid() {
        return transactionid;
    }

    public void setTransactionid(String transactionid) {
        this.transactionid = transactionid;
    }

    public String getAddorMinus() {
        return AddorMinus;
    }

    public void setAddorMinus(String addorMinus) {
        AddorMinus = addorMinus;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getPaymentmethod() {
        return paymentmethod;
    }

    public void setPaymentmethod(String paymentmethod) {
        this.paymentmethod = paymentmethod;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }
}
