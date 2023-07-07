package com.example.modabba;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

public class HistoryViewpagerAdapter extends FragmentPagerAdapter {

    private int tabCount;

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position){
            case 0:
                return "MyOrders";
            case 1:
                return "Transaction";
        }
        return null;
    }

    @Override
    public Fragment getItem(int position) {
        // Cloud Firebase 数据库中<users> 集合下的 <UserDocumentId> 文档的 <Wallet> 集合
        switch (position){
            case 0 :
                return new SubcriptionHistoryFragment();//订单
            case 1:
                return new TransactionHistoryFragment();//钱包充值信息
        }
        return null;
    }

    public HistoryViewpagerAdapter(FragmentManager fm, int tabs) {
        super(fm);
        this.tabCount = tabs;
    }

    @Override
    public int getCount() {
        return tabCount;
    }
}
