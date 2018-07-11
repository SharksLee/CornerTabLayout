package com.example.admin.cornertablayout;

import android.support.annotation.Nullable;


import java.util.Collection;

 /**
   *@author lishaojie
   *create at 2018/7/9 16:14
  */

public final class CollectionUtil {
    private CollectionUtil() {
    }

    public static boolean isEmpty(@Nullable Collection collection) {
        return null == collection || 0 == collection.size();
    }



}
