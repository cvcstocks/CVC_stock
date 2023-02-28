package com.stock.mx2.utils;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
public class Pager<T> {


        /**
         * 每頁显示條數
         */
        private int pageSize;
        /**
         * 原集合
         */
        private List<T> data;

        private Pager(List<T> data, int pageSize) {
            this.data = data;
            this.pageSize = pageSize;
        }

        /**
         * 創建分頁器
         *
         * @param data 需要分頁的數據
         * @param pageSize 每頁显示條數
         * @param <T> 業務對象
         * @return 分頁器
         */
        public static <T> Pager<T> create(List<T> data, int pageSize) {
            return new Pager<>(data, pageSize);
        }

        /**
         * 得到分頁后的數據
         *
         * @param pageNum 頁碼
         * @return 分頁后結果
         */
        public List<T> getPagedList(int pageNum) {
            int fromIndex = (pageNum - 1) * pageSize;
            if (fromIndex >= data.size()) {
                return Collections.emptyList();
            }

            int toIndex = pageNum * pageSize;
            if (toIndex >= data.size()) {
                toIndex = data.size();
            }
            return data.subList(fromIndex, toIndex);
        }

        public int getPageSize() {
            return pageSize;
        }

        public List<T> getData() {
            return data;
        }

        public static void main(String[] args) {
            Integer[] array = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12};
            List<Integer> list = Arrays.asList(array);

            Pager<Integer> pager = Pager.create(list, 10);

            List<Integer> page1 = pager.getPagedList(1);
            System.out.println(page1);

            List<Integer> page2 = pager.getPagedList(2);
            System.out.println(page2);

            List<Integer> page3 = pager.getPagedList(3);
            System.out.println(page3);
        }
    }

